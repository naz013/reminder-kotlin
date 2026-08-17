package com.github.naz013.feature.reminder.todo

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelStore
import com.github.naz013.testing.BaseTest
import com.github.naz013.ui.common.R
import com.github.naz013.ui.group.UiGroupList
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.GroupBuilderItem
import com.github.naz013.feature.reminder.build.SubTasksBuilderItem
import com.github.naz013.feature.reminder.build.SummaryBuilderItem
import com.github.naz013.feature.reminder.build.bi.BiFactory
import com.github.naz013.ui.reminder.ShopItemsFormatter
import com.github.naz013.feature.reminder.build.reminder.BiToReminderAdapter
import com.github.naz013.feature.reminder.build.reminder.ReminderToBiDecomposer
import com.github.naz013.logic.reminder.usecase.ResumeReminderUseCase
import com.github.naz013.logic.reminder.usecase.MoveReminderToArchiveUseCase
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.ShopItem
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.logic.reminder.usecase.DeleteReminderUseCase
import com.github.naz013.logic.reminder.usecase.PauseReminderUseCase
import com.github.naz013.logic.tag.ToggleTagAssignmentUseCase
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.tag.TagChipState
import com.github.naz013.ui.tag.TagChipStateAdapter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class TodoEditViewModelTest : BaseTest() {
  private val biFactory = mockk<BiFactory>()
  private val biToReminderAdapter = mockk<BiToReminderAdapter>()
  private val activateReminderUseCase = mockk<ActivateReminderUseCase>(relaxed = true)
  private val tagRepository = mockk<TagRepository>()
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>()
  private val toggleTagAssignmentUseCase = mockk<ToggleTagAssignmentUseCase>(relaxed = true)
  private val tagChipStateAdapter = mockk<TagChipStateAdapter>()
  private val dateTimeManager = mockk<DateTimeManager>(relaxed = true)
  private val todoSeedHolder = TodoSeedHolder()
  private val shopItemsFormatter = mockk<ShopItemsFormatter>(relaxed = true)
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val reminderToBiDecomposer = mockk<ReminderToBiDecomposer>()
  private val pauseReminderUseCase = mockk<PauseReminderUseCase>(relaxed = true)
  private val resumeReminderUseCase = mockk<ResumeReminderUseCase>(relaxed = true)
  private val deleteReminderUseCase = mockk<DeleteReminderUseCase>(relaxed = true)
  private val moveReminderToArchiveUseCase = mockk<MoveReminderToArchiveUseCase>(relaxed = true)

  @Before
  override fun setUp() {
    super.setUp()
    every { tagRepository.observeAll() } returns flowOf(emptyList())
    every { tagAssignmentRepository.observeTagsForItem(any(), any()) } returns flowOf(emptyList())
    coEvery { biFactory.create(BiType.SUB_TASKS) } returns subTasksItem()
    coEvery { biFactory.create(BiType.GROUP) } returns groupItem()
    // No existing reminder by default - matches create-mode for every test that doesn't
    // explicitly stub a hit, including ones that pass a non-empty id only as a stable label.
    coEvery { reminderV2Repository.getById(any()) } returns null
  }

  private fun subTasksItem() =
    SubTasksBuilderItem(
      title = "items",
      description = null,
      shopItemsFormatter = shopItemsFormatter,
      dateTimeManager = dateTimeManager,
    )

  private fun subTasksItemWithData() =
    subTasksItem().apply { modifier.update(listOf(ShopItem(summary = "Milk", createTime = ""))) }

  private fun summaryItem(title: String = "") =
    SummaryBuilderItem(title = "", description = null).apply { modifier.update(title) }

  private fun groupItem() =
    GroupBuilderItem(title = "group", description = null, groups = listOf(uiGroupFixture()), defaultGroup = null)

  private fun uiGroupFixture(id: String = "g1", title: String = "Personal") =
    UiGroupList(
      id = id,
      title = title,
      color = 0,
      colorPosition = 0,
      contrastColor = 0,
      isDefaultGroup = false,
      canDelete = true,
      canSetAsDefault = true,
    )

  private fun reminderV2Fixture(uuId: String = "todo-1") =
    ReminderV2(uuId = uuId, schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))

  private fun createViewModel(id: String = ""): TodoEditViewModel =
    TodoEditViewModel(
      navKey = TodoEditNavKey.Main(id = id),
      dispatcherProvider = mockDispatcherProvider(),
      biFactory = biFactory,
      biToReminderAdapter = biToReminderAdapter,
      activateReminderUseCase = activateReminderUseCase,
      tagRepository = tagRepository,
      tagAssignmentRepository = tagAssignmentRepository,
      toggleTagAssignmentUseCase = toggleTagAssignmentUseCase,
      tagChipStateAdapter = tagChipStateAdapter,
      dateTimeManager = dateTimeManager,
      todoSeedHolder = todoSeedHolder,
      reminderV2Repository = reminderV2Repository,
      reminderToBiDecomposer = reminderToBiDecomposer,
      pauseReminderUseCase = pauseReminderUseCase,
      resumeReminderUseCase = resumeReminderUseCase,
      deleteReminderUseCase = deleteReminderUseCase,
      moveReminderToArchiveUseCase = moveReminderToArchiveUseCase,
    )

  @Test
  fun `init obtains a sub-tasks item and the available group list, defaulting to no group`() {
    val viewModel = createViewModel()

    assertNotNull(viewModel.state.value.subTasksItem)
    assertEquals(listOf(uiGroupFixture()), viewModel.state.value.availableGroups)
    assertEquals(null, viewModel.state.value.selectedGroup)
    assertEquals(false, viewModel.state.value.isEditing)
  }

  @Test
  fun `init with an unknown id falls back to create-mode instead of leaving subTasksItem null`() {
    coEvery { reminderV2Repository.getById("missing") } returns null

    val viewModel = createViewModel(id = "missing")

    assertEquals(false, viewModel.state.value.isEditing)
    assertNotNull(viewModel.state.value.subTasksItem)
    coVerify(exactly = 0) { pauseReminderUseCase(any()) }
  }

  @Test
  fun `init with an id loads the reminder, pauses it, and seeds title, subTasks and group`() {
    val reminder = reminderV2Fixture(uuId = "42")
    coEvery { reminderV2Repository.getById("42") } returns reminder
    val loadedSubTasks = subTasksItemWithData()
    val loadedGroup = groupItem().apply { modifier.update(uiGroupFixture()) }
    coEvery { reminderToBiDecomposer(reminder) } returns listOf(summaryItem("Groceries"), loadedSubTasks, loadedGroup)

    val viewModel = createViewModel(id = "42")

    assertEquals("Groceries", viewModel.state.value.title)
    assertEquals(loadedSubTasks, viewModel.state.value.subTasksItem)
    assertEquals(uiGroupFixture(), viewModel.state.value.selectedGroup)
    assertEquals(true, viewModel.state.value.isEditing)
    coVerify(exactly = 1) { pauseReminderUseCase(match { it.uuId == "42" }) }
  }

  @Test
  fun `init with an id but no group in the decomposed items still populates availableGroups`() {
    val reminder = reminderV2Fixture(uuId = "42")
    coEvery { reminderV2Repository.getById("42") } returns reminder
    coEvery { reminderToBiDecomposer(reminder) } returns listOf(summaryItem("Groceries"), subTasksItemWithData())

    val viewModel = createViewModel(id = "42")

    assertEquals(listOf(uiGroupFixture()), viewModel.state.value.availableGroups)
    assertEquals(null, viewModel.state.value.selectedGroup)
  }

  @Test
  fun `init with an id sets canSave true when the loaded sub-tasks item is already correct`() {
    val reminder = reminderV2Fixture(uuId = "42")
    coEvery { reminderV2Repository.getById("42") } returns reminder
    coEvery { reminderToBiDecomposer(reminder) } returns listOf(subTasksItemWithData())

    val viewModel = createViewModel(id = "42")

    assertEquals(true, viewModel.state.value.canSave)
  }

  @Test
  fun `init with an id carries forward extra builder items untouched on save`() {
    val reminder = reminderV2Fixture(uuId = "42")
    coEvery { reminderV2Repository.getById("42") } returns reminder
    // Use a builder item type this screen never shows (not SUMMARY/SUB_TASKS/GROUP) to prove it
    // round-trips untouched - PRIORITY stands in for any such "extra" field.
    val priorityItem = mockk<BuilderItem<Any>>(relaxed = true)
    every { priorityItem.biType } returns BiType.PRIORITY
    coEvery { reminderToBiDecomposer(reminder) } returns listOf(subTasksItemWithData(), priorityItem)
    val itemsSlot = slot<List<BuilderItem<*>>>()
    every { biToReminderAdapter(any(), capture(itemsSlot), any()) } returns
      BiToReminderAdapter.BuildResult.Success(reminder)
    val viewModel = createViewModel(id = "42")

    viewModel.onSaveClick()

    assertEquals(true, itemsSlot.captured.contains(priorityItem))
  }

  @Test
  fun `onSaveClick when editing rebuilds from originalV2 and marks isEdited true`() {
    val reminder = reminderV2Fixture(uuId = "42")
    coEvery { reminderV2Repository.getById("42") } returns reminder
    coEvery { reminderToBiDecomposer(reminder) } returns listOf(subTasksItemWithData())
    val baseSlot = slot<ReminderV2>()
    val isEditedSlot = slot<Boolean>()
    every { biToReminderAdapter(capture(baseSlot), any(), capture(isEditedSlot)) } returns
      BiToReminderAdapter.BuildResult.Success(reminder)
    val viewModel = createViewModel(id = "42")

    viewModel.onSaveClick()

    assertEquals(reminder, baseSlot.captured)
    assertEquals(true, isEditedSlot.captured)
  }

  @Test
  fun `onSaveClick with no group selected omits a group item entirely`() {
    val viewModel = createViewModel()
    val itemsSlot = slot<List<BuilderItem<*>>>()
    every { biToReminderAdapter(any(), capture(itemsSlot), any()) } returns
      BiToReminderAdapter.BuildResult.Success(reminderV2Fixture())

    viewModel.onSaveClick()

    assertEquals(0, itemsSlot.captured.filterIsInstance<GroupBuilderItem>().size)
  }

  @Test
  fun `onSaveClick with a group selected includes a group item carrying that value`() {
    val viewModel = createViewModel()
    val group = uiGroupFixture()
    viewModel.onGroupSelected(group)
    val itemsSlot = slot<List<BuilderItem<*>>>()
    every { biToReminderAdapter(any(), capture(itemsSlot), any()) } returns
      BiToReminderAdapter.BuildResult.Success(reminderV2Fixture())

    viewModel.onSaveClick()

    val groupItem = itemsSlot.captured.filterIsInstance<GroupBuilderItem>().single()
    assertEquals(group, groupItem.modifier.getValue())
  }

  @Test
  fun `onSaveClick with a successful build activates the reminder and moves back`() {
    val viewModel = createViewModel()
    val reminder = reminderV2Fixture()
    every { biToReminderAdapter(any(), any(), any()) } returns
      BiToReminderAdapter.BuildResult.Success(reminder)

    viewModel.onSaveClick()

    coVerify(exactly = 1) { activateReminderUseCase(reminder, startAnyway = true) }
    assertEquals(TodoEditViewModel.ViewModelEvent.MoveBack, viewModel.event.value?.peekContent())
  }

  @Test
  fun `onSaveClick builds a summary item whose value is the entered title, not its label`() {
    val viewModel = createViewModel()
    viewModel.onTitleChange("Groceries")
    val itemsSlot = slot<List<BuilderItem<*>>>()
    every { biToReminderAdapter(any(), capture(itemsSlot), any()) } returns
      BiToReminderAdapter.BuildResult.Success(reminderV2Fixture())

    viewModel.onSaveClick()

    val summaryItem = itemsSlot.captured.filterIsInstance<SummaryBuilderItem>().single()
    assertEquals("Groceries", summaryItem.modifier.getValue())
  }

  @Test
  fun `onSaveClick with a failed build shows a message and does not activate anything`() {
    val viewModel = createViewModel()
    every { biToReminderAdapter(any(), any(), any()) } returns
      BiToReminderAdapter.BuildResult.Error("no items")

    viewModel.onSaveClick()

    coVerify(exactly = 0) { activateReminderUseCase(any(), any(), any()) }
    assertEquals(
      TodoEditViewModel.ViewModelEvent.ShowMessage(R.string.builder_error_create_reminder),
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onExtendClick with a successful build stores the seed and emits OpenBuilder`() {
    val viewModel = createViewModel(id = "todo-1")
    val reminder = reminderV2Fixture(uuId = "todo-1")
    every { biToReminderAdapter(any(), any(), any()) } returns
      BiToReminderAdapter.BuildResult.Success(reminder)

    viewModel.onExtendClick()

    assertEquals(reminder, todoSeedHolder.pendingSeed)
    assertEquals(
      TodoEditViewModel.ViewModelEvent.OpenBuilder("todo-1", isEditing = false),
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onExtendClick while editing an existing reminder passes isEditing through OpenBuilder`() {
    val reminder = reminderV2Fixture(uuId = "42")
    coEvery { reminderV2Repository.getById("42") } returns reminder
    coEvery { reminderToBiDecomposer(reminder) } returns listOf(subTasksItemWithData())
    every { biToReminderAdapter(any(), any(), any()) } returns
      BiToReminderAdapter.BuildResult.Success(reminder)
    val viewModel = createViewModel(id = "42")

    viewModel.onExtendClick()

    assertEquals(
      TodoEditViewModel.ViewModelEvent.OpenBuilder("42", isEditing = true),
      viewModel.event.value?.peekContent(),
    )
  }

  @Test
  fun `onExtendClick with a failed build does not store a seed`() {
    val viewModel = createViewModel(id = "todo-1")
    every { biToReminderAdapter(any(), any(), any()) } returns
      BiToReminderAdapter.BuildResult.Error("no items")

    viewModel.onExtendClick()

    assertEquals(null, todoSeedHolder.pendingSeed)
  }

  @Test
  fun `moveToTrash posts MoveBack immediately when there is no original reminder`() {
    val viewModel = createViewModel()

    viewModel.moveToTrash()

    assertEquals(TodoEditViewModel.ViewModelEvent.MoveBack, viewModel.event.value?.peekContent())
    coVerify(exactly = 0) { moveReminderToArchiveUseCase(any()) }
  }

  @Test
  fun `moveToTrash archives the original reminder when one was loaded for edit`() {
    val reminder = reminderV2Fixture(uuId = "42")
    coEvery { reminderV2Repository.getById("42") } returns reminder
    coEvery { reminderToBiDecomposer(reminder) } returns listOf(subTasksItemWithData())
    val viewModel = createViewModel(id = "42")

    viewModel.moveToTrash()

    coVerify(exactly = 1) { moveReminderToArchiveUseCase("42") }
    assertEquals(TodoEditViewModel.ViewModelEvent.MoveBack, viewModel.event.value?.peekContent())
  }

  @Test
  fun `deleteReminder posts MoveBack immediately when there is no original reminder`() {
    val viewModel = createViewModel()

    viewModel.deleteReminder(showMessage = true)

    assertEquals(TodoEditViewModel.ViewModelEvent.MoveBack, viewModel.event.value?.peekContent())
    coVerify(exactly = 0) { deleteReminderUseCase(any()) }
  }

  @Test
  fun `deleteReminder deletes and posts MoveBack when showMessage is true`() {
    val reminder = reminderV2Fixture(uuId = "42")
    coEvery { reminderV2Repository.getById("42") } returns reminder
    coEvery { reminderToBiDecomposer(reminder) } returns listOf(subTasksItemWithData())
    val viewModel = createViewModel(id = "42")

    viewModel.deleteReminder(showMessage = true)

    coVerify(exactly = 1) { deleteReminderUseCase(reminder) }
    assertEquals(TodoEditViewModel.ViewModelEvent.MoveBack, viewModel.event.value?.peekContent())
  }

  @Test
  fun `deleteReminder does not emit an event when showMessage is false`() {
    val reminder = reminderV2Fixture(uuId = "42")
    coEvery { reminderV2Repository.getById("42") } returns reminder
    coEvery { reminderToBiDecomposer(reminder) } returns listOf(subTasksItemWithData())
    val viewModel = createViewModel(id = "42")

    viewModel.deleteReminder(showMessage = false)

    coVerify(exactly = 1) { deleteReminderUseCase(reminder) }
    assertNull(viewModel.event.value?.peekContent())
  }

  @Test
  fun `onCleared resumes the reminder when it was paused and not saving`() {
    val reminder = reminderV2Fixture(uuId = "42")
    coEvery { reminderV2Repository.getById("42") } returns reminder
    coEvery { reminderToBiDecomposer(reminder) } returns listOf(subTasksItemWithData())
    val viewModel = createViewModel(id = "42")
    val store = ViewModelStore()
    store.put("todo", viewModel)

    store.clear()

    coVerify(exactly = 1) { resumeReminderUseCase(match { it.uuId == "42" }) }
  }

  @Test
  fun `onCleared does not resume the reminder while a save is in flight`() {
    val reminder = reminderV2Fixture(uuId = "42")
    coEvery { reminderV2Repository.getById("42") } returns reminder
    coEvery { reminderToBiDecomposer(reminder) } returns listOf(subTasksItemWithData())
    every { biToReminderAdapter(any(), any(), any()) } returns
      BiToReminderAdapter.BuildResult.Success(reminder)
    val viewModel = createViewModel(id = "42")
    viewModel.onSaveClick()
    val store = ViewModelStore()
    store.put("todo", viewModel)

    store.clear()

    coVerify(exactly = 0) { resumeReminderUseCase(any()) }
  }

  @Test
  fun `onCleared does not resume a brand-new todo that was never paused`() {
    val viewModel = createViewModel()
    val store = ViewModelStore()
    store.put("todo", viewModel)

    store.clear()

    coVerify(exactly = 0) { resumeReminderUseCase(any()) }
  }

  @Test
  fun `onTagToggle attaches the tag immediately, independent of save`() {
    val viewModel = createViewModel(id = "todo-1")
    val tag = TagChipState(id = "tag-1", name = "Work", color = Color.Red)

    viewModel.onTagToggle(tag)

    coVerify(exactly = 1) {
      toggleTagAssignmentUseCase("todo-1", TaggedItemType.REMINDER, "tag-1", false)
    }
  }
}
