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

internal class DateEvent(reminder: Reminder) : RepeatableEventManager(reminder) {

    override val isActive: Boolean
        get() = reminder.isActive

    override val isRepeatable: Boolean
        get() = reminder.repeatInterval > 0

    override fun start(): Boolean {
        if (TimeCount.isCurrent(reminder.eventTime)) {
            reminder.isActive = true
            super.save()
            super.enableReminder()
            super.export()
        }
        return true
    }

    override fun skip(): Boolean {
        return false
    }

    override fun next(): Boolean {
        reminder.delay = 0
        if (canSkip()) {
            val time = calculateTime(false)
            reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
            reminder.eventCount = reminder.eventCount + 1
            return start()
        } else {
            return stop()
        }
    }

    override fun onOff(): Boolean {
        if (isActive) {
            return stop()
        } else {
            super.save()
            return start()
        }
    }

    override fun canSkip(): Boolean {
        return isRepeatable && (reminder.repeatLimit == -1 || reminder.repeatLimit.toLong() - reminder.eventCount - 1 > 0)
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
        return TimeCount.getInstance(context).generateDateTime(reminder.eventTime, reminder.repeatInterval)
    }
}