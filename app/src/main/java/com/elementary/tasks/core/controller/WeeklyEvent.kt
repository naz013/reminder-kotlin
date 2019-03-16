package com.elementary.tasks.core.controller

import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.TimeCount
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
class WeeklyEvent(reminder: Reminder) : RepeatableEventManager(reminder) {

    override val isActive: Boolean
        get() = reminder.isActive

    override fun start(): Boolean {
        if (TimeCount.isCurrent(reminder.eventTime)) {
            reminder.isActive = true
            reminder.isRemoved = false
            super.save()
            super.enableReminder()
            super.export()
            return true
        }
        return false
    }

    override fun skip(): Boolean {
        if (canSkip()) {
            val time = TimeCount.getNextWeekdayTime(reminder, TimeUtil.getDateTimeFromGmt(reminder.eventTime) + 1000L)
            reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
            start()
            return true
        }
        return false
    }

    override fun next(): Boolean {
        reminder.delay = 0
        return if (canSkip()) {
            val time = calculateTime(false)
            reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
            reminder.eventCount = reminder.eventCount + 1
            start()
        } else {
            stop()
        }
    }

    override fun onOff(): Boolean {
        return if (isActive) {
            stop()
        } else {
            if (!TimeCount.isCurrent(reminder.eventTime)) {
                val time = TimeCount.getNextWeekdayTime(reminder, TimeUtil.getDateTimeFromGmt(reminder.eventTime) - 1000L)
                reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
            }
            reminder.eventCount = 0
            start()
        }
    }

    override fun canSkip(): Boolean {
        return !reminder.isLimited() || !reminder.isLimitExceed()
    }

    override fun setDelay(delay: Int) {
        if (delay == 0) {
            next()
            return
        }
        reminder.delay = delay
        super.save()
        super.setDelay(delay)
    }

    override fun calculateTime(isNew: Boolean): Long {
        return TimeCount.getNextWeekdayTime(reminder)
    }
}