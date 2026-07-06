package com.elementary.tasks.googletasks.task

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.deeplink.DeepLinkDataParser
import com.elementary.tasks.core.deeplink.GoogleTaskDateTimeDeepLinkData
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.reminder.scheduling.usecase.ActivateReminderUseCase
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.DeepLinkData
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.usecase.googletasks.GetAllGoogleTaskListsUseCase
import com.github.naz013.usecase.googletasks.GetGoogleTaskByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class EditGoogleTaskViewModel(
  val id: String,
  private val initialListId: String,
  private val googleTasksApi: GoogleTasksApi,
  dispatcherProvider: DispatcherProvider,
  private val googleTaskRepository: GoogleTaskRepository,
  private val reminderRepository: ReminderRepository,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val dateTimeManager: DateTimeManager,
  private val analyticsEventSender: AnalyticsEventSender,
  private val getAllGoogleTaskListsUseCase: GetAllGoogleTaskListsUseCase,
  private val getGoogleTaskByIdUseCase: GetGoogleTaskByIdUseCase,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val activateReminderUseCase: ActivateReminderUseCase,
  private val textProvider: TextProvider,
) : BaseProgressViewModel(dispatcherProvider) {
  val state: StateFlow<EditGoogleTaskState> field = MutableStateFlow(EditGoogleTaskState())
  val navigationEvent: LiveData<Event<EditGoogleTaskEvent>> field = mutableLiveEventOf()

  private var isEdited = false
  private var isReminderEdited = false
  private var listId: String = ""

  private var date: LocalDate = LocalDate.now()
  private var time: LocalTime = LocalTime.now()

  private var editedTask: GoogleTask? = null
  private var editedReminder: Reminder? = null

  private var googleTaskLists: List<GoogleTaskList> = emptyList()

  init {
    state.update { it.copy(hasId = id.isNotEmpty()) }
    viewModelScope.launch(dispatcherProvider.default()) {
      googleTaskLists = getAllGoogleTaskListsUseCase()
      editedTask = getGoogleTaskByIdUseCase(id)

      val googleTaskList =
        if (initialListId.isEmpty()) {
          googleTaskLists.firstOrNull { it.isDefault() }
        } else {
          googleTaskLists.firstOrNull { it.listId == initialListId }
            ?: googleTaskLists.firstOrNull { it.isDefault() }
        }

      listId = googleTaskList?.listId ?: ""
      googleTaskList?.also { list ->
        state.update { it.copy(listName = list.title) }
      }
      Logger.i(TAG, "Opening Google Task id=$id, listId=$listId")

      editedTask?.also {
        withUIContext { onEditTask(it) }
      }
    }
  }

  override fun onDestroy(owner: LifecycleOwner) {
    super.onDestroy(owner)
    appWidgetUpdater.updateAllWidgets()
  }

  fun hasId(): Boolean = id.isNotEmpty()

  fun onTitleChange(text: String) {
    state.update { it.copy(title = text, titleError = false) }
  }

  fun onNotesChange(text: String) {
    state.update { it.copy(notes = text) }
  }

  fun onDateFieldClick() {
    state.update { it.copy(dialog = EditGoogleTaskDialog.DateTypeChooser) }
  }

  fun onTimeFieldClick() {
    if (state.value.isDateSelected) {
      state.update { it.copy(dialog = EditGoogleTaskDialog.TimeTypeChooser) }
    }
  }

  fun onDateTypeSelected(selectDate: Boolean) {
    dismissDialog()
    if (selectDate) {
      navigationEvent.postValue(Event(EditGoogleTaskEvent.ShowDatePicker(date)))
    } else {
      onDateStateChanged(false)
    }
  }

  fun onTimeTypeSelected(selectTime: Boolean) {
    dismissDialog()
    if (selectTime) {
      navigationEvent.postValue(Event(EditGoogleTaskEvent.ShowTimePicker(time)))
    } else {
      onTimeStateChanged(false)
    }
  }

  fun onDateSet(localDate: LocalDate) {
    date = localDate
    onDateStateChanged(true)
  }

  fun onTimeSet(localTime: LocalTime) {
    time = localTime
    onTimeStateChanged(true)
  }

  fun onListFieldClick() {
    showListPicker(forMove = false)
  }

  fun onMoveMenuClick() {
    showListPicker(forMove = true)
  }

  private fun showListPicker(forMove: Boolean) {
    if (googleTaskLists.isEmpty()) return
    state.update {
      it.copy(
        dialog =
          EditGoogleTaskDialog.ListPicker(
            options = googleTaskLists.map { list -> GoogleTaskListOption(list.listId, list.title) },
            selectedId = listId,
            forMove = forMove,
          ),
      )
    }
  }

  fun onListPicked(selectedListId: String) {
    val dialog = state.value.dialog as? EditGoogleTaskDialog.ListPicker
    dismissDialog()
    if (dialog?.forMove == true) {
      moveTask(selectedListId)
    } else {
      onListSelected(selectedListId)
    }
  }

  fun onDeleteMenuClick() {
    state.update { it.copy(dialog = EditGoogleTaskDialog.DeleteConfirm) }
  }

  fun onDeleteConfirmed() {
    dismissDialog()
    editedTask?.let { deleteGoogleTask(it) }
  }

  fun onDialogDismiss() {
    dismissDialog()
  }

  private fun dismissDialog() {
    state.update { it.copy(dialog = null) }
  }

  fun save() {
    val summary = state.value.title.trim()
    if (summary.isEmpty()) {
      state.update { it.copy(titleError = true) }
      return
    }
    val note = state.value.notes.trim()
    val reminder = createReminder(summary).takeIf { state.value.isTimeSelected }
    val item = editedTask
    if (item != null) {
      val initListId = item.listId
      val newItem = update(item, summary, note, reminder)
      if (listId.isNotEmpty() && listId != initListId) {
        updateAndMoveGoogleTask(newItem, initListId, reminder)
      } else {
        updateGoogleTask(newItem, reminder)
      }
    } else {
      analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_GOOGLE_TASK))
      newGoogleTask(update(GoogleTask(), summary, note, reminder), reminder)
    }
  }

  private fun onDateStateChanged(enabled: Boolean) {
    if (enabled) {
      state.update {
        it.copy(isDateSelected = true, dateText = dateTimeManager.toGoogleTaskDate(date))
      }
    } else {
      state.update { it.copy(isDateSelected = false, dateText = null) }
      onTimeStateChanged(false)
    }
  }

  private fun onTimeStateChanged(enabled: Boolean) {
    if (enabled) {
      state.update { it.copy(isTimeSelected = true, timeText = dateTimeManager.getTime(time)) }
    } else {
      state.update { it.copy(isTimeSelected = false, timeText = null) }
    }
  }

  private fun moveTask(newListId: String) {
    editedTask?.also {
      val initListId = it.listId
      if (!newListId.matches(initListId.toRegex())) {
        it.listId = newListId
        moveGoogleTask(it, initListId)
      } else {
        postError(textProvider.getString(R.string.this_is_same_list))
      }
    }
  }

  private fun deleteGoogleTask(googleTask: GoogleTask) {
    setBusy(true)
    Logger.i(TAG, "Deleting Google Task (${googleTask.taskId}), listId=${googleTask.listId}")
    viewModelScope.launch(dispatcherProvider.default()) {
      if (googleTasksApi.deleteTask(googleTask)) {
        googleTaskRepository.delete(googleTask.taskId)
        setBusy(false)
        navigationEvent.postValue(Event(EditGoogleTaskEvent.Deleted))
      } else {
        setBusy(false)
        postError(textProvider.getString(R.string.failed_to_update_task))
      }
    }
  }

  private fun onListSelected(newListId: String) {
    listId = newListId
    googleTaskLists
      .firstOrNull { it.listId == newListId }
      ?.also { list ->
        state.update { it.copy(listName = list.title) }
      }
  }

  fun checkDeepLink(arguments: Bundle?) {
    if (arguments?.getBoolean(IntentKeys.INTENT_DEEP_LINK, false) == true) {
      runCatching {
        val parser = DeepLinkDataParser()
        initFromDeepLink(parser.readDeepLinkData(arguments))
      }
    }
  }

  private fun onEditTask(googleTask: GoogleTask) {
    editedTask = googleTask
    listId = googleTask.listId
    if (!isEdited) {
      Logger.d(TAG, "Editing Google Task id=${googleTask.taskId}, listId=${googleTask.listId}")
      state.update { it.copy(title = googleTask.title, notes = googleTask.notes) }
      googleTask.dueDate
        .takeIf { it != 0L }
        ?.let { dateTimeManager.fromMillis(it) }
        ?.also {
          date = it.toLocalDate()
          onDateStateChanged(true)
        }
      isEdited = true
      viewModelScope.launch(dispatcherProvider.default()) {
        googleTaskLists.firstOrNull { it.listId == googleTask.listId }?.also { list ->
          state.update { it.copy(listName = list.title) }
        }
      }
    }
    loadReminder(googleTask.uuId)
  }

  private fun initFromDeepLink(deepLinkData: DeepLinkData?) {
    if (deepLinkData is GoogleTaskDateTimeDeepLinkData) {
      onDateSet(deepLinkData.date)
      deepLinkData.time?.also { onTimeSet(it) }
    }
  }

  private fun moveGoogleTask(
    googleTask: GoogleTask,
    oldListId: String,
  ) {
    setBusy(true)
    Logger.i(
      TAG,
      "Moving Google Task (${googleTask.taskId}) from $oldListId to ${googleTask.listId}",
    )
    viewModelScope.launch(dispatcherProvider.default()) {
      googleTasksApi.moveTask(googleTask, oldListId)?.let {
        googleTaskRepository.save(it)
        setBusy(false)
        navigationEvent.postValue(Event(EditGoogleTaskEvent.Saved))
      } ?: run {
        setBusy(false)
        postError(textProvider.getString(R.string.failed_to_update_task))
      }
    }
  }

  private fun loadReminder(uuId: String) {
    setBusy(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderRepository.getById(uuId)
      if (reminder == null) {
        setBusy(false)
        return@launch
      }
      if (!isReminderEdited) {
        editedReminder = reminder
        time = dateTimeManager.fromGmtToLocal(reminder.eventTime)?.toLocalTime() ?: LocalTime.now()
        onTimeStateChanged(true)
        isReminderEdited = true
      }
      setBusy(false)
    }
  }

  private fun saveReminder(reminder: Reminder?) {
    Logger.d(TAG, "Saving reminder: $reminder")
    if (reminder != null) {
      viewModelScope.launch(dispatcherProvider.default()) {
        val group = reminderGroupRepository.defaultGroup()
        if (group != null) {
          reminder.groupColor = group.groupColor
          reminder.groupTitle = group.groupTitle
          reminder.groupUuId = group.groupUuId
          activateReminderUseCase(reminder)
        }
      }
    }
  }

  private fun newGoogleTask(
    googleTask: GoogleTask,
    reminder: Reminder?,
  ) {
    setBusy(true)
    Logger.i(TAG, "Creating Google Task (${googleTask.taskId}), listId=${googleTask.listId}")
    viewModelScope.launch(dispatcherProvider.default()) {
      googleTasksApi.saveTask(googleTask)?.let {
        googleTaskRepository.save(it)
        saveReminder(reminder)
        setBusy(false)
        navigationEvent.postValue(Event(EditGoogleTaskEvent.Saved))
      } ?: run {
        setBusy(false)
        postError(textProvider.getString(R.string.failed_to_update_task))
      }
    }
  }

  private fun updateGoogleTask(
    googleTask: GoogleTask,
    reminder: Reminder?,
  ) {
    setBusy(true)
    Logger.i(TAG, "Updating Google Task (${googleTask.taskId}), listId=${googleTask.listId}")
    viewModelScope.launch(dispatcherProvider.default()) {
      googleTasksApi.updateTask(googleTask)?.let {
        googleTaskRepository.save(it)
        saveReminder(reminder)
        setBusy(false)
        navigationEvent.postValue(Event(EditGoogleTaskEvent.Saved))
      } ?: run {
        setBusy(false)
        postError(textProvider.getString(R.string.failed_to_update_task))
      }
    }
  }

  private fun updateAndMoveGoogleTask(
    googleTask: GoogleTask,
    oldListId: String,
    reminder: Reminder?,
  ) {
    setBusy(true)
    Logger.i(
      TAG,
      "Updating and moving Google Task (${googleTask.taskId}) " +
        "to ${googleTask.listId} from $oldListId",
    )
    viewModelScope.launch(dispatcherProvider.default()) {
      googleTasksApi
        .updateTask(googleTask)
        ?.let {
          googleTasksApi.moveTask(it, oldListId)
        }?.let {
          googleTaskRepository.save(it)
          saveReminder(reminder)
          setBusy(false)
          navigationEvent.postValue(Event(EditGoogleTaskEvent.Saved))
        } ?: run {
        setBusy(false)
        postError(textProvider.getString(R.string.failed_to_update_task))
      }
    }
  }

  private fun createReminder(task: String) =
    Reminder().apply {
      type = Reminder.BY_DATE
      delay = 0
      eventCount = 0
      useGlobal = true
      isActive = true
      isRemoved = false
      summary = task.normalizeSummary()
      startTime = dateTimeManager.getGmtFromDateTime(LocalDateTime.of(date, time))
      eventTime = startTime
    }

  private fun update(
    googleTask: GoogleTask,
    summary: String,
    note: String,
    reminder: Reminder?,
  ): GoogleTask =
    googleTask.copy(
      listId = listId,
      status = GoogleTask.TASKS_NEED_ACTION,
      title = summary,
      notes = note,
      dueDate =
        date
          .takeIf { state.value.isDateSelected }
          ?.let { dateTimeManager.toMillis(LocalDateTime.of(it, time)) } ?: 0L,
      uuId = reminder?.uuId ?: "",
    )

  private fun String.normalizeSummary(): String =
    if (length > Configs.MAX_REMINDER_SUMMARY_LENGTH) {
      substring(0, Configs.MAX_REMINDER_SUMMARY_LENGTH)
    } else {
      this
    }

  private fun setBusy(busy: Boolean) {
    postInProgress(busy)
    state.update { it.copy(isLoading = busy) }
  }

  companion object {
    private const val TAG = "EditGoogleTaskViewModel"
  }
}
