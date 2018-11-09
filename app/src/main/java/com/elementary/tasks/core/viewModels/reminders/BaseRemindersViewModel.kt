package com.elementary.tasks.core.viewModels.reminders

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.elementary.tasks.R
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.viewModels.BaseDbViewModel
import com.elementary.tasks.core.viewModels.Commands
import com.elementary.tasks.reminder.work.DeleteBackupWorker
import com.elementary.tasks.reminder.work.SingleBackupWorker
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
abstract class BaseRemindersViewModel(application: Application) : BaseDbViewModel(application) {

    var defaultReminderGroup: LiveData<ReminderGroup>
    var allGroups: LiveData<List<ReminderGroup>>

    init {
        defaultReminderGroup = appDb.reminderGroupDao().loadDefault()
        allGroups = appDb.reminderGroupDao().loadAll()
    }

    fun saveAndStartReminder(reminder: Reminder) {
        isInProgress.postValue(true)
        launchDefault {
            appDb.reminderDao().insert(reminder)
            EventControlFactory.getController(reminder).start()
            withUIContext {
                isInProgress.postValue(false)
                result.postValue(Commands.SAVED)
            }
            backupReminder(reminder.uuId)
        }
    }

    fun copyReminder(reminder: Reminder, time: Long, name: String) {
        isInProgress.postValue(true)
        launchDefault {
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
            withUIContext { Toast.makeText(getApplication(), R.string.reminder_created, Toast.LENGTH_SHORT).show() }
        }
    }

    fun stopReminder(reminder: Reminder) {
        isInProgress.postValue(true)
        launchDefault {
            EventControlFactory.getController(reminder).stop()
            withUIContext { isInProgress.postValue(false) }
        }
    }

    fun pauseReminder(reminder: Reminder) {
        isInProgress.postValue(true)
        launchDefault {
            EventControlFactory.getController(reminder).pause()
            withUIContext { isInProgress.postValue(false) }
        }
    }

    fun resumeReminder(reminder: Reminder) {
        isInProgress.postValue(true)
        launchDefault {
            EventControlFactory.getController(reminder).resume()
            withUIContext { isInProgress.postValue(false) }
        }
    }

    fun toggleReminder(reminder: Reminder) {
        isInProgress.postValue(true)
        launchDefault {
            if (!EventControlFactory.getController(reminder).onOff()) {
                withUIContext {
                    isInProgress.postValue(false)
                    Toast.makeText(getApplication(), R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show()
                }
            } else {
                withUIContext {
                    isInProgress.postValue(false)
                    result.postValue(Commands.SAVED)
                }
                backupReminder(reminder.uuId)
            }
        }
    }

    fun moveToTrash(reminder: Reminder) {
        isInProgress.postValue(true)
        launchDefault {
            reminder.isRemoved = true
            EventControlFactory.getController(reminder).stop()
            appDb.reminderDao().insert(reminder)
            withUIContext {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
                Toast.makeText(getApplication(), R.string.deleted, Toast.LENGTH_SHORT).show()
            }
            backupReminder(reminder.uuId)
        }
    }

    private fun backupReminder(uuId: String) {
        val work = OneTimeWorkRequest.Builder(SingleBackupWorker::class.java)
                .setInputData(Data.Builder().putString(Constants.INTENT_ID, uuId).build())
                .addTag(uuId)
                .build()
        WorkManager.getInstance().enqueue(work)
    }

    fun deleteReminder(reminder: Reminder, showMessage: Boolean) {
        isInProgress.postValue(true)
        launchDefault {
            EventControlFactory.getController(reminder).stop()
            appDb.reminderDao().delete(reminder)
            withUIContext {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
                if (showMessage) Toast.makeText(getApplication(), R.string.deleted, Toast.LENGTH_SHORT).show()
            }
            calendarUtils.deleteEvents(reminder.uniqueId)
            val work = OneTimeWorkRequest.Builder(DeleteBackupWorker::class.java)
                    .setInputData(Data.Builder().putString(Constants.INTENT_ID, reminder.uuId).build())
                    .addTag(reminder.uuId)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
    }

    fun saveReminder(reminder: Reminder) {
        isInProgress.postValue(true)
        launchDefault {
            appDb.reminderDao().insert(reminder)
            withUIContext {
                isInProgress.postValue(false)
                result.postValue(Commands.SAVED)
            }
            backupReminder(reminder.uuId)
        }
    }
}
