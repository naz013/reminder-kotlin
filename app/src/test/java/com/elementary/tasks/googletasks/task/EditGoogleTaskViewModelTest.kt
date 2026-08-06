package com.elementary.tasks.googletasks.task

import com.elementary.tasks.BaseTest
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.getOrAwaitValue
import com.elementary.tasks.mockDispatcherProvider
import com.elementary.tasks.reminder.scheduling.usecase.ActivateReminderUseCase
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureAdoptedEvent
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.usecase.googletasks.GetAllGoogleTaskListsUseCase
import com.github.naz013.usecase.googletasks.GetGoogleTaskByIdUseCase
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
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class EditGoogleTaskViewModelTest : BaseTest() {
  private val googleTasksApi = mockk<GoogleTasksApi>()
  private val googleTaskRepository = mockk<GoogleTaskRepository>()
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val dateTimeManager = mockk<DateTimeManager>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val getAllGoogleTaskListsUseCase = mockk<GetAllGoogleTaskListsUseCase>()
  private val getGoogleTaskByIdUseCase = mockk<GetGoogleTaskByIdUseCase>()
  private val appWidgetUpdater = mockk<AppWidgetUpdater>(relaxed = true)
  private val activateReminderUseCase = mockk<ActivateReminderUseCase>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val prefs = mockk<Prefs>(relaxed = true)

  private val listA = GoogleTaskList(listId = "list1", title = "Personal", color = 1)
  private val listB = GoogleTaskList(listId = "list2", title = "Work", color = 2)

  private lateinit var viewModel: EditGoogleTaskViewModel

  private fun buildViewModel(
    id: String? = null,
    initialListId: String = "list1",
  ) = EditGoogleTaskViewModel(
    id = id,
    initialListId = initialListId,
    googleTasksApi = googleTasksApi,
    dispatcherProvider = mockDispatcherProvider(),
    googleTaskRepository = googleTaskRepository,
    reminderV2Repository = reminderV2Repository,
    dateTimeManager = dateTimeManager,
    analyticsEventSender = analyticsEventSender,
    getAllGoogleTaskListsUseCase = getAllGoogleTaskListsUseCase,
    getGoogleTaskByIdUseCase = getGoogleTaskByIdUseCase,
    appWidgetUpdater = appWidgetUpdater,
    activateReminderUseCase = activateReminderUseCase,
    textProvider = textProvider,
    prefs = prefs,
  )

  @Before
  override fun setUp() {
    super.setUp()
    coEvery { getAllGoogleTaskListsUseCase() } returns listOf(listA, listB)
    coEvery { getGoogleTaskByIdUseCase(any()) } returns null
    coEvery { googleTaskRepository.getById(any()) } returns null
    coEvery { reminderV2Repository.getById(any()) } returns null
    every { prefs.is24HourFormat } returns true
    every { prefs.hasAdoptedGoogleTasks } returns false
    every { dateTimeManager.toGoogleTaskDate(any()) } returns "24 Jul"
    every { dateTimeManager.getTime(any()) } returns "10:00"
    every { dateTimeManager.getGmtFromDateTime(any<LocalDateTime>()) } returns "2026-07-24 10:00:00.000+0000"
    every { dateTimeManager.toMillis(any<LocalDateTime>()) } returns 1_700_000_000_000L
    every { dateTimeManager.fromMillis(any()) } returns LocalDateTime.of(2026, 7, 24, 9, 0)
    every { dateTimeManager.fromGmtToLocal(any()) } returns LocalDateTime.of(2026, 7, 24, 9, 30)
    every { dateTimeManager.localToUtc(any()) } answers { firstArg() }

    viewModel = buildViewModel()
  }

  private fun observeState(target: EditGoogleTaskViewModel = viewModel): () -> EditGoogleTaskState {
    var latest = EditGoogleTaskState()
    CoroutineScope(Dispatchers.Unconfined).launch { target.state.collect { latest = it } }
    return { latest }
  }

  @Test
  fun `loads task lists and selects the initial list for a new task`() =
    runTest {
      val state = viewModel.state.first()

      assertEquals(2, state.googleTaskLists.size)
      assertEquals("list1", state.listId)
      assertEquals("Personal", state.listName)
      assertEquals("list1", state.initialListId)
      assertEquals(false, state.canMove)
      assertEquals(false, state.canDelete)
      assertEquals(R.string.new_task, state.screenTitleRes)
    }

  @Test
  fun `falls back to the default list when the initial list id is empty`() =
    runTest {
      val defaultList = listA.copy(def = 1)
      coEvery { getAllGoogleTaskListsUseCase() } returns listOf(defaultList, listB)
      val vm = buildViewModel(initialListId = "")

      val state = vm.state.first()

      assertEquals("list1", state.listId)
    }

  @Test
  fun `loads an existing task into state for editing`() =
    runTest {
      val editedTask = GoogleTask(taskId = "g1", listId = "list2", title = "Buy milk", notes = "2%")
      coEvery { getGoogleTaskByIdUseCase("g1") } returns editedTask
      val vm = buildViewModel(id = "g1")

      val state = vm.state.first()

      assertEquals("g1", state.taskId)
      assertEquals("Buy milk", state.title)
      assertEquals("2%", state.notes)
      assertEquals("list2", state.listId)
      assertEquals("Work", state.listName)
      assertEquals(true, state.canMove)
      assertEquals(true, state.canDelete)
      assertEquals(R.string.edit_task, state.screenTitleRes)
    }

  @Test
  fun `loads the due date when editing a task that has one`() =
    runTest {
      val editedTask = GoogleTask(taskId = "g1", listId = "list2", dueDate = 1_700_000_000_000L)
      coEvery { getGoogleTaskByIdUseCase("g1") } returns editedTask
      every { dateTimeManager.fromMillis(1_700_000_000_000L) } returns LocalDateTime.of(2026, 7, 24, 0, 0)
      val vm = buildViewModel(id = "g1")

      val state = vm.state.first()

      assertEquals(true, state.isDateSelected)
      assertEquals(LocalDate.of(2026, 7, 24), state.date)
    }

  @Test
  fun `does not select a date when editing a task without a due date`() =
    runTest {
      val editedTask = GoogleTask(taskId = "g1", listId = "list2", dueDate = 0L)
      coEvery { getGoogleTaskByIdUseCase("g1") } returns editedTask
      val vm = buildViewModel(id = "g1")

      val state = vm.state.first()

      assertEquals(false, state.isDateSelected)
    }

  @Test
  fun `loads the linked reminder time when editing a task with a reminder`() =
    runTest {
      val editedTask = GoogleTask(taskId = "g1", listId = "list2", uuId = "r1")
      coEvery { getGoogleTaskByIdUseCase("g1") } returns editedTask
      val eventDateTime = LocalDateTime.of(2026, 7, 24, 10, 0)
      val reminder = ReminderV2(
        uuId = "r1",
        schedule = ReminderSchedule(startDateTime = eventDateTime, eventDateTime = eventDateTime),
      )
      coEvery { reminderV2Repository.getById("r1") } returns reminder
      every { dateTimeManager.utcToLocal(eventDateTime) } returns LocalDateTime.of(2026, 7, 24, 10, 0)
      val vm = buildViewModel(id = "g1")

      val state = vm.state.first()

      assertEquals("r1", state.reminderId)
      assertEquals(true, state.isTimeSelected)
      assertEquals(LocalTime.of(10, 0), state.time)
    }

  @Test
  fun `does not set a time when the linked reminder cannot be found`() =
    runTest {
      val editedTask = GoogleTask(taskId = "g1", listId = "list2", uuId = "r2")
      coEvery { getGoogleTaskByIdUseCase("g1") } returns editedTask
      coEvery { reminderV2Repository.getById("r2") } returns null
      val vm = buildViewModel(id = "g1")

      val state = vm.state.first()

      assertEquals(false, state.isTimeSelected)
    }

  @Test
  fun `onTitleChange updates title and clears title error`() {
    val latest = observeState()

    viewModel.onTitleChange("Buy milk")

    assertEquals("Buy milk", latest().title)
    assertEquals(false, latest().titleError)
  }

  @Test
  fun `onNotesChange updates notes`() {
    val latest = observeState()

    viewModel.onNotesChange("2%")

    assertEquals("2%", latest().notes)
  }

  @Test
  fun `onDateFieldClick opens the date type chooser dialog`() {
    val latest = observeState()

    viewModel.onDateFieldClick()

    assertEquals(EditGoogleTaskDialog.DateTypeChooser, latest().dialog)
  }

  @Test
  fun `onTimeFieldClick opens the time type chooser dialog when a date is selected`() {
    val latest = observeState()
    viewModel.onDateSet(LocalDate.of(2026, 7, 24))

    viewModel.onTimeFieldClick()

    assertEquals(EditGoogleTaskDialog.TimeTypeChooser, latest().dialog)
  }

  @Test
  fun `onTimeFieldClick does nothing when no date is selected`() {
    val latest = observeState()

    viewModel.onTimeFieldClick()

    assertEquals(null, latest().dialog)
  }

  @Test
  fun `onDateTypeSelected true dismisses the dialog and requests the date picker`() {
    every { textProvider.getString(R.string.select_date) } returns "Select date"
    val latest = observeState()
    viewModel.onDateFieldClick()

    viewModel.onDateTypeSelected(true)

    assertEquals(null, latest().dialog)
    val event = viewModel.event.getOrAwaitValue()
    assertEquals(
      EditGoogleTaskViewModel.EditGoogleTaskEvent.ShowDatePicker(latest().date, "Select date"),
      event?.getContentIfNotHandled(),
    )
  }

  @Test
  fun `onDateTypeSelected false dismisses the dialog and clears the date and time selection`() {
    val latest = observeState()
    viewModel.onDateSet(LocalDate.of(2026, 7, 24))
    viewModel.onTimeSet(LocalTime.of(10, 0))
    viewModel.onDateFieldClick()

    viewModel.onDateTypeSelected(false)

    assertEquals(null, latest().dialog)
    assertEquals(false, latest().isDateSelected)
    assertEquals(false, latest().isTimeSelected)
  }

  @Test
  fun `onTimeTypeSelected true dismisses the dialog and requests the time picker`() {
    every { textProvider.getString(R.string.select_time) } returns "Select time"
    every { prefs.is24HourFormat } returns true
    val latest = observeState()
    viewModel.onDateSet(LocalDate.of(2026, 7, 24))
    viewModel.onTimeFieldClick()

    viewModel.onTimeTypeSelected(true)

    assertEquals(null, latest().dialog)
    val event = viewModel.event.getOrAwaitValue()
    assertEquals(
      EditGoogleTaskViewModel.EditGoogleTaskEvent.ShowTimePicker(latest().time, "Select time", true),
      event?.getContentIfNotHandled(),
    )
  }

  @Test
  fun `onTimeTypeSelected false dismisses the dialog and clears the time selection`() {
    val latest = observeState()
    viewModel.onDateSet(LocalDate.of(2026, 7, 24))
    viewModel.onTimeSet(LocalTime.of(10, 0))
    viewModel.onTimeFieldClick()

    viewModel.onTimeTypeSelected(false)

    assertEquals(null, latest().dialog)
    assertEquals(false, latest().isTimeSelected)
  }

  @Test
  fun `onDateSet updates the date and marks it as selected`() {
    val latest = observeState()

    viewModel.onDateSet(LocalDate.of(2026, 8, 1))

    assertEquals(LocalDate.of(2026, 8, 1), latest().date)
    assertEquals(true, latest().isDateSelected)
    assertEquals("24 Jul", latest().dateText)
  }

  @Test
  fun `onTimeSet updates the time and marks it as selected`() {
    val latest = observeState()

    viewModel.onTimeSet(LocalTime.of(14, 30))

    assertEquals(LocalTime.of(14, 30), latest().time)
    assertEquals(true, latest().isTimeSelected)
    assertEquals("10:00", latest().timeText)
  }

  @Test
  fun `onListFieldClick opens the list picker for selecting a list`() {
    val latest = observeState()

    viewModel.onListFieldClick()

    val dialog = latest().dialog as? EditGoogleTaskDialog.ListPicker
    assertEquals(false, dialog?.forMove)
    assertEquals(2, dialog?.options?.size)
    assertEquals("list1", dialog?.selectedId)
  }

  @Test
  fun `onListFieldClick does nothing when there are no task lists`() {
    coEvery { getAllGoogleTaskListsUseCase() } returns emptyList()
    val vm = buildViewModel()
    val latest = observeState(vm)

    vm.onListFieldClick()

    assertEquals(null, latest().dialog)
  }

  @Test
  fun `onMoveMenuClick opens the list picker for moving`() {
    val latest = observeState()

    viewModel.onMoveMenuClick()

    val dialog = latest().dialog as? EditGoogleTaskDialog.ListPicker
    assertEquals(true, dialog?.forMove)
  }

  @Test
  fun `onListPicked selects a new list when not moving`() {
    val latest = observeState()
    viewModel.onListFieldClick()

    viewModel.onListPicked("list2")

    assertEquals(null, latest().dialog)
    assertEquals("list2", latest().listId)
    assertEquals("Work", latest().listName)
  }

  @Test
  fun `onListPicked moves the task to a different list`() =
    runTest {
      val existingTask = GoogleTask(taskId = "g1", listId = "list1")
      coEvery { getGoogleTaskByIdUseCase("g1") } returns existingTask
      coEvery { googleTaskRepository.getById("g1") } returns existingTask
      val movedTask = existingTask.copy(listId = "list2")
      coEvery { googleTasksApi.moveTask(any(), "list1") } returns movedTask
      coEvery { googleTaskRepository.save(movedTask) } returns Unit
      val vm = buildViewModel(id = "g1")
      vm.state.first()
      vm.onMoveMenuClick()

      vm.onListPicked("list2")

      coVerify(exactly = 1) { googleTaskRepository.getById("g1") }
      coVerify(exactly = 1) { googleTaskRepository.save(movedTask) }
      val event = vm.event.getOrAwaitValue()
      assertEquals(EditGoogleTaskViewModel.EditGoogleTaskEvent.MoveBack, event?.getContentIfNotHandled())
    }

  @Test
  fun `onListPicked shows an error when moving to the same list`() =
    runTest {
      viewModel.state.first()
      every { textProvider.getString(R.string.this_is_same_list) } returns "Same list"
      viewModel.onMoveMenuClick()

      viewModel.onListPicked("list1")

      val event = viewModel.event.getOrAwaitValue()
      assertEquals(EditGoogleTaskViewModel.EditGoogleTaskEvent.ShowError("Same list"), event?.getContentIfNotHandled())
    }

  @Test
  fun `onDeleteMenuClick opens the delete confirmation dialog`() {
    val latest = observeState()

    viewModel.onDeleteMenuClick()

    assertEquals(EditGoogleTaskDialog.DeleteConfirm, latest().dialog)
  }

  @Test
  fun `onDialogDismiss clears the dialog`() {
    val latest = observeState()
    viewModel.onDeleteMenuClick()

    viewModel.onDialogDismiss()

    assertEquals(null, latest().dialog)
  }

  @Test
  fun `onDeleteConfirmed deletes the task and navigates back`() =
    runTest {
      val existingTask = GoogleTask(taskId = "g1")
      coEvery { getGoogleTaskByIdUseCase("g1") } returns existingTask
      coEvery { googleTaskRepository.getById("g1") } returns existingTask
      coEvery { googleTasksApi.deleteTask(existingTask) } returns true
      coEvery { googleTaskRepository.delete("g1") } returns Unit
      val vm = buildViewModel(id = "g1")
      vm.state.first()

      vm.onDeleteConfirmed()

      coVerify(exactly = 1) { googleTaskRepository.getById("g1") }
      coVerify(exactly = 1) { googleTaskRepository.delete("g1") }
      val event = vm.event.getOrAwaitValue()
      assertEquals(EditGoogleTaskViewModel.EditGoogleTaskEvent.MoveBack, event?.getContentIfNotHandled())
    }

  @Test
  fun `onDeleteConfirmed shows an error when the delete api call fails`() =
    runTest {
      val existingTask = GoogleTask(taskId = "g1")
      coEvery { getGoogleTaskByIdUseCase("g1") } returns existingTask
      coEvery { googleTaskRepository.getById("g1") } returns existingTask
      coEvery { googleTasksApi.deleteTask(existingTask) } returns false
      every { textProvider.getString(R.string.failed_to_update_task) } returns "Failed"
      val vm = buildViewModel(id = "g1")
      vm.state.first()

      vm.onDeleteConfirmed()

      val event = vm.event.getOrAwaitValue()
      assertEquals(EditGoogleTaskViewModel.EditGoogleTaskEvent.ShowError("Failed"), event?.getContentIfNotHandled())
    }

  @Test
  fun `save sets a title error and does not save when the title is blank`() {
    val latest = observeState()
    viewModel.onTitleChange("   ")

    viewModel.save()

    assertEquals(true, latest().titleError)
    coVerify(exactly = 0) { googleTasksApi.saveTask(any()) }
  }

  @Test
  fun `save creates a new task and reports feature adoption on first use`() =
    runTest {
      every { prefs.hasAdoptedGoogleTasks } returns false
      viewModel.state.first()
      viewModel.onTitleChange("Buy milk")
      val saved = GoogleTask(taskId = "new1", title = "Buy milk", listId = "list1")
      coEvery { googleTasksApi.saveTask(any()) } returns saved
      coEvery { googleTaskRepository.save(saved) } returns Unit

      viewModel.save()

      verify(exactly = 1) { analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_GOOGLE_TASK)) }
      verify(exactly = 1) { prefs.hasAdoptedGoogleTasks = true }
      verify(exactly = 1) { analyticsEventSender.send(FeatureAdoptedEvent(Feature.CREATE_GOOGLE_TASK)) }
      coVerify(exactly = 1) { googleTaskRepository.save(saved) }
      val event = viewModel.event.getOrAwaitValue()
      assertEquals(EditGoogleTaskViewModel.EditGoogleTaskEvent.MoveBack, event?.getContentIfNotHandled())
    }

  @Test
  fun `save creates a new task without reporting adoption when already adopted`() =
    runTest {
      every { prefs.hasAdoptedGoogleTasks } returns true
      viewModel.state.first()
      viewModel.onTitleChange("Buy milk")
      val saved = GoogleTask(taskId = "new1")
      coEvery { googleTasksApi.saveTask(any()) } returns saved
      coEvery { googleTaskRepository.save(saved) } returns Unit

      viewModel.save()

      verify(exactly = 0) { analyticsEventSender.send(FeatureAdoptedEvent(Feature.CREATE_GOOGLE_TASK)) }
      verify(exactly = 0) { prefs.hasAdoptedGoogleTasks = true }
    }

  @Test
  fun `save creates a new task with a reminder and activates it when a time is selected`() =
    runTest {
      viewModel.state.first()
      viewModel.onTitleChange("Buy milk")
      viewModel.onDateSet(LocalDate.of(2026, 7, 24))
      viewModel.onTimeSet(LocalTime.of(10, 0))
      val saved = GoogleTask(taskId = "new1")
      coEvery { googleTasksApi.saveTask(any()) } returns saved
      coEvery { googleTaskRepository.save(saved) } returns Unit

      viewModel.save()

      coVerify(exactly = 1) { activateReminderUseCase(any()) }
      verify(exactly = 1) { appWidgetUpdater.updateScheduleWidget() }
    }

  @Test
  fun `save shows an error when creating a new task fails`() =
    runTest {
      viewModel.state.first()
      viewModel.onTitleChange("Buy milk")
      coEvery { googleTasksApi.saveTask(any()) } returns null
      every { textProvider.getString(R.string.failed_to_update_task) } returns "Failed"

      viewModel.save()

      val event = viewModel.event.getOrAwaitValue()
      assertEquals(EditGoogleTaskViewModel.EditGoogleTaskEvent.ShowError("Failed"), event?.getContentIfNotHandled())
    }

  @Test
  fun `save updates an existing task in place`() =
    runTest {
      val editingTask = GoogleTask(taskId = "g1", listId = "list1", title = "Old")
      coEvery { getGoogleTaskByIdUseCase("g1") } returns editingTask
      coEvery { googleTaskRepository.getById("g1") } returns editingTask
      val vm = buildViewModel(id = "g1")
      vm.state.first()
      vm.onTitleChange("Buy milk")
      val updated = editingTask.copy(title = "Buy milk")
      coEvery { googleTasksApi.updateTask(any()) } returns updated
      coEvery { googleTaskRepository.save(updated) } returns Unit

      vm.save()

      // Tightened from getById(any()): loadInternal() must have copied the real task id into
      // state (regression test for a bug where it never did, causing save() to always look up
      // by the freshly-generated default id and silently create a duplicate task instead).
      coVerify(exactly = 1) { googleTaskRepository.getById("g1") }
      coVerify(exactly = 1) { googleTaskRepository.save(updated) }
      coVerify(exactly = 0) { googleTasksApi.moveTask(any(), any()) }
      val event = vm.event.getOrAwaitValue()
      assertEquals(EditGoogleTaskViewModel.EditGoogleTaskEvent.MoveBack, event?.getContentIfNotHandled())
    }

  @Test
  fun `save updates and moves an existing task to a different list`() =
    runTest {
      val editingTask = GoogleTask(taskId = "g1", listId = "list1", title = "Old")
      coEvery { getGoogleTaskByIdUseCase("g1") } returns editingTask
      coEvery { googleTaskRepository.getById("g1") } returns editingTask
      val vm = buildViewModel(id = "g1")
      vm.state.first()
      vm.onTitleChange("Buy milk")
      vm.onListFieldClick()
      vm.onListPicked("list2")
      val updated = editingTask.copy(title = "Buy milk", listId = "list2")
      coEvery { googleTasksApi.updateTask(any()) } returns updated
      coEvery { googleTasksApi.moveTask(updated, "list1") } returns updated
      coEvery { googleTaskRepository.save(updated) } returns Unit

      vm.save()

      coVerify(exactly = 1) { googleTaskRepository.getById("g1") }
      coVerify(exactly = 1) { googleTasksApi.moveTask(updated, "list1") }
      coVerify(exactly = 1) { googleTaskRepository.save(updated) }
    }

  @Test
  fun `save shows an error when updating a task fails`() =
    runTest {
      val editingTask = GoogleTask(taskId = "g1", listId = "list1")
      coEvery { getGoogleTaskByIdUseCase("g1") } returns editingTask
      coEvery { googleTaskRepository.getById("g1") } returns editingTask
      val vm = buildViewModel(id = "g1")
      vm.state.first()
      vm.onTitleChange("Buy milk")
      coEvery { googleTasksApi.updateTask(any()) } returns null
      every { textProvider.getString(R.string.failed_to_update_task) } returns "Failed"

      vm.save()

      val event = vm.event.getOrAwaitValue()
      assertEquals(EditGoogleTaskViewModel.EditGoogleTaskEvent.ShowError("Failed"), event?.getContentIfNotHandled())
    }
}
