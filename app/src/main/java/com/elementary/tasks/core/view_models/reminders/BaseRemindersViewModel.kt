package com.elementary.tasks.core.view_models.reminders

import android.app.Application
import android.widget.Toast

import com.elementary.tasks.R
import com.elementary.tasks.core.controller.EventControlFactory
import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.view_models.BaseDbViewModel
import com.elementary.tasks.core.view_models.Commands
import com.elementary.tasks.reminder.work.DeleteFilesAsync
import com.elementary.tasks.reminder.work.UpdateFilesAsync

import java.util.ArrayList
import java.util.Calendar
import java.util.stream.Collectors
import androidx.lifecycle.LiveData

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

    var defaultGroup: LiveData<Group>
    var allGroups: LiveData<List<Group>>

    val allGroupsNames: List<String>
        get() {
            val list = ArrayList<String>()
            val groups = allGroups.value
            if (groups != null) {
                list.addAll(groups.stream().map<String>(Function<Group, String> { it.getTitle() }).collect<List<String>, Any>(Collectors.toList()))
            }
            return list
        }

    init {

        defaultGroup = appDb!!.groupDao().loadDefault()
        allGroups = appDb!!.groupDao().loadAll()
    }

    fun saveAndStartReminder(reminder: Reminder) {
        isInProgress.postValue(true)
        run {
            appDb!!.reminderDao().insert(reminder)
            EventControlFactory.getController(reminder).start()
            end {
                isInProgress.postValue(false)
                result.postValue(Commands.SAVED)
            }
            UpdateFilesAsync(getApplication()).execute(reminder)
        }
    }

    fun copyReminder(reminder: Reminder, time: Long, name: String?) {
        if (reminder == null) return
        isInProgress.postValue(true)
        run {
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
            appDb!!.reminderDao().insert(newItem)
            EventControlFactory.getController(newItem).start()
            end { Toast.makeText(getApplication(), R.string.reminder_created, Toast.LENGTH_SHORT).show() }
        }
    }

    fun stopReminder(reminder: Reminder) {
        isInProgress.postValue(true)
        run {
            EventControlFactory.getController(reminder).stop()
            end { isInProgress.postValue(false) }
        }
    }

    fun pauseReminder(reminder: Reminder) {
        isInProgress.postValue(true)
        run {
            EventControlFactory.getController(reminder).pause()
            end { isInProgress.postValue(false) }
        }
    }

    fun resumeReminder(reminder: Reminder) {
        isInProgress.postValue(true)
        run {
            EventControlFactory.getController(reminder).resume()
            end { isInProgress.postValue(false) }
        }
    }

    fun toggleReminder(reminder: Reminder) {
        if (reminder == null) return
        isInProgress.postValue(true)
        run {
            if (!EventControlFactory.getController(reminder).onOff()) {
                end {
                    isInProgress.postValue(false)
                    Toast.makeText(getApplication(), R.string.reminder_is_outdated, Toast.LENGTH_SHORT).show()
                }
            } else {
                end {
                    isInProgress.postValue(false)
                    result.postValue(Commands.SAVED)
                }
                UpdateFilesAsync(getApplication()).execute(reminder)
            }
        }
    }

    fun moveToTrash(reminder: Reminder) {
        isInProgress.postValue(true)
        run {
            reminder.isRemoved = true
            EventControlFactory.getController(reminder).stop()
            appDb!!.reminderDao().insert(reminder)
            end {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
                Toast.makeText(getApplication(), R.string.deleted, Toast.LENGTH_SHORT).show()
            }
            UpdateFilesAsync(getApplication()).execute(reminder)
        }
    }

    fun changeGroup(reminder: Reminder, groupId: String) {
        isInProgress.postValue(true)
        run {
            reminder.groupUuId = groupId
            appDb!!.reminderDao().insert(reminder)
            end {
                isInProgress.postValue(false)
                result.postValue(Commands.SAVED)
            }
            UpdateFilesAsync(getApplication()).execute(reminder)
        }
    }

    fun deleteReminder(reminder: Reminder, showMessage: Boolean) {
        isInProgress.postValue(true)
        run {
            EventControlFactory.getController(reminder).stop()
            appDb!!.reminderDao().delete(reminder)
            end {
                isInProgress.postValue(false)
                result.postValue(Commands.DELETED)
                if (showMessage) Toast.makeText(getApplication(), R.string.deleted, Toast.LENGTH_SHORT).show()
            }
            CalendarUtils.deleteEvents(getApplication(), reminder.uniqueId)
            DeleteFilesAsync(getApplication()).execute(reminder.uuId)
        }
    }

    fun saveReminder(reminder: Reminder) {
        isInProgress.postValue(true)
        run {
            appDb!!.reminderDao().insert(reminder)
            end {
                isInProgress.postValue(false)
                result.postValue(Commands.SAVED)
            }
            UpdateFilesAsync(getApplication()).execute(reminder)
        }
    }
}
