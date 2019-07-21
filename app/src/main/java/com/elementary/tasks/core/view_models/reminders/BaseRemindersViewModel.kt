package com.elementary.tasks.core.view_models.reminders

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.reminder.work.DeleteBackupWorker
import com.elementary.tasks.reminder.work.SingleBackupWorker
import kotlinx.coroutines.runBlocking
import org.koin.core.inject
import timber.log.Timber
import java.util.*

abstract class BaseRemindersViewModel : BaseDbViewModel() {

    private var _defaultReminderGroup: MutableLiveData<ReminderGroup> = MutableLiveData()
    var defaultReminderGroup: LiveData<ReminderGroup> = _defaultReminderGroup

    private var _allGroups: MutableLiveData<List<ReminderGroup>> = MutableLiveData()
    var allGroups: LiveData<List<ReminderGroup>> = _allGroups

    val groups = mutableListOf<ReminderGroup>()
    var defaultGroup: ReminderGroup? = null

    val calendarUtils: CalendarUtils by inject()

    init {
        launchDefault {
            val defGroup = appDb.reminderGroupDao().defaultGroup(true)
            defaultGroup = defGroup
            _defaultReminderGroup.postValue(defGroup)
        }
        appDb.reminderGroupDao().loadAll().observeForever {
            _allGroups.postValue(it)
            if (it != null) {
                groups.clear()
                groups.addAll(it)
            }
        }
    }

    fun saveAndStartReminder(reminder: Reminder, isEdit: Boolean = true) {
        postInProgress(true)
        launchDefault {
            runBlocking {
                Timber.d("saveAndStartReminder: save START")
                if (reminder.groupUuId == "") {
                    val group = appDb.reminderGroupDao().defaultGroup()
                    if (group != null) {
                        reminder.groupColor = group.groupColor
                        reminder.groupTitle = group.groupTitle
                        reminder.groupUuId = group.groupUuId
                    }
                }
                appDb.reminderDao().insert(reminder)
                if (!isEdit) {
                    if (Reminder.isGpsType(reminder.type)) {
                        val places = reminder.places
                        if (places.isNotEmpty()) {
                            appDb.placesDao().insert(places[0])
                        }
                    }
                }
                EventControlFactory.getController(reminder).start()
                Timber.d("saveAndStartReminder: save DONE")
            }
            backupReminder(reminder.uuId)
            postInProgress(false)
            postCommand(Commands.SAVED)
        }
    }

    fun copyReminder(reminder: Reminder, time: Long, name: String) {
        postInProgress(true)
        launchDefault {
            runBlocking {
                if (reminder.groupUuId == "") {
                    val group = appDb.reminderGroupDao().defaultGroup()
                    if (group != null) {
                        reminder.groupColor = group.groupColor
                        reminder.groupTitle = group.groupTitle
                        reminder.groupUuId = group.groupUuId
                    }
                }
                val newItem = reminder.copy()
                newItem.summary = name
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.timeInMillis = time
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                calendar.timeInMillis = TimeUtil.getDateTimeFromGmt(newItem.eventTime)
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                while (calendar.timeInMillis < System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                newItem.eventTime = TimeUtil.getGmtFromDateTime(calendar.timeInMillis)
                newItem.startTime = TimeUtil.getGmtFromDateTime(calendar.timeInMillis)
                appDb.reminderDao().insert(newItem)
                EventControlFactory.getController(newItem).start()
            }
            postCommand(Commands.SAVED)
        }
    }

    fun stopReminder(reminder: Reminder) {
        postInProgress(true)
        launchDefault {
            EventControlFactory.getController(reminder).stop()
            postInProgress(false)
        }
    }

    fun pauseReminder(reminder: Reminder) {
        postInProgress(true)
        launchDefault {
            EventControlFactory.getController(reminder).pause()
            postInProgress(false)
        }
    }

    fun resumeReminder(reminder: Reminder) {
        postInProgress(true)
        launchDefault {
            EventControlFactory.getController(reminder).resume()
            postInProgress(false)
        }
    }

    fun toggleReminder(reminder: Reminder) {
        postInProgress(true)
        launchDefault {
            if (!EventControlFactory.getController(reminder).onOff()) {
                postInProgress(false)
                postCommand(Commands.OUTDATED)
            } else {
                backupReminder(reminder.uuId)
                postInProgress(false)
                postCommand(Commands.SAVED)
            }
        }
    }

    fun moveToTrash(reminder: Reminder) {
        withResult {
            reminder.isRemoved = true
            EventControlFactory.getController(reminder).stop()
            appDb.reminderDao().insert(reminder)
            backupReminder(reminder.uuId)
            Commands.DELETED
        }
    }

    private fun backupReminder(uuId: String) {
        Timber.d("backupReminder: start backup")
        startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, uuId)
    }

    fun deleteReminder(reminder: Reminder, showMessage: Boolean) {
        if (showMessage) {
            withResult {
                EventControlFactory.getController(reminder).stop()
                appDb.reminderDao().delete(reminder)
                calendarUtils.deleteEvents(reminder.uuId)
                startWork(DeleteBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
                Commands.DELETED
            }
        } else {
            withProgress {
                EventControlFactory.getController(reminder).stop()
                appDb.reminderDao().delete(reminder)
                calendarUtils.deleteEvents(reminder.uuId)
                startWork(DeleteBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)
            }
        }
    }

    fun saveReminder(reminder: Reminder, context: Context? = null) {
        postInProgress(true)
        launchDefault {
            runBlocking {
                appDb.reminderDao().insert(reminder)
            }
            if (context != null) UpdatesHelper.updateTasksWidget(context)
            backupReminder(reminder.uuId)
            postInProgress(false)
            postCommand(Commands.SAVED)
        }
    }

    fun skip(reminder: Reminder) {
        withResult {
            val fromDb = appDb.reminderDao().getById(reminder.uuId)
            if (fromDb != null) {
                EventControlFactory.getController(fromDb).skip()
            }
            backupReminder(reminder.uuId)
            Commands.SAVED
        }
    }
}
