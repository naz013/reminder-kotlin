package com.github.naz013.tags.details

import androidx.compose.ui.graphics.Color
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.Tag
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.birthday.UiBirthdayList
import com.github.naz013.ui.birthday.UiBirthdayListAdapter
import com.github.naz013.ui.common.text.UiTextElement
import com.github.naz013.ui.common.text.UiTextFormat
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.googletask.GoogleTaskItemState
import com.github.naz013.ui.googletask.GoogleTaskItemStateAdapter
import com.github.naz013.ui.note.UiNoteListItem
import com.github.naz013.ui.note.UiNoteListItemAdapter
import com.github.naz013.ui.reminder.UiReminderList
import com.github.naz013.ui.reminder.UiReminderListActions
import com.github.naz013.ui.reminder.UiReminderListAdapter
import com.github.naz013.ui.reminder.UiReminderListState
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class TagDetailsViewModelTest : BaseTest() {
  private val tagRepository = mockk<TagRepository>()
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>()
  private val groupV2Repository = mockk<GroupV2Repository>()
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val uiReminderListAdapter = mockk<UiReminderListAdapter>()
  private val noteRepository = mockk<NoteRepository>()
  private val uiNoteListItemAdapter = mockk<UiNoteListItemAdapter>()
  private val birthdayRepository = mockk<BirthdayRepository>()
  private val uiBirthdayListAdapter = mockk<UiBirthdayListAdapter>()
  private val googleTaskRepository = mockk<GoogleTaskRepository>()
  private val googleTaskListRepository = mockk<GoogleTaskListRepository>()
  private val googleTaskItemStateAdapter = mockk<GoogleTaskItemStateAdapter>()
  private val themeProvider = mockk<ThemeProvider>()

  private lateinit var viewModel: TagDetailsViewModel

  private fun tag(id: String = "1", name: String = "Work", color: Int = 5) =
    Tag(id = id, name = name, color = color)

  private fun reminderV2(uuId: String) = ReminderV2(
    uuId = uuId,
    groupId = "g1",
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 1, 1, 9, 0)),
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

  private fun noteWithImages(key: String) = NoteWithImages(
    note = Note(key = key, content = NoteDocument.fromLegacy(title = "Note $key", summary = "Summary $key"), syncState = SyncState.Synced),
  )

  private fun uiNoteListItem(id: String) = UiNoteListItem(
    id = id,
    content = NoteDocument.fromLegacy(title = "Note $id", summary = "Summary $id"),
    backgroundColor = Color.White,
    textColor = Color.Black,
    fontStyle = 0,
    fontSize = 14f,
    images = emptyList(),
  )

  private fun birthday(uuId: String) = Birthday(
    name = "Person $uuId",
    uuId = uuId,
    syncState = SyncState.Synced,
  )

  private fun uiBirthdayList(uuId: String) = UiBirthdayList(
    uuId = uuId,
    name = "Person $uuId",
    color = 0,
    contrastColor = 0,
    nextBirthdayDate = LocalDateTime.of(2026, 5, 1, 0, 0),
  )

  private fun googleTask(taskId: String) = GoogleTask(taskId = taskId, listId = "l1", title = "Task $taskId")

  private fun googleTaskItemState(id: String) = GoogleTaskItemState(
    id = id,
    text = "Task $id",
    notes = null,
    dueDate = null,
    isCompleted = false,
    taskListColor = null,
    reminderId = null,
  )

  private fun buildViewModel(id: String = "1") = TagDetailsViewModel(
    id = id,
    dispatcherProvider = mockDispatcherProvider(),
    tagRepository = tagRepository,
    tagAssignmentRepository = tagAssignmentRepository,
    groupV2Repository = groupV2Repository,
    reminderV2Repository = reminderV2Repository,
    uiReminderListAdapter = uiReminderListAdapter,
    noteRepository = noteRepository,
    uiNoteListItemAdapter = uiNoteListItemAdapter,
    birthdayRepository = birthdayRepository,
    uiBirthdayListAdapter = uiBirthdayListAdapter,
    googleTaskRepository = googleTaskRepository,
    googleTaskListRepository = googleTaskListRepository,
    googleTaskItemStateAdapter = googleTaskItemStateAdapter,
    themeProvider = themeProvider,
  )

  @Before
  override fun setUp() {
    super.setUp()
    every { tagRepository.observeById("1") } returns flowOf(tag())
    every { tagAssignmentRepository.observeItemIdsForTag(any(), any()) } returns flowOf(emptyList())
    coEvery { groupV2Repository.getAll() } returns emptyList()
    coEvery { reminderV2Repository.getAll(any(), any()) } returns emptyList()
    coEvery { noteRepository.getByIds(any()) } returns emptyList()
    coEvery { birthdayRepository.getAll() } returns emptyList()
    coEvery { googleTaskListRepository.getAll() } returns emptyList()
    coEvery { googleTaskRepository.getAll() } returns emptyList()
    every { themeProvider.themedColor(any()) } returns Color.Blue
    coEvery { tagAssignmentRepository.detachAllForTag(any()) } just Runs
    coEvery { tagRepository.delete(any()) } just Runs

    viewModel = buildViewModel()
  }

  @Test
  fun `load populates title and color from the tag`() = runTest {
    val state = viewModel.state.first()

    assertEquals(false, state.isLoading)
    assertEquals("Work", state.title)
    assertEquals(Color.Blue, state.color)
  }

  @Test
  fun `load groups tagged items into sections ordered reminders, notes, tasks, birthdays`() = runTest {
    val reminder = reminderV2("r1")
    val uiReminder = uiReminderList("r1")
    val note = noteWithImages("n1")
    val uiNote = uiNoteListItem("n1")
    val task = googleTask("t1")
    val uiTask = googleTaskItemState("t1")
    val bday = birthday("b1")
    val uiBday = uiBirthdayList("b1")

    every { tagAssignmentRepository.observeItemIdsForTag("1", TaggedItemType.REMINDER) } returns flowOf(listOf("r1"))
    every { tagAssignmentRepository.observeItemIdsForTag("1", TaggedItemType.NOTE) } returns flowOf(listOf("n1"))
    every { tagAssignmentRepository.observeItemIdsForTag("1", TaggedItemType.GOOGLE_TASK) } returns flowOf(listOf("t1"))
    every { tagAssignmentRepository.observeItemIdsForTag("1", TaggedItemType.BIRTHDAY) } returns flowOf(listOf("b1"))
    coEvery { reminderV2Repository.getAll(active = true, removed = false) } returns listOf(reminder)
    every { uiReminderListAdapter.createV2(reminder, null) } returns uiReminder
    coEvery { noteRepository.getByIds(listOf("n1")) } returns listOf(note)
    every { uiNoteListItemAdapter.convert(note) } returns uiNote
    coEvery { googleTaskRepository.getAll() } returns listOf(task)
    every { googleTaskItemStateAdapter.convert(task, null) } returns uiTask
    coEvery { birthdayRepository.getAll() } returns listOf(bday)
    every { uiBirthdayListAdapter.convert(bday, any()) } returns uiBday
    val vm = buildViewModel()

    val state = vm.state.first()

    assertEquals(4, state.sections.size)
    assertEquals(
      listOf(TagContentType.REMINDER, TagContentType.NOTE, TagContentType.GOOGLE_TASK, TagContentType.BIRTHDAY),
      state.sections.map { it.type },
    )
    assertEquals(listOf(TagDetailItem.ReminderItem(uiReminder)), state.sections[0].items)
    assertEquals(listOf(TagDetailItem.NoteItem(uiNote)), state.sections[1].items)
    assertEquals(listOf(TagDetailItem.GoogleTaskItem(uiTask)), state.sections[2].items)
    assertEquals(listOf(TagDetailItem.BirthdayItem(uiBday)), state.sections[3].items)
  }

  @Test
  fun `sections update reactively when a reminder is attached to the tag`() = runTest {
    val reminder = reminderV2("r1")
    val uiReminder = uiReminderList("r1")
    coEvery { reminderV2Repository.getAll(active = true, removed = false) } returns listOf(reminder)
    every { uiReminderListAdapter.createV2(reminder, null) } returns uiReminder
    val reminderIdsFlow = MutableStateFlow<List<String>>(emptyList())
    every { tagAssignmentRepository.observeItemIdsForTag("1", TaggedItemType.REMINDER) } returns reminderIdsFlow
    val vm = buildViewModel()
    assertTrue(vm.state.first().sections.isEmpty())

    reminderIdsFlow.value = listOf("r1")

    val state = vm.state.first()
    assertEquals(listOf(TagDetailItem.ReminderItem(uiReminder)), state.sections.single().items)
  }

  @Test
  fun `sections with no tagged items are omitted`() = runTest {
    val state = viewModel.state.first()

    assertTrue(state.sections.isEmpty())
  }

  @Test
  fun `onTypeSelected narrows sections down to just that type`() = runTest {
    val reminder = reminderV2("r1")
    val uiReminder = uiReminderList("r1")
    val bday = birthday("b1")
    val uiBday = uiBirthdayList("b1")
    every { tagAssignmentRepository.observeItemIdsForTag("1", TaggedItemType.REMINDER) } returns flowOf(listOf("r1"))
    every { tagAssignmentRepository.observeItemIdsForTag("1", TaggedItemType.BIRTHDAY) } returns flowOf(listOf("b1"))
    coEvery { reminderV2Repository.getAll(active = true, removed = false) } returns listOf(reminder)
    every { uiReminderListAdapter.createV2(reminder, null) } returns uiReminder
    coEvery { birthdayRepository.getAll() } returns listOf(bday)
    every { uiBirthdayListAdapter.convert(bday, any()) } returns uiBday
    val vm = buildViewModel()
    vm.state.first()

    vm.onTypeSelected(TagContentType.BIRTHDAY)
    val state = vm.state.first()

    assertEquals(listOf(TagContentType.BIRTHDAY), state.sections.map { it.type })
    assertEquals(TagContentType.BIRTHDAY, state.selectedType)
  }

  @Test
  fun `onSearchQueryChange updates the search query in state immediately`() = runTest {
    viewModel.state.first()

    viewModel.onSearchQueryChange("Alpha")

    assertEquals("Alpha", viewModel.state.first().searchQuery)
  }

  @Test
  fun `search query narrows items by matching text`() = runTest {
    val reminder = reminderV2("r1")
    val uiReminder = uiReminderList("r1")
    every { tagAssignmentRepository.observeItemIdsForTag("1", TaggedItemType.REMINDER) } returns flowOf(listOf("r1"))
    coEvery { reminderV2Repository.getAll(active = true, removed = false) } returns listOf(reminder)
    every { uiReminderListAdapter.createV2(reminder, null) } returns uiReminder
    val vm = buildViewModel()
    vm.state.first()

    vm.onSearchQueryChange("does not match anything")
    val state = vm.state.first()

    assertTrue(state.sections.isEmpty())
  }

  @Test
  fun `onEditClick emits OpenEdit for this tag`() = runTest {
    viewModel.state.first()

    viewModel.onEditClick()

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(TagDetailsViewModel.NavigationEvent.OpenEdit("1"), event)
  }

  @Test
  fun `onDeleteClick emits ConfirmDelete`() = runTest {
    viewModel.state.first()

    viewModel.onDeleteClick()

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(TagDetailsViewModel.NavigationEvent.ConfirmDelete, event)
  }

  @Test
  fun `onDeleteConfirmed detaches all assignments, deletes the tag and emits Deleted`() = runTest {
    viewModel.state.first()

    viewModel.onDeleteConfirmed()

    coVerify(exactly = 1) { tagAssignmentRepository.detachAllForTag("1") }
    coVerify(exactly = 1) { tagRepository.delete("1") }
    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(TagDetailsViewModel.NavigationEvent.Deleted, event)
  }

  @Test
  fun `onItemClick emits the matching preview event per item type`() = runTest {
    viewModel.state.first()

    viewModel.onItemClick(TagDetailItem.ReminderItem(uiReminderList("r1")))
    assertEquals(
      TagDetailsViewModel.NavigationEvent.OpenReminderPreview("r1"),
      viewModel.navigationEvent.value?.peekContent(),
    )

    viewModel.onItemClick(TagDetailItem.NoteItem(uiNoteListItem("n1")))
    assertEquals(
      TagDetailsViewModel.NavigationEvent.OpenNotePreview("n1"),
      viewModel.navigationEvent.value?.peekContent(),
    )

    viewModel.onItemClick(TagDetailItem.BirthdayItem(uiBirthdayList("b1")))
    assertEquals(
      TagDetailsViewModel.NavigationEvent.OpenBirthdayPreview("b1"),
      viewModel.navigationEvent.value?.peekContent(),
    )

    viewModel.onItemClick(TagDetailItem.GoogleTaskItem(googleTaskItemState("t1")))
    assertEquals(
      TagDetailsViewModel.NavigationEvent.OpenGoogleTaskPreview("t1"),
      viewModel.navigationEvent.value?.peekContent(),
    )
  }
}
