package com.elementary.tasks.core.view_models.google_tasks

import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.analytics.AnalyticsEventSender
import com.elementary.tasks.core.analytics.Feature
import com.elementary.tasks.core.analytics.FeatureUsedEvent
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.normalizeSummary
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.view_models.BaseProgressViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.elementary.tasks.google_tasks.create.TasksConstants
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import timber.log.Timber

class GoogleTaskViewModel(
  id: String,
  private val gTasks: GTasks,
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val googleTasksDao: GoogleTasksDao,
  private val googleTaskListsDao: GoogleTaskListsDao,
  private val reminderDao: ReminderDao,
  private val reminderGroupDao: ReminderGroupDao,
  private val dateTimeManager: DateTimeManager,
  private val analyticsEventSender: AnalyticsEventSender
) : BaseProgressViewModel(dispatcherProvider) {

  private val _formattedDate = mutableLiveDataOf<String>()
  val formattedDate = _formattedDate.toLiveData()

  private val _formattedTime = mutableLiveDataOf<String>()
  val formattedTime = _formattedTime.toLiveData()

  private val _isReminder = mutableLiveDataOf<Boolean>()
  val isReminder = _isReminder.toLiveData()

  private val _isDateEnabled = mutableLiveDataOf<Boolean>()
  val isDateEnabled = _isDateEnabled.toLiveData()

  private val _taskList = mutableLiveDataOf<GoogleTaskList>()
  val taskList = _taskList.toLiveData()

  var isEdited = false
  private var isReminderEdited = false
  var listId: String = ""
  var action: String = ""
  var date: LocalDate = LocalDate.now()
    private set
  var time: LocalTime = LocalTime.now()
    private set

  var isLoading = false
  var editedTask: GoogleTask? = null
  private var editedReminder: Reminder? = null

  val googleTask = googleTasksDao.loadById(id)
  val defaultTaskList = googleTaskListsDao.loadDefault()
  val googleTaskLists = googleTaskListsDao.loadAll()

  val isLogged = gTasks.isLogged

  fun onDateSet(localDate: LocalDate) {
    date = localDate
    _formattedDate.postValue(dateTimeManager.toGoogleTaskDate(date))
  }

  fun onTimeSet(localTime: LocalTime) {
    time = localTime
    _formattedTime.postValue(dateTimeManager.toTime(time))
  }

  fun save(summary: String, note: String) {
    val reminder = createReminder(summary).takeIf { isReminder.value == true }
    val item = editedTask
    if (action == TasksConstants.EDIT && item != null) {
      val initListId = item.listId
      val newItem = update(item, summary, note, reminder)
      if (listId.isNotEmpty()) {
        updateAndMoveGoogleTask(newItem, initListId, reminder)
      } else {
        updateGoogleTask(newItem, reminder)
      }
    } else {
      analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_GOOGLE_TASK))
      newGoogleTask(update(GoogleTask(), summary, note, reminder), reminder)
    }
  }

  fun onEditTask(googleTask: GoogleTask) {
    editedTask = googleTask
    listId = googleTask.listId
    if (!isEdited) {
      googleTask.dueDate
        .takeIf { it != 0L }
        ?.also {
          date = dateTimeManager.fromMillis(it).toLocalDate()
          onDateStateChanged(true)
        }
      isEdited = true
      viewModelScope.launch(dispatcherProvider.default()) {
        googleTaskListsDao.all().firstOrNull { it.listId == googleTask.listId }?.also {
          _taskList.postValue(it)
        }
      }
    }
    loadReminder(googleTask.uuId)
  }

  fun initDefaults() {
    onDateSet(LocalDate.now())
    onTimeSet(LocalTime.now())
  }

  fun onDateStateChanged(enabled: Boolean) {
    _isDateEnabled.postValue(enabled)
    if (enabled) {
      onDateSet(date)
    }
  }

  fun onReminderStateChanged(enabled: Boolean) {
    _isReminder.postValue(enabled)
    if (enabled) {
      onTimeSet(time)
    }
  }

  fun moveGoogleTask(googleTask: GoogleTask, oldListId: String) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      googleTasksDao.insert(googleTask)
      gTasks.moveTask(googleTask, oldListId)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun deleteGoogleTask(googleTask: GoogleTask) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      try {
        gTasks.deleteTask(googleTask)
        googleTasksDao.delete(googleTask)
        postInProgress(false)
        postCommand(Commands.DELETED)
      } catch (e: Exception) {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  private fun loadReminder(uuId: String) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderDao.getById(uuId) ?: return@launch
      if (!isReminderEdited) {
        editedReminder = reminder
        time = dateTimeManager.fromGmtToLocal(reminder.eventTime).toLocalTime()
        isReminderEdited = true
        onReminderStateChanged(true)
      }
      postInProgress(false)
    }
  }

  private fun saveReminder(reminder: Reminder?) {
    Timber.d("saveReminder: $reminder")
    if (reminder != null) {
      viewModelScope.launch(dispatcherProvider.default()) {
        val group = reminderGroupDao.defaultGroup()
        if (group != null) {
          reminder.groupColor = group.groupColor
          reminder.groupTitle = group.groupTitle
          reminder.groupUuId = group.groupUuId
          reminderDao.insert(reminder)
        }
        if (reminder.groupUuId != "") {
          eventControlFactory.getController(reminder).start()
          workerLauncher.startWork(
            ReminderSingleBackupWorker::class.java,
            Constants.INTENT_ID, reminder.uuId
          )
        }
      }
    }
  }

  private fun newGoogleTask(googleTask: GoogleTask, reminder: Reminder?) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      try {
        gTasks.insertTask(googleTask)
        saveReminder(reminder)
        postInProgress(false)
        postCommand(Commands.SAVED)
      } catch (e: Exception) {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  private fun updateGoogleTask(googleTask: GoogleTask, reminder: Reminder?) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      googleTasksDao.insert(googleTask)
      try {
        gTasks.updateTask(googleTask)
        saveReminder(reminder)
        postInProgress(false)
        postCommand(Commands.SAVED)
      } catch (e: Exception) {
        postInProgress(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  private fun updateAndMoveGoogleTask(googleTask: GoogleTask, oldListId: String, reminder: Reminder?) {
    if (!gTasks.isLogged) {
      postCommand(Commands.FAILED)
      return
    }
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      googleTasksDao.insert(googleTask)
      try {
        gTasks.updateTask(googleTask)
        gTasks.moveTask(googleTask, oldListId)
        saveReminder(reminder)
        postInProgress(false)
        postCommand(Commands.SAVED)
      } catch (e: Exception) {
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
      status = GTasks.TASKS_NEED_ACTION,
      title = summary,
      notes = note,
      dueDate = date.takeIf { isDateEnabled.value == true }
        ?.let { dateTimeManager.toMillis(LocalDateTime.of(date, time)) } ?: 0L,
      uuId = reminder?.uuId ?: ""
    )
  }
}
