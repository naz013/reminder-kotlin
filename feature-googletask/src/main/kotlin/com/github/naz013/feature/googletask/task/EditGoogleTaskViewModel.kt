package com.github.naz013.feature.googletask.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureAdoptedEvent
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.GoogleTask
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.feature.googletask.GoogleTasksPreferences
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.SaveOneTimeReminderUseCase
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.ui.common.R
import com.github.naz013.usecase.googletasks.GetAllGoogleTaskListsUseCase
import com.github.naz013.usecase.googletasks.GetGoogleTaskByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.util.UUID

internal class EditGoogleTaskViewModel(
  val id: String?,
  private val initialListId: String,
  private val googleTasksApi: GoogleTasksApi,
  private val dispatcherProvider: DispatcherProvider,
  private val googleTaskRepository: GoogleTaskRepository,
  private val reminderV2Repository: ReminderV2Repository,
  private val dateTimeManager: DateTimeManager,
  private val analyticsEventSender: AnalyticsEventSender,
  private val getAllGoogleTaskListsUseCase: GetAllGoogleTaskListsUseCase,
  private val getGoogleTaskByIdUseCase: GetGoogleTaskByIdUseCase,
  private val saveOneTimeReminderUseCase: SaveOneTimeReminderUseCase,
  private val textProvider: TextProvider,
  private val preferences: GoogleTasksPreferences,
) : ViewModel() {

  private val _state = MutableStateFlow(EditGoogleTaskState())
  val state = _state.stateInWhileSubscribed(EditGoogleTaskState())
    .onStart { loadInternal() }

  val event: LiveData<Event<EditGoogleTaskEvent>> field = mutableLiveEventOf()

  fun onTitleChange(text: String) {
    _state.update { it.copy(title = text, titleError = false) }
  }

  fun onNotesChange(text: String) {
    _state.update { it.copy(notes = text) }
  }

  fun onDateFieldClick() {
    _state.update { it.copy(dialog = EditGoogleTaskDialog.DateTypeChooser) }
  }

  fun onTimeFieldClick() {
    if (_state.value.isDateSelected) {
      _state.update { it.copy(dialog = EditGoogleTaskDialog.TimeTypeChooser) }
    }
  }

  fun onDateTypeSelected(selectDate: Boolean) {
    dismissDialog()
    if (selectDate) {
      event.emit(
        EditGoogleTaskEvent.ShowDatePicker(
          date = _state.value.date,
          title = textProvider.getString(R.string.select_date),
        )
      )
    } else {
      onDateStateChanged(false)
    }
  }

  fun onTimeTypeSelected(selectTime: Boolean) {
    dismissDialog()
    if (selectTime) {
      event.emit(
        EditGoogleTaskEvent.ShowTimePicker(
          time = _state.value.time,
          title = textProvider.getString(R.string.select_time),
          is24Hour = preferences.is24HourFormat,
        )
      )
    } else {
      onTimeStateChanged(false)
    }
  }

  fun onDateSet(localDate: LocalDate) {
    _state.update {
      it.copy(date = localDate)
    }
    onDateStateChanged(true)
  }

  fun onTimeSet(localTime: LocalTime) {
    _state.update {
      it.copy(time = localTime)
    }
    onTimeStateChanged(true)
  }

  fun onListFieldClick() {
    showListPicker(forMove = false)
  }

  fun onMoveMenuClick() {
    showListPicker(forMove = true)
  }

  private fun showListPicker(forMove: Boolean) {
    if (_state.value.googleTaskLists.isEmpty()) return
    _state.update {
      it.copy(
        dialog =
          EditGoogleTaskDialog.ListPicker(
            options = _state.value.googleTaskLists.map { list -> GoogleTaskListOption(list.listId, list.title) },
            selectedId = _state.value.listId,
            forMove = forMove,
          ),
      )
    }
  }

  fun onListPicked(selectedListId: String) {
    val dialog = _state.value.dialog as? EditGoogleTaskDialog.ListPicker
    dismissDialog()
    if (dialog?.forMove == true) {
      moveTask(selectedListId)
    } else {
      onListSelected(selectedListId)
    }
  }

  fun onDeleteMenuClick() {
    _state.update { it.copy(dialog = EditGoogleTaskDialog.DeleteConfirm) }
  }

  fun onDeleteConfirmed() {
    dismissDialog()
    deleteGoogleTask()
  }

  fun onDialogDismiss() {
    dismissDialog()
  }

  private fun dismissDialog() {
    _state.update { it.copy(dialog = null) }
  }

  fun save() {
    val summary = _state.value.title.trim()
    if (summary.isEmpty()) {
      _state.update { it.copy(titleError = true) }
      return
    }
    val note = _state.value.notes.trim()
    val reminderDateTime = dateTimeManager.localToUtc(LocalDateTime.of(_state.value.date, _state.value.time))
      .takeIf { _state.value.isTimeSelected }
    val reminderUuId = UUID.randomUUID().toString()
    viewModelScope.launch(dispatcherProvider.main()) {
      val editTask = withContext(dispatcherProvider.io()) {
        googleTaskRepository.getById(_state.value.taskId)
      }
      if (editTask == null) {
        analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_GOOGLE_TASK))
        if (!preferences.hasAdoptedGoogleTasks) {
          preferences.hasAdoptedGoogleTasks = true
          analyticsEventSender.send(FeatureAdoptedEvent(Feature.CREATE_GOOGLE_TASK))
        }
        newGoogleTask(update(GoogleTask(), summary, note, reminderUuId)) {
          saveReminder(reminderUuId, summary, reminderDateTime)
        }
      } else {
        val newItem = update(editTask, summary, note, reminderUuId)
        if (_state.value.initialListId != _state.value.listId) {
          updateAndMoveGoogleTask(newItem, _state.value.initialListId) {
            saveReminder(reminderUuId, summary, reminderDateTime)
          }
        } else {
          updateGoogleTask(newItem) {
            saveReminder(reminderUuId, summary, reminderDateTime)
          }
        }
      }
    }
  }

  private fun saveReminder(
    uuId: String,
    text: String,
    dateTime: LocalDateTime?,
  ) {
    if (dateTime == null) return
    viewModelScope.launch(dispatcherProvider.io()) {
      saveOneTimeReminderUseCase(
        uuId = uuId,
        summary = text,
        dateTime = dateTime
      )
    }
  }

  private fun onDateStateChanged(enabled: Boolean) {
    if (enabled) {
      _state.update {
        it.copy(
          isDateSelected = true,
          dateText = dateTimeManager.toGoogleTaskDate(_state.value.date)
        )
      }
    } else {
      _state.update { it.copy(isDateSelected = false, dateText = null) }
      onTimeStateChanged(false)
    }
  }

  private fun onTimeStateChanged(enabled: Boolean) {
    if (enabled) {
      _state.update {
        it.copy(
          isTimeSelected = true,
          timeText = dateTimeManager.getTime(_state.value.time)
        )
      }
    } else {
      _state.update { it.copy(isTimeSelected = false, timeText = null) }
    }
  }

  private fun moveTask(newListId: String) {
    val initialListId = _state.value.initialListId
    if (newListId != initialListId) {
      finishTaskMove(newListId)
    } else {
      event.emit(
        EditGoogleTaskEvent.ShowError(textProvider.getString(R.string.this_is_same_list))
      )
    }
  }

  private fun deleteGoogleTask() {
    val taskId = _state.value.taskId
    val listId = _state.value.listId
    Logger.i(TAG, "Deleting Google Task ($taskId), listId=$listId")
    viewModelScope.launch(dispatcherProvider.main()) {
      val googleTask = withContext(dispatcherProvider.io()) {
        googleTaskRepository.getById(taskId)
      } ?: run {
        Logger.e(TAG, "Cannot delete Google Task with id = $taskId. Not found")
        return@launch
      }

      val deleted = withContext(dispatcherProvider.io()) {
        googleTasksApi.deleteTask(googleTask).also {
          if (it) {
            googleTaskRepository.delete(googleTask.taskId)
          }
        }
      }

      if (deleted) {
        event.emit(EditGoogleTaskEvent.MoveBack)
      } else {
        event.emit(EditGoogleTaskEvent.ShowError(textProvider.getString(R.string.failed_to_update_task)))
      }
    }
  }

  private fun onListSelected(newListId: String) {
    val listName = _state.value.googleTaskLists.firstOrNull { it.listId == newListId }?.title
    _state.update {
      it.copy(
        listId = newListId,
        listName = listName ?: ""
      )
    }
  }

  private fun finishTaskMove(newListId: String) {
    viewModelScope.launch(dispatcherProvider.main()) {
      val googleTask = withContext(dispatcherProvider.io()) {
        googleTaskRepository.getById(_state.value.taskId)
      } ?: run {
        Logger.e(TAG, "Failed to move Google Task (${_state.value.taskId}) to List ($newListId). Task not found.")
        return@launch
      }
      Logger.i(
        TAG,
        "Moving Google Task (${googleTask.taskId}) from ${googleTask.listId} to $newListId",
      )
      val oldListId = googleTask.listId
      googleTask.listId = newListId

      val movedGoogleTask = withContext(dispatcherProvider.io()) {
        googleTasksApi.moveTask(googleTask, oldListId)
      } ?: run {
        Logger.e(TAG, "Failed to move Google Task (${_state.value.taskId}) to List ($newListId). Api Error.")
        event.emit(
          EditGoogleTaskEvent.ShowError(textProvider.getString(R.string.failed_to_update_task))
        )
        return@launch
      }

      withContext(dispatcherProvider.io()) {
        googleTaskRepository.save(movedGoogleTask)
      }

      event.emit(EditGoogleTaskEvent.MoveBack)
    }
  }

  private suspend fun loadReminder(uuId: String) {
    val reminder = withContext(dispatcherProvider.io()) {
      reminderV2Repository.getById(uuId)
    }
    if (reminder == null) return
    val time = reminder.schedule.eventDateTime?.let { dateTimeManager.utcToLocal(it) }?.toLocalTime() ?: LocalTime.now()
    _state.update {
      it.copy(
        reminderId = uuId,
        time = time,
      )
    }
    onTimeStateChanged(true)
  }

  private suspend fun newGoogleTask(googleTask: GoogleTask, onSuccess: () -> Unit = {}) {
    Logger.i(TAG, "Creating Google Task (${googleTask.taskId}), listId=${googleTask.listId}")
    val savedGoogleTask = withContext(dispatcherProvider.io()) {
      googleTasksApi.saveTask(googleTask)?.also {
        googleTaskRepository.save(it)
        onSuccess()
      }
    }
    if (savedGoogleTask != null) {
      event.emit(EditGoogleTaskEvent.MoveBack)
    } else {
      event.emit(EditGoogleTaskEvent.ShowError(textProvider.getString(R.string.failed_to_update_task)))
    }
  }

  private suspend fun updateGoogleTask(googleTask: GoogleTask, onSuccess: () -> Unit = {}) {
    Logger.i(TAG, "Updating Google Task (${googleTask.taskId}), listId=${googleTask.listId}")
    val savedGoogleTask = withContext(dispatcherProvider.io()) {
      googleTasksApi.updateTask(googleTask)?.also {
        googleTaskRepository.save(it)
        onSuccess()
      }
    }
    if (savedGoogleTask != null) {
      event.emit(EditGoogleTaskEvent.MoveBack)
    } else {
      event.emit(EditGoogleTaskEvent.ShowError(textProvider.getString(R.string.failed_to_update_task)))
    }
  }

  private suspend fun updateAndMoveGoogleTask(
    googleTask: GoogleTask,
    oldListId: String,
    onSuccess: () -> Unit = {},
  ) {
    Logger.i(
      TAG,
      "Updating and moving Google Task (${googleTask.taskId}) " +
        "to ${googleTask.listId} from $oldListId",
    )
    val savedGoogleTask = withContext(dispatcherProvider.io()) {
      googleTasksApi.updateTask(googleTask)?.let {
        googleTasksApi.moveTask(it, oldListId)
      }?.also {
        googleTaskRepository.save(it)
        onSuccess()
      }
    }
    if (savedGoogleTask != null) {
      event.emit(EditGoogleTaskEvent.MoveBack)
    } else {
      event.emit(EditGoogleTaskEvent.ShowError(textProvider.getString(R.string.failed_to_update_task)))
    }
  }

  private fun update(
    googleTask: GoogleTask,
    summary: String,
    note: String,
    reminderUuId: String?
  ): GoogleTask =
    googleTask.copy(
      listId = _state.value.listId,
      status = GoogleTask.TASKS_NEED_ACTION,
      title = summary,
      notes = note,
      dueDate =
        _state.value.date
          .takeIf { _state.value.isDateSelected }
          ?.let { dateTimeManager.toMillis(LocalDateTime.of(it, _state.value.time)) } ?: 0L,
      uuId = reminderUuId ?: "",
    )



  private fun loadInternal() {
    viewModelScope.launch(dispatcherProvider.main()) {
      val googleTaskLists = withContext(dispatcherProvider.io()) {
        getAllGoogleTaskListsUseCase()
      }
      val editedTask = withContext(dispatcherProvider.io()) {
        id?.let { getGoogleTaskByIdUseCase(it) }
      }

      val googleTaskList = when {
        editedTask != null -> {
          googleTaskLists.firstOrNull { it.listId == editedTask.listId }
        }

        initialListId.isNotEmpty() -> {
          googleTaskLists.firstOrNull { it.listId == initialListId }
        }

        else -> {
          googleTaskLists.firstOrNull { it.isDefault() }
        }
      } ?: googleTaskLists.firstOrNull { it.isDefault() }

      _state.update {
        it.copy(
          listId = googleTaskList?.listId ?: "",
          listName = googleTaskList?.title ?: "",
          initialListId = googleTaskList?.listId ?: "",
          googleTaskLists = googleTaskLists,
          canMove = editedTask != null,
          canDelete = editedTask != null,
          screenTitleRes = if (editedTask != null) {
            R.string.edit_task
          } else {
            R.string.new_task
          }
        )
      }
      Logger.i(TAG, "Opening Google Task id=$id, listId=${_state.value.listId}")

      editedTask?.also { task ->
        Logger.d(TAG, "Editing Google Task id=${task.taskId}, listId=${task.listId}")
        _state.update {
          it.copy(
            taskId = task.taskId,
            title = task.title,
            notes = task.notes
          )
        }

        task.dueDate
          .takeIf { it != 0L }
          ?.let { dateTimeManager.fromMillis(it) }
          ?.also { dueDate ->
            _state.update {
              it.copy(date = dueDate.toLocalDate())
            }
            onDateStateChanged(true)
          }

        loadReminder(task.uuId)
      }
    }
  }

  sealed interface EditGoogleTaskEvent {
    data class ShowDatePicker(
      val date: LocalDate,
      val title: String,
    ) : EditGoogleTaskEvent

    data class ShowTimePicker(
      val time: LocalTime,
      val title: String,
      val is24Hour: Boolean
    ) : EditGoogleTaskEvent

    data object MoveBack : EditGoogleTaskEvent

    data class ShowError(
      val message: String
    ) : EditGoogleTaskEvent
  }

  companion object {
    private const val TAG = "EditGoogleTaskViewModel"
  }
}
