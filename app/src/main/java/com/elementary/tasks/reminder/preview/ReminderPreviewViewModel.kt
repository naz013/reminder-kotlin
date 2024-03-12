package com.elementary.tasks.reminder.preview

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.dao.CalendarEventsDao
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.ui.UiShareData
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.data.ui.note.UiNoteList
import com.elementary.tasks.core.data.ui.reminder.UiReminderType
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.TextProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.reminder.preview.data.UiCalendarEventList
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewData
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewDataAdapter
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewDetails
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import timber.log.Timber

class ReminderPreviewViewModel(
  private val id: String,
  private val reminderDao: ReminderDao,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val uiReminderPreviewDataAdapter: UiReminderPreviewDataAdapter,
  private val backupTool: BackupTool,
  private val updatesHelper: UpdatesHelper,
  private val notesDao: NotesDao,
  private val googleTasksDao: GoogleTasksDao,
  private val googleTaskListsDao: GoogleTaskListsDao,
  private val calendarEventsDao: CalendarEventsDao,
  private val reminderGroupDao: ReminderGroupDao,
  private val dateTimeManager: DateTimeManager,
  private val googleTaskToUiReminderPreviewGoogleTask: GoogleTaskToUiReminderPreviewGoogleTask,
  private val noteToUiReminderPreviewNote: NoteToUiReminderPreviewNote,
  private val textProvider: TextProvider,
  private val eventToUiReminderPreview: EventToUiReminderPreview
) : BaseProgressViewModel(dispatcherProvider) {

  private val _note = mutableLiveDataOf<UiNoteList>()
  val note = _note.toLiveData()

  private val _googleTask = mutableLiveDataOf<UiGoogleTaskList>()
  val googleTask = _googleTask.toLiveData()

  private val _sharedFile = mutableLiveDataOf<UiShareData>()
  val sharedFile = _sharedFile.toLiveData()

  private val _reminder = mutableLiveDataOf<UiReminderPreviewDetails>()
  val reminder = _reminder.toLiveData()

  private val _reminderData = mutableLiveDataOf<List<UiReminderPreviewData>>()
  val reminderData = _reminderData.toLiveData()

  var canCopy = false
    private set
  var canDelete = false
    private set

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    loadReminder()
  }

  fun onSubTaskRemoved(subTaskId: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderDao.getById(id) ?: return@launch
      val subTasks = reminder.shoppings.toMutableList()
      val index = subTasks.indexOfFirst { it.uuId == subTaskId }

      Timber.d("onSubTaskRemoved: id=$subTaskId, subTasks=$subTasks")

      if (index != -1) {
        subTasks.removeAt(index)

        Timber.d("onSubTaskRemoved: save subTasks=$subTasks")

        saveReminder(reminder.copy(shoppings = subTasks.toList()))
        loadReminder()
      }
    }
  }

  fun onSubTaskChecked(subTaskId: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderDao.getById(id) ?: return@launch
      val subTasks = reminder.shoppings
      val index = subTasks.indexOfFirst { it.uuId == subTaskId }

      if (index != -1) {
        subTasks[index].isChecked = !subTasks[index].isChecked
        saveReminder(reminder.copy(shoppings = subTasks.toList()))
        loadReminder()
      }
    }
  }

  fun switchClick() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderDao.getById(id) ?: return@launch

      if (reminder.isRemoved) return@launch

      toggleReminder(reminder)
    }
  }

  private fun loadReminder() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderDao.getById(id) ?: return@launch

      val type = UiReminderType(reminder.type)

      canCopy = type.isBase(UiReminderType.Base.DATE)
      canDelete = reminder.isRemoved

      val data = uiReminderPreviewDataAdapter.create(reminder).toMutableList()
      _reminderData.postValue(data)

      notesDao.getById(reminder.noteId)?.let { noteToUiReminderPreviewNote(it) }
        ?.also { data.addAll(it) }

      googleTasksDao.getByReminderId(reminder.uuId)?.let {
        googleTaskToUiReminderPreviewGoogleTask(it, googleTaskListsDao.getById(it.listId))
      }?.also { data.addAll(it) }

      googleCalendarUtils.loadEvents(reminder.uuId).takeIf { it.isNotEmpty() }?.let {
        eventToUiReminderPreview(it, googleCalendarUtils.getCalendarsList())
      }?.also { data.addAll(it) }

      _reminderData.postValue(data)
    }
  }

  private fun saveReminder(reminder: Reminder) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      runBlocking {
        reminderDao.insert(reminder)
      }
      updatesHelper.updateTasksWidget()
      workerLauncher.startWork(
        ReminderSingleBackupWorker::class.java,
        Constants.INTENT_ID,
        reminder.uuId
      )
      postInProgress(false)
      postCommand(Commands.SAVED)
      loadReminder()
    }
  }

  fun deleteEvent(eventItem: UiCalendarEventList) {
    viewModelScope.launch(dispatcherProvider.default()) {
      if (eventItem.localId.isNotBlank()) {
        calendarEventsDao.deleteById(eventItem.localId)
      }
      googleCalendarUtils.deleteEvent(eventItem.id)
      loadReminder()
    }
  }

  private suspend fun toggleReminder(reminder: Reminder) {
    postInProgress(true)
    if (!eventControlFactory.getController(reminder).onOff()) {
      postInProgress(false)
      postCommand(Commands.OUTDATED)
    } else {
      workerLauncher.startWork(
        ReminderSingleBackupWorker::class.java,
        Constants.INTENT_ID,
        reminder.uuId
      )
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
    loadReminder()
  }

  fun copyReminder(time: LocalTime) {
    val reminderId = reminder.value?.id ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderDao.getById(reminderId)?.also { reminder ->
        postInProgress(true)
        runBlocking {
          if (reminder.groupUuId == "") {
            val group = reminderGroupDao.defaultGroup()
            if (group != null) {
              reminder.groupColor = group.groupColor
              reminder.groupTitle = group.groupTitle
              reminder.groupUuId = group.groupUuId
            }
          }
          val newItem = reminder.copy()
          newItem.summary = reminder.summary + " - " + textProvider.getText(R.string.copy)

          val date = dateTimeManager.fromGmtToLocal(newItem.eventTime)?.toLocalDate()
            ?: LocalDate.now()
          var dateTime = LocalDateTime.of(date, time)

          while (dateTime < LocalDateTime.now()) {
            dateTime = dateTime.plusDays(1)
          }
          newItem.eventTime = dateTimeManager.getGmtFromDateTime(dateTime)
          newItem.startTime = dateTimeManager.getGmtFromDateTime(dateTime)
          reminderDao.insert(newItem)
          eventControlFactory.getController(newItem).enable()
        }
        postCommand(Commands.SAVED)
      }
    }
  }

  fun deleteReminder(showMessage: Boolean) {
    val reminderId = reminder.value?.id ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderDao.getById(reminderId)?.also { reminder ->
        if (showMessage) {
          withResult {
            eventControlFactory.getController(reminder).disable()
            reminderDao.delete(reminder)
            googleCalendarUtils.deleteEvents(reminder.uuId)
            workerLauncher.startWork(
              ReminderDeleteBackupWorker::class.java,
              Constants.INTENT_ID,
              reminder.uuId
            )
            Commands.DELETED
          }
        } else {
          withProgress {
            eventControlFactory.getController(reminder).disable()
            reminderDao.delete(reminder)
            googleCalendarUtils.deleteEvents(reminder.uuId)
            workerLauncher.startWork(
              ReminderDeleteBackupWorker::class.java,
              Constants.INTENT_ID,
              reminder.uuId
            )
          }
        }
      }
    }
  }

  fun moveToTrash() {
    val reminderId = reminder.value?.id ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderDao.getById(reminderId)?.also {
        it.isRemoved = true
        eventControlFactory.getController(it).disable()
        reminderDao.insert(it)
        workerLauncher.startWork(
          ReminderSingleBackupWorker::class.java,
          Constants.INTENT_ID,
          it.uuId
        )
        Commands.DELETED
      }
    }
  }

  fun shareReminder() {
    val reminderId = reminder.value?.id ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderDao.getById(reminderId)?.let {
        UiShareData(
          file = backupTool.reminderToFile(it),
          name = it.summary
        )
      }?.also {
        Timber.d("shareReminder: $it")
        _sharedFile.postValue(it)
      }
    }
  }
}
