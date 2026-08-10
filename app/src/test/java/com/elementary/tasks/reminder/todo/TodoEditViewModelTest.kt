package com.elementary.tasks.reminder.todo

import androidx.compose.ui.graphics.Color
import com.elementary.tasks.BaseTest
import com.elementary.tasks.R
import com.elementary.tasks.mockDispatcherProvider
import com.elementary.tasks.reminder.build.GroupBuilderItem
import com.elementary.tasks.reminder.build.SubTasksBuilderItem
import com.elementary.tasks.reminder.build.bi.BiFactory
import com.elementary.tasks.reminder.build.formatter.`object`.ShopItemsFormatter
import com.elementary.tasks.reminder.build.reminder.BiToReminderAdapter
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.logic.tag.ToggleTagAssignmentUseCase
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.tag.TagChipState
import com.github.naz013.ui.tag.TagChipStateAdapter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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

  @Before
  override fun setUp() {
    super.setUp()
    every { tagRepository.observeAll() } returns flowOf(emptyList())
    every { tagAssignmentRepository.observeTagsForItem(any(), any()) } returns flowOf(emptyList())
    coEvery { biFactory.create(BiType.SUB_TASKS) } returns subTasksItem()
    coEvery { biFactory.create(BiType.GROUP) } returns groupItem()
  }

  private fun subTasksItem() =
    SubTasksBuilderItem(
      title = "items",
      description = null,
      shopItemsFormatter = shopItemsFormatter,
      dateTimeManager = dateTimeManager,
    )

  private fun groupItem() =
    GroupBuilderItem(title = "group", description = null, groups = emptyList(), defaultGroup = null)

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
    )

  @Test
  fun `init obtains a sub-tasks and a group builder item`() {
    val viewModel = createViewModel()

    assertNotNull(viewModel.state.value.subTasksItem)
    assertNotNull(viewModel.state.value.groupItem)
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
      TodoEditViewModel.ViewModelEvent.OpenBuilder("todo-1"),
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
  fun `onTagToggle attaches the tag immediately, independent of save`() {
    val viewModel = createViewModel(id = "todo-1")
    val tag = TagChipState(id = "tag-1", name = "Work", color = Color.Red)

    viewModel.onTagToggle(tag)

    coVerify(exactly = 1) {
      toggleTagAssignmentUseCase("todo-1", TaggedItemType.REMINDER, "tag-1", false)
    }
  }
}
