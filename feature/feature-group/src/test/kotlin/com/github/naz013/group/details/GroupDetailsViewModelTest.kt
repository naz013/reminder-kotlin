package com.github.naz013.group.details

import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logic.group.DeleteGroupUseCase
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.common.text.UiTextElement
import com.github.naz013.ui.common.text.UiTextFormat
import com.github.naz013.ui.group.UiGroupList
import com.github.naz013.ui.group.UiGroupListAdapter
import com.github.naz013.ui.notification.settings.NotificationOverrideSubtitleFormatter
import com.github.naz013.ui.notification.settings.NotificationOverrideSubtitles
import com.github.naz013.ui.reminder.UiReminderList
import com.github.naz013.ui.reminder.UiReminderListActions
import com.github.naz013.ui.reminder.UiReminderListAdapter
import com.github.naz013.ui.reminder.UiReminderListState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class GroupDetailsViewModelTest : BaseTest() {
  private val groupV2Repository = mockk<GroupV2Repository>()
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val uiReminderListAdapter = mockk<UiReminderListAdapter>()
  private val uiGroupListAdapter = mockk<UiGroupListAdapter>()
  private val deleteGroupUseCase = mockk<DeleteGroupUseCase>(relaxed = true)
  private val notificationOverrideSubtitleFormatter = mockk<NotificationOverrideSubtitleFormatter>()

  private lateinit var viewModel: GroupDetailsViewModel

  private fun groupV2(
    id: String = "1",
    title: String = "Work",
    color: Int = 5,
    isDefault: Boolean = false,
    notification: NotificationSettingsOverride = NotificationSettingsOverride(),
  ) = GroupV2(
    uuId = id,
    title = title,
    color = color,
    isDefault = isDefault,
    notification = notification,
    syncState = SyncState.Synced,
  )

  private fun reminderV2(id: String) = ReminderV2(
    uuId = id,
    groupId = "1",
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 1, 1, 9, 0)),
  )

  private fun uiGroupList(color: Int) = UiGroupList(
    id = "1",
    title = "Work",
    color = color,
    colorPosition = 5,
    contrastColor = 0,
    isDefaultGroup = false,
    canDelete = true,
    canSetAsDefault = true,
  )

  private fun uiReminderList(id: String) = UiReminderList(
    id = id,
    noteId = null,
    dueDateTime = null,
    mainText = UiTextElement(text = "Reminder $id", textFormat = UiTextFormat(fontSize = 16f)),
    secondaryText = null,
    tertiaryText = null,
    tags = emptyList(),
    actions = UiReminderListActions(),
    state = UiReminderListState(isActive = true),
  )

  private fun buildViewModel(id: String = "1") = GroupDetailsViewModel(
    id = id,
    dispatcherProvider = mockDispatcherProvider(),
    groupV2Repository = groupV2Repository,
    reminderV2Repository = reminderV2Repository,
    uiReminderListAdapter = uiReminderListAdapter,
    uiGroupListAdapter = uiGroupListAdapter,
    deleteGroupUseCase = deleteGroupUseCase,
    notificationOverrideSubtitleFormatter = notificationOverrideSubtitleFormatter,
  )

  @Before
  override fun setUp() {
    super.setUp()
    every { groupV2Repository.observeById("1") } returns flowOf(groupV2(id = "1"))
    coEvery { groupV2Repository.countAll() } returns 2
    every { reminderV2Repository.observeActiveByGroupId("1") } returns flowOf(emptyList())
    every { notificationOverrideSubtitleFormatter.format(any(), any()) } returns NotificationOverrideSubtitles()
    every { uiGroupListAdapter.convert(any<GroupV2>()) } answers {
      uiGroupList(color = firstArg<GroupV2>().color * 100)
    }

    viewModel = buildViewModel()
  }

  @Test
  fun `load populates title, color, subtitles and reminders`() = runTest {
    val reminder = reminderV2("r1")
    val uiReminder = uiReminderList("r1")
    every { reminderV2Repository.observeActiveByGroupId("1") } returns flowOf(listOf(reminder))
    every { uiReminderListAdapter.createV2(reminder, any()) } returns uiReminder
    every { notificationOverrideSubtitleFormatter.format(any(), any()) } returns
      NotificationOverrideSubtitles(priority = "High")
    val vm = buildViewModel()

    val state = vm.state.first()

    assertEquals(false, state.isLoading)
    assertEquals("Work", state.title)
    assertEquals(500, state.color)
    assertEquals("High", state.notificationSubtitles.priority)
    assertEquals(listOf(uiReminder), state.reminders)
  }

  @Test
  fun `canDelete is false when the group is the only one`() = runTest {
    coEvery { groupV2Repository.countAll() } returns 1
    val vm = buildViewModel()

    val state = vm.state.first()

    assertEquals(false, state.canDelete)
  }

  @Test
  fun `canDelete is false when the group is the default group`() = runTest {
    every { groupV2Repository.observeById("1") } returns flowOf(groupV2(id = "1", isDefault = true))
    val vm = buildViewModel()

    val state = vm.state.first()

    assertEquals(false, state.canDelete)
  }

  @Test
  fun `state updates reactively when the group Flow emits a new value`() = runTest {
    val groupFlow = MutableStateFlow(groupV2(id = "1", title = "Work"))
    every { groupV2Repository.observeById("1") } returns groupFlow
    val vm = buildViewModel()
    assertEquals("Work", vm.state.first().title)

    groupFlow.value = groupV2(id = "1", title = "Personal")

    assertEquals("Personal", vm.state.first().title)
  }

  @Test
  fun `onEditClick emits OpenEdit for this group`() = runTest {
    viewModel.state.first()

    viewModel.onEditClick()

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(GroupDetailsViewModel.NavigationEvent.OpenEdit("1"), event)
  }

  @Test
  fun `onDeleteClick emits ConfirmDelete for this group`() = runTest {
    viewModel.state.first()

    viewModel.onDeleteClick()

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(GroupDetailsViewModel.NavigationEvent.ConfirmDelete("1"), event)
  }

  @Test
  fun `onDeleteConfirmed does nothing when group cannot be deleted`() = runTest {
    coEvery { groupV2Repository.countAll() } returns 1
    val vm = buildViewModel()
    vm.state.first()

    vm.onDeleteConfirmed()

    coVerify(exactly = 0) { deleteGroupUseCase(any()) }
  }

  @Test
  fun `onDeleteConfirmed deletes the group and emits Deleted when allowed`() = runTest {
    viewModel.state.first()

    viewModel.onDeleteConfirmed()

    coVerify(exactly = 1) { deleteGroupUseCase("1") }
    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(GroupDetailsViewModel.NavigationEvent.Deleted, event)
  }

  @Test
  fun `onReminderClick emits OpenReminderPreview for the clicked reminder`() = runTest {
    viewModel.state.first()

    viewModel.onReminderClick("r1")

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(GroupDetailsViewModel.NavigationEvent.OpenReminderPreview("r1"), event)
  }
}
