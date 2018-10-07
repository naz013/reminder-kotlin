package com.elementary.tasks.core.controller

import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.Google
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.services.EventJobService
import com.elementary.tasks.core.services.RepeatNotificationReceiver
import com.elementary.tasks.core.utils.TimeUtil

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
            val mItem = GoogleTask()
            mItem.listId = ""
            mItem.status = Google.TASKS_NEED_ACTION
            mItem.title = reminder.summary
            mItem.dueDate = due
            mItem.notes = context.getString(R.string.from_reminder)
            mItem.uuId = reminder.uuId
            // TODO: 23.06.2018 Add export to Google Tasks work via WorkManager
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

    override fun resume(): Boolean {
        if (reminder.isActive) {
            enableReminder()
        }
        return true
    }

    override fun pause(): Boolean {
        notifier.hideNotification(reminder.uniqueId)
        EventJobService.cancelReminder(reminder.uniqueId.toString())
        RepeatNotificationReceiver().cancelAlarm(context, reminder.uniqueId)
        return true
    }

    override fun stop(): Boolean {
        reminder.isActive = false
        save()
        return pause()
    }

    override fun setDelay(delay: Int) {
        EventJobService.enableDelay(delay, reminder.uuId)
    }
}
