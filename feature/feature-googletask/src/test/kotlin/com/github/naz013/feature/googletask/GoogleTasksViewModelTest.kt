package com.github.naz013.feature.googletask

import android.content.Context
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.logic.googletask.usecase.SyncAllGoogleTaskListsUseCase
import com.github.naz013.platform.SystemInfo
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.getOrAwaitValue
import com.github.naz013.testing.mockDispatcherProvider
import com.github.naz013.ui.googletask.GoogleTaskItemState
import com.github.naz013.ui.googletask.GoogleTaskItemStateAdapter
import com.github.naz013.ui.tag.TagChipStateAdapter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GoogleTasksViewModelTest : BaseTest() {
  private val googleTasksApi = mockk<GoogleTasksApi>()
  private val appWidgetUpdater = mockk<AppWidgetUpdater>(relaxed = true)
  private val googleTaskRepository = mockk<GoogleTaskRepository>()
  private val googleTaskListRepository = mockk<GoogleTaskListRepository>()
  private val googleTaskItemStateAdapter = mockk<GoogleTaskItemStateAdapter>()
  private val syncAllGoogleTaskListsUseCase = mockk<SyncAllGoogleTaskListsUseCase>(relaxed = true)
  private val contextProvider = mockk<ContextProvider>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val googleTasksAuthManager = mockk<GoogleTasksAuthManager>()
  private val systemInfo = mockk<SystemInfo>()
  private val tagAssignmentRepository = mockk<TagAssignmentRepository>(relaxed = true)
  private val tagRepository = mockk<TagRepository>()
  private val tagChipStateAdapter = mockk<TagChipStateAdapter>()

  private lateinit var viewModel: GoogleTasksViewModel

  private fun taskList(
    id: String,
    title: String = "List $id",
    isDefault: Boolean = false,
    color: Int = 0,
  ) = GoogleTaskList(listId = id, title = title, def = if (isDefault) 1 else 0, color = color)

  private fun task(
    id: String,
    listId: String = "a",
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

  private fun buildViewModel() =
    GoogleTasksViewModel(
      googleTasksApi = googleTasksApi,
      dispatcherProvider = mockDispatcherProvider(),
      appWidgetUpdater = appWidgetUpdater,
      googleTaskRepository = googleTaskRepository,
      googleTaskListRepository = googleTaskListRepository,
      googleTaskItemStateAdapter = googleTaskItemStateAdapter,
      syncAllGoogleTaskListsUseCase = syncAllGoogleTaskListsUseCase,
      contextProvider = contextProvider,
      analyticsEventSender = analyticsEventSender,
      textProvider = textProvider,
      googleTasksAuthManager = googleTasksAuthManager,
      systemInfo = systemInfo,
      tagAssignmentRepository = tagAssignmentRepository,
      tagRepository = tagRepository,
      tagChipStateAdapter = tagChipStateAdapter,
    )

  @Before
  override fun setUp() {
    super.setUp()
    every { contextProvider.themedContext } returns mockk<Context>(relaxed = true)
    every { googleTasksAuthManager.isAuthorized() } returns true
    coEvery { googleTaskListRepository.getAll() } returns emptyList()
    coEvery { googleTaskRepository.getAll() } returns emptyList()
    every { googleTaskItemStateAdapter.convert(any(), any()) } returns uiTask()
    every { tagRepository.observeAll() } returns flowOf(emptyList())

    viewModel = buildViewModel()
  }

  private fun observeState(): () -> GoogleTasksState {
    var latest = GoogleTasksState()
    CoroutineScope(Dispatchers.Unconfined).launch { viewModel.state.collect { latest = it } }
    return { latest }
  }

  @Test
  fun `sends a screen-used analytics event on creation`() {
    verify(exactly = 1) { analyticsEventSender.send(ScreenUsedEvent(Screen.GOOGLE_TASKS_LIST)) }
  }

  @Test
  fun `loads task lists and tasks with fab colors from the default list on first collection`() =
    runTest {
      val listA = taskList("a", isDefault = true)
      val listB = taskList("b")
      coEvery { googleTaskListRepository.getAll() } returns listOf(listA, listB)
      val t1 = task("t1", listId = "a")
      coEvery { googleTaskRepository.getAll() } returns listOf(t1)
      every { googleTaskItemStateAdapter.convert(t1, listA) } returns uiTask("t1")

      val state = viewModel.state.first()

      assertEquals(true, state.isLoggedIn)
      assertEquals(2, state.taskLists.size)
      assertEquals(1, state.tasks.size)
      assertNotNull(state.fabContainerColor)
      assertNotNull(state.fabContentColor)
    }

  @Test
  fun `onTagSelected filters tasks down to items carrying that tag`() =
    runTest {
      val t1 = task("t1")
      val t2 = task("t2")
      coEvery { googleTaskRepository.getAll() } returns listOf(t1, t2)
      every { googleTaskItemStateAdapter.convert(t1, null) } returns uiTask("t1")
      every { googleTaskItemStateAdapter.convert(t2, null) } returns uiTask("t2")
      coEvery { tagAssignmentRepository.getItemIdsForTag("tag1", TaggedItemType.GOOGLE_TASK) } returns listOf("t1")
      val vm = buildViewModel()
      vm.state.first()

      vm.onTagSelected("tag1")

      assertEquals(listOf("t1"), vm.state.first().tasks.map { it.id })
    }

  @Test
  fun `onTagSelected twice with the same tag clears the filter`() =
    runTest {
      val t1 = task("t1")
      val t2 = task("t2")
      coEvery { googleTaskRepository.getAll() } returns listOf(t1, t2)
      every { googleTaskItemStateAdapter.convert(t1, null) } returns uiTask("t1")
      every { googleTaskItemStateAdapter.convert(t2, null) } returns uiTask("t2")
      coEvery { tagAssignmentRepository.getItemIdsForTag("tag1", TaggedItemType.GOOGLE_TASK) } returns listOf("t1")
      val vm = buildViewModel()
      vm.state.first()
      vm.onTagSelected("tag1")

      vm.onTagSelected("tag1")

      assertEquals(null, vm.state.first().selectedTagId)
      assertEquals(2, vm.state.first().tasks.size)
    }

  @Test
  fun `isLoggedIn reflects the auth manager state on load`() =
    runTest {
      every { googleTasksAuthManager.isAuthorized() } returns false

      val state = viewModel.state.first()

      assertEquals(false, state.isLoggedIn)
    }

  @Test
  fun `fab colors are null when there are no task lists`() =
    runTest {
      coEvery { googleTaskListRepository.getAll() } returns emptyList()

      val state = viewModel.state.first()

      assertNull(state.fabContainerColor)
      assertNull(state.fabContentColor)
    }

  @Test
  fun `falls back to the first task list for fab colors when none is marked default`() =
    runTest {
      val listA = taskList("a")
      val listB = taskList("b")
      coEvery { googleTaskListRepository.getAll() } returns listOf(listA, listB)

      val state = viewModel.state.first()

      assertNotNull(state.fabContainerColor)
    }

  @Test
  fun `onGoogleTasksAuthFailed emits ShowLoginError`() {
    viewModel.onGoogleTasksAuthFailed()

    val event = viewModel.event.getOrAwaitValue()
    assertEquals(GoogleTasksViewModel.ViewModelEvent.ShowLoginError, event?.getContentIfNotHandled())
  }

  @Test
  fun `onGoogleTasksLoginStateChanged true updates isLoggedIn, syncs and reports feature use`() =
    runTest {
      val latest = observeState()

      viewModel.onGoogleTasksLoginStateChanged(true)

      assertEquals(true, latest().isLoggedIn)
      coVerify(exactly = 1) { syncAllGoogleTaskListsUseCase() }
      verify(exactly = 1) { analyticsEventSender.send(FeatureUsedEvent(Feature.GOOGLE_TASK)) }
    }

  @Test
  fun `onGoogleTasksLoginStateChanged false updates isLoggedIn without syncing`() =
    runTest {
      val latest = observeState()

      viewModel.onGoogleTasksLoginStateChanged(false)

      assertEquals(false, latest().isLoggedIn)
      coVerify(exactly = 0) { syncAllGoogleTaskListsUseCase() }
    }

  @Test
  fun `onLoginClicked emits Login when Google Play services are available`() {
    every { systemInfo.googlePlayServicesAvailable } returns true

    viewModel.onLoginClicked()

    val event = viewModel.event.getOrAwaitValue()
    assertEquals(GoogleTasksViewModel.ViewModelEvent.Login, event?.getContentIfNotHandled())
  }

  @Test
  fun `onLoginClicked emits an error when Google Play services are unavailable`() {
    every { systemInfo.googlePlayServicesAvailable } returns false
    every { textProvider.getString(R.string.google_play_services_not_installed) } returns "No Play Services"

    viewModel.onLoginClicked()

    val event = viewModel.event.getOrAwaitValue()
    assertEquals(
      GoogleTasksViewModel.ViewModelEvent.ShowError("No Play Services"),
      event?.getContentIfNotHandled(),
    )
  }

  @Test
  fun `onBackPressed emits MoveBack`() {
    viewModel.onBackPressed()

    val event = viewModel.event.getOrAwaitValue()
    assertEquals(GoogleTasksViewModel.ViewModelEvent.MoveBack, event?.getContentIfNotHandled())
  }

  @Test
  fun `sync triggers a full sync and reload`() =
    runTest {
      viewModel.sync()

      coVerify(exactly = 1) { syncAllGoogleTaskListsUseCase() }
    }

  @Test
  fun `loadGoogleTasks triggers a full sync and reload`() =
    runTest {
      viewModel.loadGoogleTasks()

      coVerify(exactly = 1) { syncAllGoogleTaskListsUseCase() }
    }

  @Test
  fun `toggleTask does nothing when the task is not found`() =
    runTest {
      coEvery { googleTaskRepository.getById("missing") } returns null

      viewModel.toggleTask("missing")

      coVerify(exactly = 0) { googleTasksApi.updateTaskStatus(any(), any()) }
      verify(exactly = 0) { appWidgetUpdater.updateScheduleWidget() }
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
  fun `toggleTask reverts a completed task to needing action`() =
    runTest {
      val existing = task("t1", status = GoogleTask.TASKS_COMPLETE)
      val updated = existing.copy(status = GoogleTask.TASKS_NEED_ACTION)
      coEvery { googleTaskRepository.getById("t1") } returns existing
      coEvery { googleTasksApi.updateTaskStatus(GoogleTask.TASKS_NEED_ACTION, existing) } returns updated
      coEvery { googleTaskRepository.save(updated) } returns Unit

      viewModel.toggleTask("t1")

      coVerify(exactly = 1) { googleTaskRepository.save(updated) }
    }

  @Test
  fun `toggleTask does not refresh the widget when the api call fails`() =
    runTest {
      val existing = task("t1", status = GoogleTask.TASKS_NEED_ACTION)
      coEvery { googleTaskRepository.getById("t1") } returns existing
      coEvery { googleTasksApi.updateTaskStatus(GoogleTask.TASKS_COMPLETE, existing) } returns null

      viewModel.toggleTask("t1")

      coVerify(exactly = 0) { googleTaskRepository.save(any()) }
      verify(exactly = 0) { appWidgetUpdater.updateScheduleWidget() }
    }

  @Test
  fun `toggleTask swallows api exceptions and does not refresh the widget`() =
    runTest {
      val existing = task("t1", status = GoogleTask.TASKS_NEED_ACTION)
      coEvery { googleTaskRepository.getById("t1") } returns existing
      coEvery { googleTasksApi.updateTaskStatus(any(), any()) } throws RuntimeException("boom")

      viewModel.toggleTask("t1")

      coVerify(exactly = 0) { googleTaskRepository.save(any()) }
      verify(exactly = 0) { appWidgetUpdater.updateScheduleWidget() }
    }
}
