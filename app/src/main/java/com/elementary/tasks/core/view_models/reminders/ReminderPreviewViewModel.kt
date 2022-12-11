package com.elementary.tasks.core.view_models.reminders

import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.adapter.UiReminderPreviewAdapter
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.ShopItem
import com.elementary.tasks.core.data.ui.UiShareData
import com.elementary.tasks.core.utils.BackupTool
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.WorkManagerProvider
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.view_models.DispatcherProvider
import kotlinx.coroutines.launch
import timber.log.Timber

class ReminderPreviewViewModel(
  id: String,
  private val appDb: AppDb,
  prefs: Prefs,
  calendarUtils: CalendarUtils,
  eventControlFactory: EventControlFactory,
  dispatcherProvider: DispatcherProvider,
  workManagerProvider: WorkManagerProvider,
  private val uiReminderPreviewAdapter: UiReminderPreviewAdapter,
  private val backupTool: BackupTool,
  updatesHelper: UpdatesHelper
) : BaseRemindersViewModel(
  prefs,
  calendarUtils,
  eventControlFactory,
  dispatcherProvider,
  workManagerProvider,
  updatesHelper,
  appDb.reminderDao(),
  appDb.reminderGroupDao(),
  appDb.placesDao()
) {

  private val _note = mutableLiveDataOf<NoteWithImages>()
  val note = _note.toLiveData()

  private val _googleTask = mutableLiveDataOf<Pair<GoogleTaskList?, GoogleTask?>>()
  val googleTask = _googleTask.toLiveData()

  private val _calendarEvent = mutableLiveDataOf<List<CalendarUtils.EventItem>>()
  val calendarEvent = _calendarEvent.toLiveData()

  private val _sharedFile = mutableLiveDataOf<UiShareData>()
  val sharedFile = _sharedFile.toLiveData()

  val clearExtraData = mutableLiveDataOf<Boolean>()
  val reminder = Transformations.map(reminderDao.loadById(id)) {
    uiReminderPreviewAdapter.create(it)
  }

  val db = appDb
  var hasSameInDb: Boolean = false

  fun saveNewShopList(shopList: List<ShopItem>) {
    val reminderId = reminder.value?.id ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderDao.getById(reminderId)?.also {
        saveReminder(it.copy(shoppings = shopList))
      }
    }
  }

  fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminder = reminderDao.getById(id)
      hasSameInDb = reminder != null
    }
  }

  fun loadExtra() {
    val reminder = reminder.value ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      clearExtraData.postValue(true)
      _note.postValue(appDb.notesDao().getById(reminder.noteId))
      val googleTask = appDb.googleTasksDao().getByReminderId(reminder.id)
      if (googleTask != null) {
        _googleTask.postValue(
          Pair(
            appDb.googleTaskListsDao().getById(googleTask.listId),
            googleTask
          )
        )
      }
      val events = calendarUtils.loadEvents(reminder.id)
      if (events.isNotEmpty()) {
        val calendars = calendarUtils.getCalendarsList()
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

  fun deleteEvent(eventItem: CalendarUtils.EventItem) {
    viewModelScope.launch(dispatcherProvider.default()) {
      if (eventItem.localId.isNotBlank()) {
        appDb.calendarEventsDao().deleteById(eventItem.localId)
      }
      calendarUtils.deleteEvent(eventItem.id)
      loadExtra()
    }
  }

  fun toggleReminder() {
    val reminderId = reminder.value?.id ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderDao.getById(reminderId)?.also { toggleReminder(it) }
    }
  }

  fun copyReminder(time: Long, name: String) {
    val reminderId = reminder.value?.id ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderDao.getById(reminderId)?.also { copyReminder(it, time, name) }
    }
  }

  fun deleteReminder(showMessage: Boolean) {
    val reminderId = reminder.value?.id ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderDao.getById(reminderId)?.also { deleteReminder(it, showMessage) }
    }
  }

  fun moveToTrash() {
    val reminderId = reminder.value?.id ?: return
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderDao.getById(reminderId)?.also { moveToTrash(it) }
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
