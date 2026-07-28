package com.elementary.tasks.groups.create

import android.content.Context
import com.elementary.tasks.BaseTest
import com.elementary.tasks.groups.usecase.DeleteGroupUseCase
import com.elementary.tasks.groups.usecase.SaveGroupUseCase
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.GroupV2Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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

  private lateinit var viewModel: EditGroupViewModel

  private fun groupV2(
    id: String = "1",
    title: String = "Work",
    isDefault: Boolean = false,
  ) = GroupV2(
    uuId = id,
    title = title,
    color = 0,
    isDefault = isDefault,
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
  )

  @Before
  override fun setUp() {
    super.setUp()
    every { contextProvider.themedContext } returns mockk<Context>(relaxed = true)
    every { dateTimeManager.getCurrentDateTime() } returns org.threeten.bp.LocalDateTime.now()
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
  fun `reloads group data on each fresh state collection`() =
    runTest {
      viewModel.state.first()
      viewModel.state.first()

      coVerify(exactly = 2) { groupV2Repository.getById("1") }
    }

  // `state` re-runs load() in onStart on every fresh collection (matching the confetti test in
  // PreviewBirthdayViewModelTest), which would silently overwrite an in-memory edit to a field
  // load() also sets (title/colorPosition/isDefault) if we called `.first()` again to inspect it.
  // Observe through one persistent subscription instead so the mutation isn't clobbered by a reload.
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
}
