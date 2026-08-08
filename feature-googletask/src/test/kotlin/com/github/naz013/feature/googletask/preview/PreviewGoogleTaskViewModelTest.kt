package com.github.naz013.feature.googletask.preview

import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.getOrAwaitValue
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.common.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PreviewGoogleTaskViewModelTest : BaseTest() {
  private val id = "t1"

  private val googleTasksApi = mockk<GoogleTasksApi>()
  private val googleTaskRepository = mockk<GoogleTaskRepository>()
  private val googleTaskListRepository = mockk<GoogleTaskListRepository>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val googleTaskPreviewStateAdapter = mockk<GoogleTaskPreviewStateAdapter>()
  private val appWidgetUpdater = mockk<AppWidgetUpdater>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)

  private lateinit var viewModel: PreviewGoogleTaskViewModel

  private fun task(status: String = GoogleTask.TASKS_NEED_ACTION, listId: String = "list1") =
    GoogleTask(taskId = id, listId = listId, status = status, title = "Buy milk")

  private fun taskList(listId: String = "list1") = GoogleTaskList(listId = listId, title = "Work")

  private fun uiPreview() =
    GoogleTaskPreviewState(
      id = id,
      text = "Buy milk",
      notes = null,
      dueDate = null,
      createdDate = null,
      completedDate = null,
      isCompleted = false,
      taskListName = "Work",
      taskListColor = 0,
    )

  @Before
  override fun setUp() {
    super.setUp()
    coEvery { googleTaskRepository.getById(id) } returns task()
    coEvery { googleTaskListRepository.getById("list1") } returns taskList()
    coEvery { googleTaskListRepository.defaultGoogleTaskList() } returns null
    every { googleTaskPreviewStateAdapter.convert(any(), any()) } returns uiPreview()

    viewModel =
      PreviewGoogleTaskViewModel(
        id = id,
        googleTasksApi = googleTasksApi,
        dispatcherProvider = mockDispatcherProvider(),
        googleTaskRepository = googleTaskRepository,
        googleTaskListRepository = googleTaskListRepository,
        analyticsEventSender = analyticsEventSender,
        googleTaskPreviewStateAdapter = googleTaskPreviewStateAdapter,
        appWidgetUpdater = appWidgetUpdater,
        textProvider = textProvider,
      )
  }

  @Test
  fun `sends a feature-used analytics event on creation`() {
    verify(exactly = 1) { analyticsEventSender.send(FeatureUsedEvent(Feature.GOOGLE_TASK_PREVIEW)) }
  }

  @Test
  fun `loads the task into state on first collection`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals("Buy milk", state.task?.text)
      assertEquals("Work", state.task?.taskListName)
    }

  @Test
  fun `does nothing when the task is not found`() =
    runTest {
      coEvery { googleTaskRepository.getById(id) } returns null

      val state = viewModel.state.first()

      assertNull(state.task)
    }

  @Test
  fun `falls back to the default task list when the task's list cannot be found`() =
    runTest {
      coEvery { googleTaskRepository.getById(id) } returns task(listId = "missing")
      coEvery { googleTaskListRepository.getById("missing") } returns null
      val defaultList = taskList(listId = "default")
      coEvery { googleTaskListRepository.defaultGoogleTaskList() } returns defaultList
      every { googleTaskPreviewStateAdapter.convert(any(), defaultList) } returns uiPreview()

      viewModel.state.first()

      coVerify(exactly = 1) { googleTaskPreviewStateAdapter.convert(any(), defaultList) }
    }

  @Test
  fun `onDeleteClick shows the delete confirmation dialog`() =
    runTest {
      viewModel.onDeleteClick()

      assertEquals(true, viewModel.state.first().showDeleteConfirm)
    }

  @Test
  fun `onDeleteDismiss hides the delete confirmation dialog`() =
    runTest {
      viewModel.onDeleteClick()

      viewModel.onDeleteDismiss()

      assertEquals(false, viewModel.state.first().showDeleteConfirm)
    }

  @Test
  fun `onDeleteConfirmed shows an error when the task is not found`() =
    runTest {
      coEvery { googleTaskRepository.getById(id) } returns null
      every { textProvider.getString(R.string.failed_to_update_task) } returns "Failed"

      viewModel.onDeleteConfirmed()

      val event = viewModel.event.getOrAwaitValue()
      assertEquals(
        PreviewGoogleTaskViewModel.PreviewGoogleTaskEvent.ShowError("Failed"),
        event?.getContentIfNotHandled()
      )
    }

  @Test
  fun `onDeleteConfirmed deletes the task and navigates back`() =
    runTest {
      val existing = task()
      coEvery { googleTaskRepository.getById(id) } returns existing
      coEvery { googleTasksApi.deleteTask(existing) } returns true
      coEvery { googleTaskRepository.delete(id) } returns Unit

      viewModel.onDeleteConfirmed()

      coVerify(exactly = 1) { googleTaskRepository.delete(id) }
      val event = viewModel.event.getOrAwaitValue()
      assertEquals(PreviewGoogleTaskViewModel.PreviewGoogleTaskEvent.MoveBack, event?.getContentIfNotHandled())
    }

  @Test
  fun `onDeleteConfirmed shows an error when the delete api call fails`() =
    runTest {
      val existing = task()
      coEvery { googleTaskRepository.getById(id) } returns existing
      coEvery { googleTasksApi.deleteTask(existing) } returns false
      every { textProvider.getString(R.string.failed_to_update_task) } returns "Failed"

      viewModel.onDeleteConfirmed()

      coVerify(exactly = 0) { googleTaskRepository.delete(any()) }
      val event = viewModel.event.getOrAwaitValue()
      assertEquals(
        PreviewGoogleTaskViewModel.PreviewGoogleTaskEvent.ShowError("Failed"),
        event?.getContentIfNotHandled()
      )
    }

  @Test
  fun `onComplete shows an error when the task is not found`() =
    runTest {
      coEvery { googleTaskRepository.getById(id) } returns null
      every { textProvider.getString(R.string.failed_to_update_task) } returns "Failed"

      viewModel.onComplete()

      verify(exactly = 0) { appWidgetUpdater.updateScheduleWidget() }
      val event = viewModel.event.getOrAwaitValue()
      assertEquals(
        PreviewGoogleTaskViewModel.PreviewGoogleTaskEvent.ShowError("Failed"),
        event?.getContentIfNotHandled()
      )
    }

  @Test
  fun `onComplete shows an error when the task is already completed`() =
    runTest {
      coEvery { googleTaskRepository.getById(id) } returns task(status = GoogleTask.TASKS_COMPLETE)
      every { textProvider.getString(R.string.failed_to_update_task) } returns "Failed"

      viewModel.onComplete()

      coVerify(exactly = 0) { googleTasksApi.updateTaskStatus(any(), any()) }
      verify(exactly = 0) { appWidgetUpdater.updateScheduleWidget() }
      val event = viewModel.event.getOrAwaitValue()
      assertEquals(
        PreviewGoogleTaskViewModel.PreviewGoogleTaskEvent.ShowError("Failed"),
        event?.getContentIfNotHandled()
      )
    }

  @Test
  fun `onComplete marks a pending task complete and refreshes the widget`() =
    runTest {
      val existing = task(status = GoogleTask.TASKS_NEED_ACTION)
      val updated = existing.copy(status = GoogleTask.TASKS_COMPLETE)
      coEvery { googleTaskRepository.getById(id) } returns existing
      coEvery { googleTasksApi.updateTaskStatus(GoogleTask.TASKS_COMPLETE, existing) } returns updated
      coEvery { googleTaskRepository.save(updated) } returns Unit

      viewModel.onComplete()

      coVerify(exactly = 1) { googleTaskRepository.save(updated) }
      verify(exactly = 1) { appWidgetUpdater.updateScheduleWidget() }
    }

  @Test
  fun `onComplete shows an error when the api call fails`() =
    runTest {
      val existing = task(status = GoogleTask.TASKS_NEED_ACTION)
      coEvery { googleTaskRepository.getById(id) } returns existing
      coEvery { googleTasksApi.updateTaskStatus(GoogleTask.TASKS_COMPLETE, existing) } returns null
      every { textProvider.getString(R.string.failed_to_update_task) } returns "Failed"

      viewModel.onComplete()

      verify(exactly = 0) { appWidgetUpdater.updateScheduleWidget() }
      val event = viewModel.event.getOrAwaitValue()
      assertEquals(
        PreviewGoogleTaskViewModel.PreviewGoogleTaskEvent.ShowError("Failed"),
        event?.getContentIfNotHandled()
      )
    }
}
