package com.elementary.tasks.googletasks.tasklist

import androidx.compose.ui.graphics.Color
import com.elementary.tasks.BaseTest
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.getOrAwaitValue
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.usecase.googletasks.GetGoogleTaskListByIdUseCase
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

class EditGoogleTaskListViewModelTest : BaseTest() {
  private val googleTasksApi = mockk<GoogleTasksApi>()
  private val googleTaskRepository = mockk<GoogleTaskRepository>()
  private val googleTaskListRepository = mockk<GoogleTaskListRepository>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val getGoogleTaskListByIdUseCase = mockk<GetGoogleTaskListByIdUseCase>()
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val themeProvider = mockk<ThemeProvider>()
  private val appWidgetUpdater = mockk<AppWidgetUpdater>(relaxed = true)
  private val prefs = mockk<Prefs>(relaxed = true)

  private lateinit var viewModel: EditGoogleTaskListViewModel

  private fun buildViewModel(listId: String? = null) =
    EditGoogleTaskListViewModel(
      listId = listId,
      googleTasksApi = googleTasksApi,
      dispatcherProvider = mockDispatcherProvider(),
      googleTaskRepository = googleTaskRepository,
      googleTaskListRepository = googleTaskListRepository,
      analyticsEventSender = analyticsEventSender,
      getGoogleTaskListByIdUseCase = getGoogleTaskListByIdUseCase,
      textProvider = textProvider,
      themeProvider = themeProvider,
      appWidgetUpdater = appWidgetUpdater,
      prefs = prefs,
    )

  @Before
  override fun setUp() {
    super.setUp()
    every { themeProvider.colorsForSliderThemed() } returns listOf(Color(0xFFFF0000))
    coEvery { getGoogleTaskListByIdUseCase(any()) } returns null
    coEvery { googleTaskListRepository.getById(any()) } returns null

    viewModel = buildViewModel()
  }

  private fun observeState(target: EditGoogleTaskListViewModel = viewModel): () -> EditGoogleTaskListState {
    var latest = EditGoogleTaskListState()
    CoroutineScope(Dispatchers.Unconfined).launch { target.state.collect { latest = it } }
    return { latest }
  }

  @Test
  fun `initializes slider colors from the theme provider`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals(1, state.sliderColors.size)
    }

  @Test
  fun `does nothing when list id is null`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals("", state.name)
      assertEquals(R.string.new_tasks_list, state.screenTitleRes)
    }

  @Test
  fun `loads an existing list into state for editing`() =
    runTest {
      val list = GoogleTaskList(listId = "list1", title = "Work", color = 3, def = 0)
      coEvery { getGoogleTaskListByIdUseCase("list1") } returns list
      val vm = buildViewModel(listId = "list1")

      val state = vm.state.first()

      assertEquals("list1", state.id)
      assertEquals("Work", state.name)
      assertEquals(3, state.colorIndex)
      assertEquals(R.string.edit_task_list, state.screenTitleRes)
    }

  @Test
  fun `marks the default list as locked and non-deletable`() =
    runTest {
      val list = GoogleTaskList(listId = "list1", title = "Work", def = 1)
      coEvery { getGoogleTaskListByIdUseCase("list1") } returns list
      val vm = buildViewModel(listId = "list1")

      val state = vm.state.first()

      assertEquals(true, state.isDefault)
      assertEquals(true, state.isDefaultLocked)
      assertEquals(true, state.wasDefault)
      assertEquals(false, state.canDelete)
    }

  @Test
  fun `marks a non-default list as deletable and unlocked`() =
    runTest {
      val list = GoogleTaskList(listId = "list1", title = "Work", def = 0)
      coEvery { getGoogleTaskListByIdUseCase("list1") } returns list
      val vm = buildViewModel(listId = "list1")

      val state = vm.state.first()

      assertEquals(false, state.isDefault)
      assertEquals(false, state.isDefaultLocked)
      assertEquals(true, state.canDelete)
    }

  @Test
  fun `onNameChange updates name and clears name error`() {
    val latest = observeState()

    viewModel.onNameChange("Groceries")

    assertEquals("Groceries", latest().name)
    assertEquals(false, latest().nameError)
  }

  @Test
  fun `onColorSelected updates the selected color index`() {
    val latest = observeState()

    viewModel.onColorSelected(5)

    assertEquals(5, latest().colorIndex)
  }

  @Test
  fun `onDefaultToggle flips isDefault when not locked`() {
    val latest = observeState()

    viewModel.onDefaultToggle()

    assertEquals(true, latest().isDefault)
  }

  @Test
  fun `onDefaultToggle does nothing when the list is locked as default`() =
    runTest {
      val list = GoogleTaskList(listId = "list1", title = "Work", def = 1)
      coEvery { getGoogleTaskListByIdUseCase("list1") } returns list
      val vm = buildViewModel(listId = "list1")
      val latest = observeState(vm)

      vm.onDefaultToggle()

      assertEquals(true, latest().isDefault)
    }

  @Test
  fun `onDeleteClick shows the delete confirmation dialog`() {
    val latest = observeState()

    viewModel.onDeleteClick()

    assertEquals(true, latest().showDeleteConfirm)
  }

  @Test
  fun `onDeleteDismiss hides the delete confirmation dialog`() {
    val latest = observeState()
    viewModel.onDeleteClick()

    viewModel.onDeleteDismiss()

    assertEquals(false, latest().showDeleteConfirm)
  }

  @Test
  fun `deleteGoogleTaskList deletes the list and navigates back`() =
    runTest {
      val list = GoogleTaskList(listId = "list1", title = "Work", def = 0)
      coEvery { getGoogleTaskListByIdUseCase("list1") } returns list
      val vm = buildViewModel(listId = "list1")
      vm.state.first()
      coEvery { googleTasksApi.deleteTaskList("list1") } returns true
      coEvery { googleTaskListRepository.delete("list1") } returns Unit
      coEvery { googleTaskRepository.deleteAll("list1") } returns Unit

      vm.deleteGoogleTaskList()

      coVerify(exactly = 1) { googleTaskListRepository.delete("list1") }
      coVerify(exactly = 1) { googleTaskRepository.deleteAll("list1") }
      verify(exactly = 1) { appWidgetUpdater.updateScheduleWidget() }
      val event = vm.navigationEvent.getOrAwaitValue()
      assertEquals(EditGoogleTaskListViewModel.EditGoogleTaskListEvent.MoveBack, event?.getContentIfNotHandled())
    }

  @Test
  fun `deleteGoogleTaskList promotes another list to default when deleting the default list`() =
    runTest {
      val list = GoogleTaskList(listId = "list1", title = "Work", def = 1)
      coEvery { getGoogleTaskListByIdUseCase("list1") } returns list
      val vm = buildViewModel(listId = "list1")
      vm.state.first()
      val otherList = GoogleTaskList(listId = "list2", title = "Personal", def = 0)
      coEvery { googleTasksApi.deleteTaskList("list1") } returns true
      coEvery { googleTaskListRepository.delete("list1") } returns Unit
      coEvery { googleTaskRepository.deleteAll("list1") } returns Unit
      coEvery { googleTaskListRepository.getAll() } returns listOf(otherList)
      coEvery { googleTaskListRepository.save(any()) } returns Unit

      vm.deleteGoogleTaskList()

      coVerify(exactly = 1) { googleTaskListRepository.save(match { it.listId == "list2" && it.def == 1 }) }
    }

  @Test
  fun `deleteGoogleTaskList shows an error when the api call fails`() =
    runTest {
      val list = GoogleTaskList(listId = "list1", title = "Work", def = 0)
      coEvery { getGoogleTaskListByIdUseCase("list1") } returns list
      val vm = buildViewModel(listId = "list1")
      vm.state.first()
      coEvery { googleTasksApi.deleteTaskList("list1") } returns false
      every { textProvider.getString(R.string.failed_to_update_task) } returns "Failed"

      vm.deleteGoogleTaskList()

      val event = vm.navigationEvent.getOrAwaitValue()
      assertEquals(
        EditGoogleTaskListViewModel.EditGoogleTaskListEvent.ShowError("Failed"),
        event?.getContentIfNotHandled(),
      )
    }

  @Test
  fun `save sets a name error and does not save when the name is blank`() {
    val latest = observeState()
    viewModel.onNameChange("   ")

    viewModel.save()

    assertEquals(true, latest().nameError)
  }

  @Test
  fun `save creates a new list and navigates back`() =
    runTest {
      viewModel.state.first()
      viewModel.onNameChange("Groceries")
      val saved = GoogleTaskList(listId = "new1", title = "Groceries")
      coEvery { googleTasksApi.saveTasksList("Groceries", any()) } returns saved
      coEvery { googleTaskListRepository.save(saved) } returns Unit

      viewModel.save()

      coVerify(exactly = 1) { googleTaskListRepository.save(saved) }
      verify(exactly = 1) { analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_GOOGLE_TASK_LIST)) }
      verify(exactly = 1) { appWidgetUpdater.updateScheduleWidget() }
      val event = viewModel.navigationEvent.getOrAwaitValue()
      assertEquals(EditGoogleTaskListViewModel.EditGoogleTaskListEvent.MoveBack, event?.getContentIfNotHandled())
    }

  @Test
  fun `save marks the new list as default and demotes the previous default`() =
    runTest {
      viewModel.state.first()
      viewModel.onNameChange("Groceries")
      viewModel.onDefaultToggle()
      val previousDefault = GoogleTaskList(listId = "old", title = "Old default", def = 1)
      coEvery { googleTaskListRepository.getDefault() } returns listOf(previousDefault)
      coEvery { googleTaskListRepository.save(match { it.listId == "old" }) } returns Unit
      val saved = GoogleTaskList(listId = "new1", title = "Groceries", def = 1)
      coEvery { googleTasksApi.saveTasksList("Groceries", any()) } returns saved
      coEvery { googleTaskListRepository.save(saved) } returns Unit

      viewModel.save()

      coVerify(exactly = 1) { googleTaskListRepository.save(match { it.listId == "old" && it.def == 0 }) }
      coVerify(exactly = 1) { googleTaskListRepository.save(saved) }
    }

  @Test
  fun `save shows an error when creating a new list fails`() =
    runTest {
      viewModel.state.first()
      viewModel.onNameChange("Groceries")
      coEvery { googleTasksApi.saveTasksList("Groceries", any()) } returns null
      every { textProvider.getString(R.string.failed_to_update_task) } returns "Failed"

      viewModel.save()

      val event = viewModel.navigationEvent.getOrAwaitValue()
      assertEquals(
        EditGoogleTaskListViewModel.EditGoogleTaskListEvent.ShowError("Failed"),
        event?.getContentIfNotHandled(),
      )
    }

  @Test
  fun `save updates an existing list and navigates back`() =
    runTest {
      val list = GoogleTaskList(listId = "list1", title = "Work", def = 0)
      coEvery { getGoogleTaskListByIdUseCase("list1") } returns list
      coEvery { googleTaskListRepository.getById("list1") } returns list
      val vm = buildViewModel(listId = "list1")
      vm.state.first()
      vm.onNameChange("Work Updated")
      val updated = list.copy(title = "Work Updated")
      coEvery { googleTasksApi.updateTasksList("Work Updated", any()) } returns updated
      coEvery { googleTaskListRepository.save(updated) } returns Unit

      vm.save()

      coVerify(exactly = 1) { googleTaskListRepository.save(updated) }
      val event = vm.navigationEvent.getOrAwaitValue()
      assertEquals(EditGoogleTaskListViewModel.EditGoogleTaskListEvent.MoveBack, event?.getContentIfNotHandled())
    }

  @Test
  fun `save shows an error when updating a list fails`() =
    runTest {
      val list = GoogleTaskList(listId = "list1", title = "Work", def = 0)
      coEvery { getGoogleTaskListByIdUseCase("list1") } returns list
      coEvery { googleTaskListRepository.getById("list1") } returns list
      val vm = buildViewModel(listId = "list1")
      vm.state.first()
      vm.onNameChange("Work Updated")
      coEvery { googleTasksApi.updateTasksList("Work Updated", any()) } returns null
      every { textProvider.getString(R.string.failed_to_update_task) } returns "Failed"

      vm.save()

      val event = vm.navigationEvent.getOrAwaitValue()
      assertEquals(
        EditGoogleTaskListViewModel.EditGoogleTaskListEvent.ShowError("Failed"),
        event?.getContentIfNotHandled(),
      )
    }
}
