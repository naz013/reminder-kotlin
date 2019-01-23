package com.elementary.tasks.core.controller

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.EventJobService
import com.elementary.tasks.core.services.RepeatNotificationReceiver
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.googleTasks.work.SaveNewTaskWorker
import com.elementary.tasks.googleTasks.work.UpdateTaskWorker
import com.google.gson.Gson

/**
 * Copyright 2016 Nazar Suhovich
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
abstract class RepeatableEventManager(reminder: Reminder) : EventManager(reminder) {

    protected fun enableReminder() {
        EventJobService.enableReminder(reminder)
    }

    protected fun export() {
        if (reminder.exportToTasks) {
            val due = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
            val googleTask = GoogleTask()
            googleTask.listId = ""
            googleTask.status = GTasks.TASKS_NEED_ACTION
            googleTask.title = reminder.summary
            googleTask.dueDate = due
            googleTask.notes = context.getString(R.string.from_reminder)
            googleTask.uuId = reminder.uuId
            val work = OneTimeWorkRequest.Builder(SaveNewTaskWorker::class.java)
                    .setInputData(Data.Builder().putString(Constants.INTENT_JSON, Gson().toJson(googleTask)).build())
                    .addTag(reminder.uuId)
                    .build()
            WorkManager.getInstance().enqueue(work)
        }
        if (reminder.exportToCalendar) {
            if (prefs.isStockCalendarEnabled) {
                calendarUtils.addEventToStock(reminder.summary, TimeUtil.getDateTimeFromGmt(reminder.eventTime))
            }
            if (prefs.isCalendarEnabled) {
                calendarUtils.addEvent(reminder)
            }
        }
    }

    private fun makeGoogleTaskDone() {
        if (reminder.exportToTasks) {
            launchIo {
                val googleTask = AppDb.getAppDatabase(context).googleTasksDao().getByReminderId(reminder.uuId)
                if (googleTask != null && googleTask.status == GTasks.TASKS_NEED_ACTION) {
                    val work = OneTimeWorkRequest.Builder(UpdateTaskWorker::class.java)
                            .setInputData(
                                    Data.Builder()
                                            .putString(Constants.INTENT_JSON, Gson().toJson(googleTask))
                                            .putString(Constants.INTENT_STATUS, GTasks.TASKS_COMPLETE)
                                            .build())
                            .addTag(reminder.uuId)
                            .build()
                    WorkManager.getInstance().enqueue(work)
                }
            }
        }
    }

    override fun resume(): Boolean {
        if (reminder.isActive) {
            enableReminder()
        }
        return true
    }

    override fun pause(): Boolean {
        notifier.hideNotification(reminder.uniqueId)
        EventJobService.cancelReminder(reminder.uuId)
        RepeatNotificationReceiver().cancelAlarm(context, reminder.uniqueId)
        return true
    }

    override fun stop(): Boolean {
        reminder.isActive = false
        if (prefs.moveCompleted) {
            reminder.isRemoved = true
        }
        save()
        makeGoogleTaskDone()
        return pause()
    }

    override fun setDelay(delay: Int) {
        EventJobService.enableDelay(delay, reminder.uuId)
    }
}
