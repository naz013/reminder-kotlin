package com.elementary.tasks.groups.create

import android.content.Context
import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.utils.VibrationPlayer
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.groups.NotificationOverrideSubtitleFormatter
import com.elementary.tasks.groups.NotificationOverrideSubtitles
import com.elementary.tasks.groups.usecase.DeleteGroupUseCase
import com.elementary.tasks.groups.usecase.SaveGroupUseCase
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.common.ContextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.NotificationSettings
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderSettingsRepository
import com.github.naz013.ui.common.theme.ThemeProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class EditGroupViewModelTest : BaseTest() {
  private val groupV2Repository = mockk<GroupV2Repository>()
  private val dateTimeManager = mockk<DateTimeManager>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val intentDataReader = mockk<IntentDataReader>()
  private val contextProvider = mockk<ContextProvider>()
  private val deleteGroupUseCase = mockk<DeleteGroupUseCase>(relaxed = true)
  private val saveGroupUseCase = mockk<SaveGroupUseCase>(relaxed = true)
  private val reminderSettingsRepository = mockk<ReminderSettingsRepository>()
  private val vibrationPlayer = mockk<VibrationPlayer>(relaxed = true)
  private val prefs = mockk<Prefs>(relaxed = true)
  private val notificationOverrideSubtitleFormatter = mockk<NotificationOverrideSubtitleFormatter>(relaxed = true)
  private val themeProvider = mockk<ThemeProvider>(relaxed = true)

  private lateinit var viewModel: EditGroupViewModel

  private fun groupV2(
    id: String = "1",
    title: String = "Work",
    isDefault: Boolean = false,
    notification: NotificationSettingsOverride = NotificationSettingsOverride(),
  ) = GroupV2(
    uuId = id,
    title = title,
    color = 0,
    isDefault = isDefault,
    notification = notification,
    syncState = SyncState.Synced,
  )

  private fun groupV2FromFile(
    id: String = "9",
    title: String = "From File",
    isDefault: Boolean = false,
  ) = groupV2(id = id, title = title, isDefault = isDefault)

  private fun buildViewModel(
    id: String = "1",
    fromIntentData: Boolean = false,
  ) = EditGroupViewModel(
    id = id,
    fromIntentData = fromIntentData,
    dispatcherProvider = mockDispatcherProvider(),
    groupV2Repository = groupV2Repository,
    dateTimeManager = dateTimeManager,
    analyticsEventSender = analyticsEventSender,
    intentDataReader = intentDataReader,
    contextProvider = contextProvider,
    deleteGroupUseCase = deleteGroupUseCase,
    saveGroupUseCase = saveGroupUseCase,
    reminderSettingsRepository = reminderSettingsRepository,
    vibrationPlayer = vibrationPlayer,
    prefs = prefs,
    notificationOverrideSubtitleFormatter = notificationOverrideSubtitleFormatter,
    themeProvider = themeProvider,
  )

  @Before
  override fun setUp() {
    super.setUp()
    every { contextProvider.themedContext } returns mockk<Context>(relaxed = true)
    every { dateTimeManager.getCurrentDateTime() } returns org.threeten.bp.LocalDateTime.now()
    every { reminderSettingsRepository.getNotificationDefaults() } returns NotificationSettings()
    every { notificationOverrideSubtitleFormatter.format(any(), any()) } returns NotificationOverrideSubtitles()
    coEvery { groupV2Repository.getById(any()) } returns null
    coEvery { groupV2Repository.getById("1") } returns groupV2(id = "1")
    coEvery { groupV2Repository.countAll() } returns 2

    viewModel = buildViewModel()
  }

  @Test
  fun `loads existing group into state on first collection`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals("Work", state.title)
      assertEquals(true, state.isEdited)
      assertEquals(false, state.isFromFile)
    }

  @Test
  fun `canDelete is false when the group is the only one`() =
    runTest {
      coEvery { groupV2Repository.countAll() } returns 1

      val state = viewModel.state.first()

      assertEquals(false, state.canDelete)
    }

  @Test
  fun `canDelete is false when the group is the default group`() =
    runTest {
      coEvery { groupV2Repository.getById("1") } returns groupV2(id = "1", isDefault = true)

      val state = viewModel.state.first()

      assertEquals(false, state.canDelete)
    }

  @Test
  fun `canDelete is true when there are multiple groups and this is not default`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals(true, state.canDelete)
    }

  @Test
  fun `loads from intent data and detects a matching group already in db`() =
    runTest {
      val fromFile = groupV2FromFile(id = "9", title = "From File")
      every { intentDataReader.get(IntentKeys.INTENT_ITEM, GroupV2::class.java) } returns fromFile
      coEvery { groupV2Repository.getById("9") } returns groupV2(id = "9", title = "From File")
      val fileViewModel = buildViewModel(id = "9", fromIntentData = true)

      val state = fileViewModel.state.first()

      assertEquals("From File", state.title)
      assertEquals(true, state.isFromFile)
      assertEquals(true, state.hasSameInDb)
      assertEquals(false, state.canDelete)
    }

  @Test
  fun `loads from intent data when no matching group exists in db`() =
    runTest {
      val fromFile = groupV2FromFile(id = "9", title = "From File")
      every { intentDataReader.get(IntentKeys.INTENT_ITEM, GroupV2::class.java) } returns fromFile
      coEvery { groupV2Repository.getById("9") } returns null
      val fileViewModel = buildViewModel(id = "9", fromIntentData = true)

      val state = fileViewModel.state.first()

      assertEquals(false, state.hasSameInDb)
    }

  @Test
  fun `does not reload group data on a second fresh state collection`() =
    runTest {
      viewModel.state.first()
      viewModel.state.first()

      // load() is guarded to run once - see EditGroupViewModel.hasLoaded. Without the guard,
      // navigating to a sub-screen (e.g. "Workflow rules") and back re-subscribes `state`,
      // re-running load() and silently overwriting any unsaved edit to a field it sets
      // (title/colorPosition/isDefault/notification).
      coVerify(exactly = 1) { groupV2Repository.getById("1") }
    }

  private fun observeState(): () -> EditGroupState {
    var latest = EditGroupState()
    CoroutineScope(Dispatchers.Unconfined).launch { viewModel.state.collect { latest = it } }
    return { latest }
  }

  @Test
  fun `onNameChanged updates title and clears title error`() {
    val latest = observeState()

    viewModel.onNameChanged("New Title")

    assertEquals("New Title", latest().title)
    assertEquals(false, latest().titleError)
  }

  @Test
  fun `onColorSelected updates color position`() {
    val latest = observeState()

    viewModel.onColorSelected(3)

    assertEquals(3, latest().colorPosition)
  }

  @Test
  fun `onDefaultCheckChanged updates isDefault flag`() {
    val latest = observeState()

    viewModel.onDefaultCheckChanged(true)

    assertEquals(true, latest().isDefault)
  }

  @Test
  fun `onSaveClick sets titleError when title is blank`() =
    runTest {
      viewModel.state.first()
      viewModel.onNameChanged("   ")

      viewModel.onSaveClick()

      assertEquals(true, viewModel.state.first().titleError)
      coVerify(exactly = 0) { saveGroupUseCase(any()) }
    }

  @Test
  fun `onSaveClick shows copy conflict dialog when from file and already in db`() =
    runTest {
      val fromFile = groupV2FromFile(id = "9", title = "From File")
      every { intentDataReader.get(IntentKeys.INTENT_ITEM, GroupV2::class.java) } returns fromFile
      coEvery { groupV2Repository.getById("9") } returns groupV2(id = "9", title = "From File")
      val fileViewModel = buildViewModel(id = "9", fromIntentData = true)
      fileViewModel.state.first()

      fileViewModel.onSaveClick()

      assertEquals(EditGroupDialog.CopyConflict, fileViewModel.state.first().dialog)
    }

  @Test
  fun `onSaveClick saves the group and navigates back`() =
    runTest {
      viewModel.state.first()

      viewModel.onSaveClick()

      coVerify(exactly = 1) { saveGroupUseCase(any()) }
      val event = viewModel.navigationEvent.value?.peekContent()
      assertEquals(EditGroupViewModel.NavigationEvent.Back, event)
    }

  @Test
  fun `onCopyKeepClick dismisses dialog and saves a copy under a new id`() =
    runTest {
      val fromFile = groupV2FromFile(id = "9", title = "From File")
      every { intentDataReader.get(IntentKeys.INTENT_ITEM, GroupV2::class.java) } returns fromFile
      coEvery { groupV2Repository.getById("9") } returns groupV2(id = "9", title = "From File")
      val fileViewModel = buildViewModel(id = "9", fromIntentData = true)
      fileViewModel.state.first()
      fileViewModel.onSaveClick()

      fileViewModel.onCopyKeepClick()

      assertNull(fileViewModel.state.first().dialog)
      coVerify(exactly = 1) { saveGroupUseCase(match { it.uuId != "9" }) }
    }

  @Test
  fun `onCopyReplaceClick dismisses dialog and saves replacing the same id`() =
    runTest {
      val fromFile = groupV2FromFile(id = "9", title = "From File")
      every { intentDataReader.get(IntentKeys.INTENT_ITEM, GroupV2::class.java) } returns fromFile
      coEvery { groupV2Repository.getById("9") } returns groupV2(id = "9", title = "From File")
      val fileViewModel = buildViewModel(id = "9", fromIntentData = true)
      fileViewModel.state.first()
      fileViewModel.onSaveClick()

      fileViewModel.onCopyReplaceClick()

      assertNull(fileViewModel.state.first().dialog)
      coVerify(exactly = 1) { saveGroupUseCase(match { it.uuId == "9" }) }
    }

  @Test
  fun `onDeleteMenuClick shows delete confirmation dialog`() =
    runTest {
      viewModel.state.first()

      viewModel.onDeleteMenuClick()

      assertEquals(EditGroupDialog.DeleteConfirm, viewModel.state.first().dialog)
    }

  @Test
  fun `onDialogDismiss clears the dialog`() =
    runTest {
      viewModel.state.first()
      viewModel.onDeleteMenuClick()

      viewModel.onDialogDismiss()

      assertNull(viewModel.state.first().dialog)
    }

  @Test
  fun `onDeleteConfirmed does nothing when group cannot be deleted`() =
    runTest {
      coEvery { groupV2Repository.countAll() } returns 1
      viewModel.state.first()

      viewModel.onDeleteConfirmed()

      coVerify(exactly = 0) { deleteGroupUseCase(any()) }
    }

  @Test
  fun `onDeleteConfirmed deletes the group and navigates back when allowed`() =
    runTest {
      viewModel.state.first()

      viewModel.onDeleteConfirmed()

      coVerify(exactly = 1) { deleteGroupUseCase("1") }
      val event = viewModel.navigationEvent.value?.peekContent()
      assertEquals(EditGroupViewModel.NavigationEvent.Back, event)
    }

  @Test
  fun `loads the group's existing notification override into state`() =
    runTest {
      coEvery { groupV2Repository.getById("1") } returns
        groupV2(id = "1", notification = NotificationSettingsOverride(priority = ReminderPriority.HIGH, vibrate = true))

      val state = viewModel.state.first()

      assertEquals(ReminderPriority.HIGH, state.notification.priority)
      assertEquals(true, state.notification.vibrate)
    }

  @Test
  fun `onSaveClick persists the edited notification override with the group`() =
    runTest {
      viewModel.state.first()

      viewModel.onPriorityClick()
      // index 0 is "Inherit from Settings", so index 1 is the first real option (LOWEST).
      viewModel.onNotificationChoiceSelected(1)
      viewModel.onSaveClick()

      coVerify(exactly = 1) { saveGroupUseCase(match { it.notification.priority == ReminderPriority.LOWEST }) }
    }

  @Test
  fun `onNotificationChoiceSelected with index 0 clears the override back to inherit`() =
    runTest {
      coEvery { groupV2Repository.getById("1") } returns
        groupV2(id = "1", notification = NotificationSettingsOverride(priority = ReminderPriority.HIGH))
      val latest = observeState()
      viewModel.state.first()

      viewModel.onPriorityClick()
      viewModel.onNotificationChoiceSelected(0)

      assertNull(latest().notification.priority)
    }

  @Test
  fun `onVibrationPatternClick then selecting a preset plays it and does not play for inherit`() =
    runTest {
      viewModel.state.first()

      viewModel.onVibrationPatternClick()
      viewModel.onNotificationChoiceSelected(1)

      verify { vibrationPlayer.play(any()) }
    }

  @Test
  fun `onVibrationPatternClick then selecting inherit does not play a vibration`() =
    runTest {
      viewModel.state.first()

      viewModel.onVibrationPatternClick()
      viewModel.onNotificationChoiceSelected(0)

      verify(exactly = 0) { vibrationPlayer.play(any()) }
    }

  @Test
  fun `onDelayMinutesConfirm stores the preview value only when overridden`() {
    val latest = observeState()

    viewModel.onDelayMinutesClick()
    viewModel.onDelayMinutesOverrideToggle(true)
    viewModel.onDelayMinutesPreviewChange(45)
    viewModel.onDelayMinutesConfirm()

    assertEquals(45, latest().notification.delayMinutes)
  }

  @Test
  fun `onDelayMinutesConfirm leaves delayMinutes null when not overridden`() {
    val latest = observeState()

    viewModel.onDelayMinutesClick()
    viewModel.onDelayMinutesConfirm()

    assertNull(latest().notification.delayMinutes)
  }
}
