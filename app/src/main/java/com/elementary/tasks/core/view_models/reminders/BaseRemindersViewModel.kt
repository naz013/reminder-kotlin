package com.elementary.tasks.core.view_models.reminders

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.elementary.tasks.core.app_widgets.UpdatesHelper
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.*
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.reminder.work.DeleteBackupWorker
import com.elementary.tasks.reminder.work.SingleBackupWorker
import kotlinx.coroutines.runBlocking
import org.koin.standalone.inject
import timber.log.Timber
import java.util.*

/**
 * Copyright 2018 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            withUIContext {  _defaultReminderGroup.postValue(defGroup) }
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
            withUIContext {
                postInProgress(false)
                postCommand(Commands.SAVED)
            }
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
            withUIContext { postCommand(Commands.SAVED) }
        }
    }

    fun stopReminder(reminder: Reminder) {
        postInProgress(true)
        launchDefault {
            EventControlFactory.getController(reminder).stop()
            withUIContext { postInProgress(false) }
        }
    }

    fun pauseReminder(reminder: Reminder) {
        postInProgress(true)
        launchDefault {
            EventControlFactory.getController(reminder).pause()
            withUIContext { postInProgress(false) }
        }
    }

    fun resumeReminder(reminder: Reminder) {
        postInProgress(true)
        launchDefault {
            EventControlFactory.getController(reminder).resume()
            withUIContext { postInProgress(false) }
        }
    }

    fun toggleReminder(reminder: Reminder) {
        postInProgress(true)
        launchDefault {
            if (!EventControlFactory.getController(reminder).onOff()) {
                withUIContext {
                    postInProgress(false)
                    postCommand(Commands.FAILED)
                }
            } else {
                backupReminder(reminder.uuId)
                withUIContext {
                    postInProgress(false)
                    postCommand(Commands.SAVED)
                }
            }
        }
    }

    fun moveToTrash(reminder: Reminder) {
        postInProgress(true)
        launchDefault {
            runBlocking {
                reminder.isRemoved = true
                EventControlFactory.getController(reminder).stop()
                appDb.reminderDao().insert(reminder)
            }
            backupReminder(reminder.uuId)
            withUIContext {
                postInProgress(false)
                postCommand(Commands.DELETED)
            }
        }
    }

    private fun backupReminder(uuId: String) {
        Timber.d("backupReminder: start backup")
        startWork(SingleBackupWorker::class.java, Constants.INTENT_ID, uuId)
    }

    fun deleteReminder(reminder: Reminder, showMessage: Boolean) {
        postInProgress(true)
        launchDefault {
            runBlocking {
                EventControlFactory.getController(reminder).stop()
                appDb.reminderDao().delete(reminder)
                calendarUtils.deleteEvents(reminder.uuId)
            }

            startWork(DeleteBackupWorker::class.java, Constants.INTENT_ID, reminder.uuId)

            withUIContext {
                postInProgress(false)
                if (showMessage) postCommand(Commands.DELETED)
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
            withUIContext {
                postInProgress(false)
                postCommand(Commands.SAVED)
            }
        }
    }

    fun skip(reminder: Reminder) {
        postInProgress(true)
        launchDefault {
            runBlocking {
                val fromDb = appDb.reminderDao().getById(reminder.uuId)
                if (fromDb != null) {
                    EventControlFactory.getController(fromDb).skip()
                }
            }
            backupReminder(reminder.uuId)
            withUIContext {
                postInProgress(false)
                postCommand(Commands.SAVED)
            }
        }
    }
}
