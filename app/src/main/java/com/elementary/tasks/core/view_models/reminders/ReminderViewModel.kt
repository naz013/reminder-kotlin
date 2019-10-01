package com.elementary.tasks.core.view_models.reminders

import androidx.lifecycle.*
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.GoogleTaskList
import com.elementary.tasks.core.data.models.NoteWithImages
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.launchDefault
import timber.log.Timber

class ReminderViewModel private constructor(id: String) : BaseRemindersViewModel() {

    private val _note = MutableLiveData<NoteWithImages>()
    val note: LiveData<NoteWithImages> = _note
    private val _googleTask = MutableLiveData<Pair<GoogleTaskList?, GoogleTask?>>()
    val googleTask: LiveData<Pair<GoogleTaskList?, GoogleTask?>> = _googleTask
    private val _calendarEvent = MutableLiveData<List<CalendarUtils.EventItem>>()
    val calendarEvent: LiveData<List<CalendarUtils.EventItem>> = _calendarEvent
    val clearExtraData = MutableLiveData<Boolean>()

    var hasSameInDb: Boolean = false

    fun findSame(id: String) {
        launchDefault {
            val reminder = appDb.reminderDao().getById(id)
            hasSameInDb = reminder != null
        }
    }

    val reminder = appDb.reminderDao().loadById(id)
    private val mObserver = Observer<Reminder> {
        Timber.d("ReminderViewModel: $it")
    }

    init {
        reminder.observeForever(mObserver)
    }

    fun loadExtra(reminder: Reminder) {
        launchDefault {
            clearExtraData.postValue(true)
            _note.postValue(appDb.notesDao().getById(reminder.noteId))
            val googleTask = appDb.googleTasksDao().getByReminderId(reminder.uuId)
            if (googleTask != null) {
                _googleTask.postValue(Pair(appDb.googleTaskListsDao().getById(googleTask.listId), googleTask))
            }
            val events = calendarUtils.loadEvents(reminder.uuId)
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

    fun deleteEvent(eventItem: CalendarUtils.EventItem, reminder: Reminder) {
        launchDefault {
            if (eventItem.localId.isNotBlank()) {
                appDb.calendarEventsDao().deleteById(eventItem.localId)
            }
            calendarUtils.deleteEvent(eventItem.id)
            loadExtra(reminder)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        reminder.removeObserver(mObserver)
    }

    class Factory(private val id: String) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReminderViewModel(id) as T
        }
    }
}
