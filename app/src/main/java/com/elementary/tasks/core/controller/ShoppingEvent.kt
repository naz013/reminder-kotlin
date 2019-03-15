package com.elementary.tasks.core.controller

import android.text.TextUtils

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
class ShoppingEvent(reminder: Reminder) : RepeatableEventManager(reminder) {

    override val isActive: Boolean
        get() = reminder.isActive

    override fun start(): Boolean {
        return if (reminder.hasReminder) {
            if (!TextUtils.isEmpty(reminder.eventTime) && TimeCount.isCurrent(reminder.eventTime)) {
                reminder.isActive = true
                reminder.isRemoved = false
                super.save()
                super.enableReminder()
                true
            } else {
                false
            }
        } else {
            reminder.isActive = true
            reminder.isRemoved = false
            super.save()
            true
        }
    }

    override fun skip(): Boolean {
        if (canSkip()) {
            val time = TimeCount.generateDateTime(reminder.eventTime, reminder.repeatInterval, TimeUtil.getDateTimeFromGmt(reminder.eventTime))
            reminder.eventTime = TimeUtil.getGmtFromDateTime(time)
            start()
            return true
        }
        return false
    }

    override fun next(): Boolean {
        return stop()
    }

    override fun onOff(): Boolean {
        return if (isActive) {
            stop()
        } else {
            start()
        }
    }

    override fun canSkip(): Boolean {
        return reminder.isRepeating() && (!reminder.isLimited() || !reminder.isLimitExceed())
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
        return TimeCount.generateDateTime(reminder.eventTime, reminder.repeatInterval)
    }
}