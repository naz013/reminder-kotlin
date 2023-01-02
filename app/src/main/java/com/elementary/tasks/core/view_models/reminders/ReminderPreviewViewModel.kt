package com.elementary.tasks.core.view_models.reminders

import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.adapter.UiReminderPreviewAdapter
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
import com.elementary.tasks.core.data.dao.CalendarEventsDao
import com.elementary.tasks.core.data.dao.GoogleTaskListsDao
import com.elementary.tasks.core.data.dao.GoogleTasksDao
import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.dao.ReminderDao
import com.elementary.tasks.core.data.dao.ReminderGroupDao
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.data.ui.UiShareData
import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.BackupTool
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.reminder.work.ReminderDeleteBackupWorker
import com.elementary.tasks.reminder.work.ReminderSingleBackupWorker
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import timber.log.Timber

class ReminderPreviewViewModel(
  id: String,
  private val reminderDao: ReminderDao,
  private val googleCalendarUtils: GoogleCalendarUtils,
  private val eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val uiReminderPreviewAdapter: UiReminderPreviewAdapter,
  private val backupTool: BackupTool,
  private val updatesHelper: UpdatesHelper,
  private val notesDao: NotesDao,
  private val googleTasksDao: GoogleTasksDao,
  private val googleTaskListsDao: GoogleTaskListsDao,
  private val calendarEventsDao: CalendarEventsDao,
  private val reminderGroupDao: ReminderGroupDao,
  private val dateTimeManager: DateTimeManager,
  private val uiGoogleTaskListAdapter: UiGoogleTaskListAdapter
) : BaseProgressViewModel(dispatcherProvider) {

  private val _note = mutableLiveDataOf<NoteWithImages>()
  val note = _note.toLiveData()

  private val _googleTask = mutableLiveDataOf<UiGoogleTaskList>()
  val googleTask = _googleTask.toLiveData()

  private val _calendarEvent = mutableLiveDataOf<List<GoogleCalendarUtils.EventItem>>()
  val calendarEvent = _calendarEvent.toLiveData()

  private val _sharedFile = mutableLiveDataOf<UiShareData>()
  val sharedFile = _sharedFile.toLiveData()

  val clearExtraData = mutableLiveDataOf<Boolean>()
  val reminder = Transformations.map(reminderDao.loadById(id)) {
    uiReminderPreviewAdapter.create(it)
  }

  fun saveNewShopList(shopList: List<ShopItem>) {
    val reminderId = reminder.value?.id ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderDao.getById(reminderId)?.also {
        saveReminder(it.copy(shoppings = shopList))
      }
    }
  }

  fun saveReminder(reminder: Reminder) {
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      runBlocking {
        reminderDao.insert(reminder)
      }
      updatesHelper.updateTasksWidget()
      workerLauncher.startWork(ReminderSingleBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun loadExtra() {
    val reminder = reminder.value ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      clearExtraData.postValue(true)
      _note.postValue(notesDao.getById(reminder.noteId))
      val googleTask = googleTasksDao.getByReminderId(reminder.id)
      if (googleTask != null) {
        _googleTask.postValue(
          uiGoogleTaskListAdapter.convert(googleTask, googleTaskListsDao.getById(googleTask.listId))
        )
      }
      val events = googleCalendarUtils.loadEvents(reminder.id)
      if (events.isNotEmpty()) {
        val calendars = googleCalendarUtils.getCalendarsList()
        for (c in calendars) {
          for (e in events) {
            if (e.calendarId == c.id) {
              e.calendarName = c.name
            }
          }
        }
        _calendarEvent.postValue(events)
      }
    }
  }

  fun deleteEvent(eventItem: GoogleCalendarUtils.EventItem) {
    viewModelScope.launch(dispatcherProvider.default()) {
      if (eventItem.localId.isNotBlank()) {
        calendarEventsDao.deleteById(eventItem.localId)
      }
      googleCalendarUtils.deleteEvent(eventItem.id)
      loadExtra()
    }
  }

  fun toggleReminder() {
    val reminderId = reminder.value?.id ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderDao.getById(reminderId)?.also { reminder ->
        postInProgress(true)
        if (!eventControlFactory.getController(reminder).onOff()) {
          postInProgress(false)
          postCommand(Commands.OUTDATED)
        } else {
          workerLauncher.startWork(ReminderSingleBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
          postInProgress(false)
          postCommand(Commands.SAVED)
        }
      }
    }
  }

  fun copyReminder(time: LocalTime, name: String) {
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
          newItem.summary = name

          val date = dateTimeManager.fromGmtToLocal(newItem.eventTime)?.toLocalDate()
            ?: LocalDate.now()
          var dateTime = LocalDateTime.of(date, time)

          while (dateTime < LocalDateTime.now()) {
            dateTime = dateTime.plusDays(1)
          }
          newItem.eventTime = dateTimeManager.getGmtFromDateTime(dateTime)
          newItem.startTime = dateTimeManager.getGmtFromDateTime(dateTime)
          reminderDao.insert(newItem)
          eventControlFactory.getController(newItem).start()
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
            eventControlFactory.getController(reminder).stop()
            reminderDao.delete(reminder)
            googleCalendarUtils.deleteEvents(reminder.uuId)
            workerLauncher.startWork(ReminderDeleteBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
            Commands.DELETED
          }
        } else {
          withProgress {
            eventControlFactory.getController(reminder).stop()
            reminderDao.delete(reminder)
            googleCalendarUtils.deleteEvents(reminder.uuId)
            workerLauncher.startWork(ReminderDeleteBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
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
        eventControlFactory.getController(it).stop()
        reminderDao.insert(it)
        workerLauncher.startWork(ReminderSingleBackupWorker::class.java, Constants.INTENT_ID, it.uuId)
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
