package com.elementary.tasks.googletasks.list

import android.content.Context
import com.elementary.tasks.BaseTest
import com.elementary.tasks.R
import com.github.naz013.ui.googletask.GoogleTaskItemStateAdapter
import com.github.naz013.ui.googletask.GoogleTaskItemState
import com.elementary.tasks.getOrAwaitValue
import com.github.naz013.feature.googletask.usecase.tasklist.SyncGoogleTaskList
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.feature.googletask.TaskListState
import com.github.naz013.feature.googletask.TaskListViewModel
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
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
import org.junit.Before
import org.junit.Test

class TaskListViewModelTest : BaseTest() {
  private val listId = "list1"

  private val googleTasksApi = mockk<GoogleTasksApi>()
  private val appWidgetUpdater = mockk<AppWidgetUpdater>(relaxed = true)
  private val googleTaskRepository = mockk<GoogleTaskRepository>()
  private val googleTaskListRepository = mockk<GoogleTaskListRepository>()
  private val googleTaskItemStateAdapter = mockk<GoogleTaskItemStateAdapter>()
  private val syncGoogleTaskList = mockk<SyncGoogleTaskList>(relaxed = true)
  private val contextProvider = mockk<ContextProvider>()
  private val textProvider = mockk<TextProvider>(relaxed = true)

  private lateinit var viewModel: TaskListViewModel

  private fun taskList(
    id: String = listId,
    title: String = "Work",
    isDefault: Boolean = false,
  ) = GoogleTaskList(listId = id, title = title, def = if (isDefault) 1 else 0, color = 1)

  private fun task(
    id: String,
    status: String = GoogleTask.TASKS_NEED_ACTION,
  ) = GoogleTask(taskId = id, listId = listId, status = status)

  private fun uiTask(id: String = "t1") =
    GoogleTaskItemState(
      id = id,
      text = "Task $id",
      notes = null,
      dueDate = null,
      isCompleted = false,
      taskListColor = 0,
      reminderId = "",
    )

  @Before
  override fun setUp() {
    super.setUp()
    every { contextProvider.themedContext } returns mockk<Context>(relaxed = true)
    coEvery { googleTaskListRepository.getById(listId) } returns taskList()
    coEvery { googleTaskRepository.getAllByList(listId) } returns emptyList()
    every { googleTaskItemStateAdapter.convert(any(), any()) } returns uiTask()

    viewModel =
      TaskListViewModel(
        listId = listId,
        googleTasksApi = googleTasksApi,
        dispatcherProvider = mockDispatcherProvider(),
        appWidgetUpdater = appWidgetUpdater,
        googleTaskRepository = googleTaskRepository,
        googleTaskListRepository = googleTaskListRepository,
        uiGoogleTaskListAdapter = googleTaskItemStateAdapter,
        syncGoogleTaskList = syncGoogleTaskList,
        contextProvider = contextProvider,
        textProvider = textProvider,
      )
  }

  private fun observeState(): () -> TaskListState {
    var latest = TaskListState()
    CoroutineScope(Dispatchers.Unconfined).launch { viewModel.state.collect { latest = it } }
    return { latest }
  }

  @Test
  fun `loads list details and tasks into state on first collection`() =
    runTest {
      val t1 = task("t1")
      coEvery { googleTaskRepository.getAllByList(listId) } returns listOf(t1)
      every { googleTaskItemStateAdapter.convert(t1, taskList()) } returns uiTask("t1")

      val state = viewModel.state.first()

      assertEquals(listId, state.listId)
      assertEquals("Work", state.title)
      assertEquals(1, state.tasks.size)
    }

  @Test
  fun `does nothing when the task list is not found`() =
    runTest {
      coEvery { googleTaskListRepository.getById(listId) } returns null

      val state = viewModel.state.first()

      assertEquals("", state.listId)
      assertEquals("", state.title)
    }

  @Test
  fun `marks a non-default list as deletable`() =
    runTest {
      coEvery { googleTaskListRepository.getById(listId) } returns taskList(isDefault = false)

      val state = viewModel.state.first()

      assertEquals(true, state.canDelete)
      assertEquals(false, state.isDefaultList)
    }

  @Test
  fun `marks the default list as not deletable`() =
    runTest {
      coEvery { googleTaskListRepository.getById(listId) } returns taskList(isDefault = true)

      val state = viewModel.state.first()

      assertEquals(false, state.canDelete)
      assertEquals(true, state.isDefaultList)
    }

  @Test
  fun `onEditClicked emits EditTaskList with the current listId`() =
    runTest {
      viewModel.state.first()

      viewModel.onEditClicked()

      val event = viewModel.event.getOrAwaitValue()
      assertEquals(TaskListViewModel.TaskListEvent.EditTaskList(listId), event?.getContentIfNotHandled())
    }

  @Test
  fun `sync updates tasks and refreshes the widget after syncing`() =
    runTest {
      viewModel.sync()

      coVerify(exactly = 1) { syncGoogleTaskList(taskList()) }
      verify(exactly = 1) { appWidgetUpdater.updateScheduleWidget() }
    }

  @Test
  fun `sync shows an error and stops syncing when the task list cannot be found`() =
    runTest {
      coEvery { googleTaskListRepository.getById(listId) } returns null
      every { textProvider.getString(R.string.failed_to_update_task) } returns "Failed"

      viewModel.sync()

      val event = viewModel.event.getOrAwaitValue()
      assertEquals(TaskListViewModel.TaskListEvent.ShowError("Failed"), event?.getContentIfNotHandled())
    }

  @Test
  fun `clearList deletes completed tasks locally and remotely then refreshes the widget`() =
    runTest {
      viewModel.state.first()
      val completed = task("c1", status = GoogleTask.TASKS_COMPLETE)
      coEvery { googleTaskRepository.getAllByList(listId, GoogleTask.TASKS_COMPLETE) } returns listOf(completed)
      coEvery { googleTaskRepository.deleteAll(listOf("c1")) } returns Unit
      coEvery { googleTasksApi.clearTaskList(listId) } returns true

      viewModel.clearList()

      coVerify(exactly = 1) { googleTaskRepository.deleteAll(listOf("c1")) }
      coVerify(exactly = 1) { googleTasksApi.clearTaskList(listId) }
      verify(exactly = 1) { appWidgetUpdater.updateScheduleWidget() }
    }

  @Test
  fun `onDeleteListClick shows the delete confirmation dialog`() {
    val latest = observeState()

    viewModel.onDeleteListClick()

    assertEquals(true, latest().showDeleteConfirm)
  }

  @Test
  fun `onDeleteDismiss hides the delete confirmation dialog`() {
    val latest = observeState()
    viewModel.onDeleteListClick()

    viewModel.onDeleteDismiss()

    assertEquals(false, latest().showDeleteConfirm)
  }

  @Test
  fun `deleteGoogleTaskList deletes the list and navigates back`() =
    runTest {
      viewModel.state.first()
      coEvery { googleTasksApi.deleteTaskList(listId) } returns true
      coEvery { googleTaskListRepository.delete(listId) } returns Unit
      coEvery { googleTaskRepository.deleteAll(listId) } returns Unit

      viewModel.deleteGoogleTaskList()

      coVerify(exactly = 1) { googleTaskListRepository.delete(listId) }
      coVerify(exactly = 1) { googleTaskRepository.deleteAll(listId) }
      val event = viewModel.event.getOrAwaitValue()
      assertEquals(TaskListViewModel.TaskListEvent.MoveBack, event?.getContentIfNotHandled())
    }

  @Test
  fun `deleteGoogleTaskList promotes another list to default when deleting the default list`() =
    runTest {
      coEvery { googleTaskListRepository.getById(listId) } returns taskList(isDefault = true)
      viewModel.state.first()
      val otherList = taskList(id = "other", isDefault = false)
      coEvery { googleTasksApi.deleteTaskList(listId) } returns true
      coEvery { googleTaskListRepository.delete(listId) } returns Unit
      coEvery { googleTaskRepository.deleteAll(listId) } returns Unit
      coEvery { googleTaskListRepository.getAll() } returns listOf(otherList)
      coEvery { googleTaskListRepository.save(any()) } returns Unit

      viewModel.deleteGoogleTaskList()

      coVerify(exactly = 1) { googleTaskListRepository.save(match { it.listId == "other" && it.def == 1 }) }
    }

  @Test
  fun `deleteGoogleTaskList shows an error when the api call fails`() =
    runTest {
      viewModel.state.first()
      coEvery { googleTasksApi.deleteTaskList(listId) } returns false
      every { textProvider.getString(R.string.failed_to_update_task) } returns "Failed"

      viewModel.deleteGoogleTaskList()

      val event = viewModel.event.getOrAwaitValue()
      assertEquals(TaskListViewModel.TaskListEvent.ShowError("Failed"), event?.getContentIfNotHandled())
    }

  @Test
  fun `toggleTask shows an error when the task is not found`() =
    runTest {
      coEvery { googleTaskRepository.getById("missing") } returns null
      every { textProvider.getString(R.string.failed_to_update_task) } returns "Failed"

      viewModel.toggleTask("missing")

      val event = viewModel.event.getOrAwaitValue()
      assertEquals(TaskListViewModel.TaskListEvent.ShowError("Failed"), event?.getContentIfNotHandled())
    }

  @Test
  fun `toggleTask marks a pending task complete and refreshes the widget`() =
    runTest {
      val existing = task("t1", status = GoogleTask.TASKS_NEED_ACTION)
      val updated = existing.copy(status = GoogleTask.TASKS_COMPLETE)
      coEvery { googleTaskRepository.getById("t1") } returns existing
      coEvery { googleTasksApi.updateTaskStatus(GoogleTask.TASKS_COMPLETE, existing) } returns updated
      coEvery { googleTaskRepository.save(updated) } returns Unit

      viewModel.toggleTask("t1")

      coVerify(exactly = 1) { googleTaskRepository.save(updated) }
      verify(exactly = 1) { appWidgetUpdater.updateScheduleWidget() }
    }

  @Test
  fun `toggleTask shows an error and does not refresh the widget when the api call fails`() =
    runTest {
      val existing = task("t1", status = GoogleTask.TASKS_NEED_ACTION)
      coEvery { googleTaskRepository.getById("t1") } returns existing
      coEvery { googleTasksApi.updateTaskStatus(GoogleTask.TASKS_COMPLETE, existing) } returns null
      every { textProvider.getString(R.string.failed_to_update_task) } returns "Failed"

      viewModel.toggleTask("t1")

      verify(exactly = 0) { appWidgetUpdater.updateScheduleWidget() }
      val event = viewModel.event.getOrAwaitValue()
      assertEquals(TaskListViewModel.TaskListEvent.ShowError("Failed"), event?.getContentIfNotHandled())
    }

  @Test
  fun `toggleTask shows an error when the api call throws`() =
    runTest {
      val existing = task("t1", status = GoogleTask.TASKS_NEED_ACTION)
      coEvery { googleTaskRepository.getById("t1") } returns existing
      coEvery { googleTasksApi.updateTaskStatus(any(), any()) } throws RuntimeException("boom")
      every { textProvider.getString(R.string.failed_to_update_task) } returns "Failed"

      viewModel.toggleTask("t1")

      verify(exactly = 0) { appWidgetUpdater.updateScheduleWidget() }
      val event = viewModel.event.getOrAwaitValue()
      assertEquals(TaskListViewModel.TaskListEvent.ShowError("Failed"), event?.getContentIfNotHandled())
    }
}
