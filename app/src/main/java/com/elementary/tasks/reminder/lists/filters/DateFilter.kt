package com.elementary.tasks.reminder.lists.filters

import android.app.AlarmManager

import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.data.models.Reminder

import java.util.Calendar

/**
 * Copyright 2017 Nazar Suhovich
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
class DateFilter internal constructor(filter: ObjectFilter<Reminder>?) : AbstractFilter<DateFilter.DateRange, Reminder>(filter) {

    private var range = DateRange.ALL

    override fun filter(reminder: Reminder): Boolean {
        if (!super.filter(reminder)) return false
        return if (range == DateRange.ALL)
            true
        else {
            when (range) {
                DateFilter.DateRange.PERMANENT -> reminder.eventTime == ""
                DateFilter.DateRange.TODAY -> compareToday(reminder.eventTime)
                DateFilter.DateRange.TOMORROW -> compareTomorrow(reminder.eventTime)
                else -> reminder.eventTime == ""
            }
        }
    }

    private fun compareTomorrow(time: String?): Boolean {
        return compareDay(time, 1)
    }

    private fun compareToday(time: String?): Boolean {
        return compareDay(time, 0)
    }

    private fun compareDay(time: String?, daysAfter: Int): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis() + daysAfter * AlarmManager.INTERVAL_DAY
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = TimeUtil.getGmtFromDateTime(calendar.timeInMillis)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = TimeUtil.getGmtFromDateTime(calendar.timeInMillis)
        if (time == null) return false
        val st = time.compareTo(start)
        val ed = time.compareTo(end)
        return st >= 0 && ed <= 0
    }

    @Throws(Exception::class)
    override fun accept(value: DateRange) {
        this.range = value
    }

    enum class DateRange {
        ALL,
        PERMANENT,
        TODAY,
        TOMORROW
    }
}
