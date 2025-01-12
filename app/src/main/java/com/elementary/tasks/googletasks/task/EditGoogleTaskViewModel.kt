package com.elementary.tasks.googletasks.task

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.deeplink.DeepLinkDataParser
import com.elementary.tasks.core.deeplink.GoogleTaskDateTimeDeepLinkData
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.domain.Reminder
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.DeepLinkData
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.repository.ReminderRepository
import com.github.naz013.usecase.googletasks.GetAllGoogleTaskListsUseCase
import com.github.naz013.usecase.googletasks.GetGoogleTaskByIdUseCase
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class EditGoogleTaskViewModel(
  private val arguments: Bundle?,
  private val googleTasksApi: GoogleTasksApi,
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val googleTaskRepository: GoogleTaskRepository,
  private val reminderRepository: ReminderRepository,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val dateTimeManager: DateTimeManager,
  private val analyticsEventSender: AnalyticsEventSender,
  private val getAllGoogleTaskListsUseCase: GetAllGoogleTaskListsUseCase,
  private val getGoogleTaskByIdUseCase: GetGoogleTaskByIdUseCase,
  private val appWidgetUpdater: AppWidgetUpdater
) : BaseProgressViewModel(dispatcherProvider) {

  private val _dateState = mutableLiveDataOf<DateState>()
  val dateState = _dateState.toLiveData()

  private val _timeState = mutableLiveDataOf<TimeState>()
  val timeState = _timeState.toLiveData()

  private val _taskList = mutableLiveDataOf<GoogleTaskList>()
  val taskList = _taskList.toLiveData()

  private val _task = mutableLiveDataOf<GoogleTask>()
  val task = _task.toLiveData()

  private val _toast = mutableLiveDataOf<Int>()
  val toast = _toast.toLiveData()

  var id = ""
    private set
  private var isEdited = false
  private var isReminderEdited = false
  var listId: String = ""
    private set

  var date: LocalDate = LocalDate.now()
    private set
  var time: LocalTime = LocalTime.now()
    private set

  var editedTask: GoogleTask? = null
    private set
  private var editedReminder: Reminder? = null

  var googleTaskLists: List<GoogleTaskList> = emptyList()
    private set

  init {
    id = arguments?.getString(IntentKeys.INTENT_ID) ?: ""
    viewModelScope.launch(dispatcherProvider.default()) {
      googleTaskLists = getAllGoogleTaskListsUseCase()
      editedTask = getGoogleTaskByIdUseCase(id)

      val bundleListId = arguments?.getString(IntentKeys.INTENT_LIST_ID)
      val googleTaskList = if (bundleListId.isNullOrEmpty()) {
        // Load default list
        googleTaskLists.firstOrNull { it.isDefault() }
      } else {
        // Load list by id
        googleTaskLists.firstOrNull { it.listId == bundleListId }
          ?: googleTaskLists.firstOrNull { it.isDefault() }
      }

      listId = googleTaskList?.listId ?: ""
      googleTaskList?.also {
        _taskList.postValue(it)
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

  fun onCreated(
    arguments: Bundle?,
    savedInstanceState: Bundle?
  ) {
    if (savedInstanceState == null) {
      initDefaults()
      checkDeepLink(arguments)
    }
  }

  fun hasId(): Boolean {
    return id.isNotEmpty()
  }

  fun onDateSet(localDate: LocalDate) {
    date = localDate
    _dateState.postValue(DateState.SelectedDate(dateTimeManager.toGoogleTaskDate(date)))
  }

  fun onTimeSet(localTime: LocalTime) {
    time = localTime
    _timeState.postValue(TimeState.SelectedTime(dateTimeManager.getTime(time)))
  }

  fun save(summary: String, note: String) {
    val reminder = createReminder(summary).takeIf { isTimeSelected() }
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

  fun onDateStateChanged(enabled: Boolean) {
    if (enabled) {
      onDateSet(date)
    } else {
      _dateState.postValue(DateState.NoDate)
      onTimeStateChanged(false)
    }
  }

  fun onTimeStateChanged(enabled: Boolean) {
    if (enabled) {
      onTimeSet(time)
    } else {
      _timeState.postValue(TimeState.NoTime)
    }
  }

  fun moveTask(listId: String) {
    editedTask?.also {
      val initListId = it.listId
      if (!listId.matches(initListId.toRegex())) {
        it.listId = listId
        moveGoogleTask(it, initListId)
      } else {
        _toast.postValue(R.string.this_is_same_list)
      }
    }
  }

  fun deleteGoogleTask(googleTask: GoogleTask) {
    postInProgress(true)
    Logger.i(TAG, "Deleting Google Task (${googleTask.taskId}), listId=${googleTask.listId}")
    viewModelScope.launch(dispatcherProvider.default()) {
      if (googleTasksApi.deleteTask(googleTask)) {
        googleTaskRepository.delete(googleTask.taskId)
        postInProgress(false)
        postCommand(Commands.DELETED)
      } else {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  fun onListSelected(listId: String) {
    this.listId = listId
    googleTaskLists.firstOrNull { it.listId == listId }
      ?.also {
        _taskList.postValue(it)
      }
  }

  fun isDateSelected(): Boolean {
    return dateState.value is DateState.SelectedDate
  }

  fun isTimeSelected(): Boolean {
    return timeState.value is TimeState.SelectedTime
  }

  private fun checkDeepLink(arguments: Bundle?) {
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
      googleTask.dueDate
        .takeIf { it != 0L }
        ?.let { dateTimeManager.fromMillis(it) }
        ?.also {
          date = it.toLocalDate()
          onDateStateChanged(true)
        }
      isEdited = true
      _task.postValue(googleTask)
      viewModelScope.launch(dispatcherProvider.default()) {
        googleTaskLists.firstOrNull { it.listId == googleTask.listId }?.also {
          _taskList.postValue(it)
        }
      }
    }
    loadReminder(googleTask.uuId)
  }

  private fun initFromDeepLink(deepLinkData: DeepLinkData?) {
    if (deepLinkData is GoogleTaskDateTimeDeepLinkData) {
      onDateStateChanged(true)
      onDateSet(deepLinkData.date)
      deepLinkData.time?.also {
        onTimeStateChanged(true)
        onTimeSet(it)
      }
    }
  }

  private fun initDefaults() {
    _dateState.postValue(DateState.NoDate)
    _timeState.postValue(TimeState.NoTime)
  }

  private fun moveGoogleTask(googleTask: GoogleTask, oldListId: String) {
    postInProgress(true)
    Logger.i(
      TAG,
      "Moving Google Task (${googleTask.taskId}) from $oldListId to ${googleTask.listId}"
    )
    viewModelScope.launch(dispatcherProvider.default()) {
      googleTasksApi.moveTask(googleTask, oldListId)?.let {
        googleTaskRepository.save(it)
        postInProgress(false)
        postCommand(Commands.SAVED)
      } ?: run {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  private fun loadReminder(uuId: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderRepository.getById(uuId)
      if (reminder == null) {
        postInProgress(false)
        return@launch
      }
      if (!isReminderEdited) {
        editedReminder = reminder
        time = dateTimeManager.fromGmtToLocal(reminder.eventTime)?.toLocalTime() ?: LocalTime.now()
        onTimeStateChanged(true)
        isReminderEdited = true
      }
      postInProgress(false)
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
          reminderRepository.save(reminder)
        }
        if (reminder.groupUuId != "") {
          eventControlFactory.getController(reminder).enable()
          workerLauncher.startWork(
            ReminderSingleBackupWorker::class.java,
            IntentKeys.INTENT_ID,
            reminder.uuId
          )
        }
      }
    }
  }

  private fun newGoogleTask(googleTask: GoogleTask, reminder: Reminder?) {
    postInProgress(true)
    Logger.i(TAG, "Creating Google Task (${googleTask.taskId}), listId=${googleTask.listId}")
    viewModelScope.launch(dispatcherProvider.default()) {
      googleTasksApi.saveTask(googleTask)?.let {
        googleTaskRepository.save(it)
        saveReminder(reminder)
        postInProgress(false)
        postCommand(Commands.SAVED)
      } ?: run {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  private fun updateGoogleTask(googleTask: GoogleTask, reminder: Reminder?) {
    postInProgress(true)
    Logger.i(TAG, "Updating Google Task (${googleTask.taskId}), listId=${googleTask.listId}")
    viewModelScope.launch(dispatcherProvider.default()) {
      googleTasksApi.updateTask(googleTask)?.let {
        googleTaskRepository.save(it)
        saveReminder(reminder)
        postInProgress(false)
        postCommand(Commands.SAVED)
      } ?: run {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  private fun updateAndMoveGoogleTask(
    googleTask: GoogleTask,
    oldListId: String,
    reminder: Reminder?
  ) {
    postInProgress(true)
    Logger.i(
      TAG,
      "Updating and moving Google Task (${googleTask.taskId}) " +
        "to ${googleTask.listId} from $oldListId"
    )
    viewModelScope.launch(dispatcherProvider.default()) {
      googleTasksApi.updateTask(googleTask)?.let {
        googleTasksApi.moveTask(it, oldListId)
      }?.let {
        googleTaskRepository.save(it)
        saveReminder(reminder)
        postInProgress(false)
        postCommand(Commands.SAVED)
      } ?: run {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  private fun createReminder(task: String) = Reminder().apply {
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
    reminder: Reminder?
  ): GoogleTask {
    return googleTask.copy(
      listId = listId,
      status = GoogleTask.TASKS_NEED_ACTION,
      title = summary,
      notes = note,
      dueDate = date.takeIf { isDateSelected() }
        ?.let { dateTimeManager.toMillis(LocalDateTime.of(it, time)) } ?: 0L,
      uuId = reminder?.uuId ?: ""
    )
  }

  private fun String.normalizeSummary(): String {
    return if (length > Configs.MAX_REMINDER_SUMMARY_LENGTH) {
      substring(0, Configs.MAX_REMINDER_SUMMARY_LENGTH)
    } else {
      this
    }
  }

  sealed class DateState {
    data class SelectedDate(
      val formattedDate: String
    ) : DateState()

    data object NoDate : DateState()
  }

  sealed class TimeState {
    data class SelectedTime(
      val formattedTime: String
    ) : TimeState()

    data object NoTime : TimeState()
  }

  companion object {
    private const val TAG = "EditGoogleTaskViewModel"
  }
}
