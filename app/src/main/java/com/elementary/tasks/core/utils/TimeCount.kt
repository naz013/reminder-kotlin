package com.elementary.tasks.core.utils

import android.app.AlarmManager
import android.content.Context
import android.text.TextUtils
import androidx.annotation.StringRes
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import java.util.*

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
class TimeCount private constructor(context: Context) {

    private val holder: ContextHolder = ContextHolder(context)

    private val context: Context
        get() = holder.context

    fun generateNextTimer(reminder: Reminder, isNew: Boolean): Long {
        val hours = reminder.hours
        val fromHour = reminder.from
        val toHour = reminder.to
        val calendar = Calendar.getInstance()
        if (isNew) {
            calendar.timeInMillis = System.currentTimeMillis() + reminder.after
        } else {
            calendar.timeInMillis = TimeUtil.getDateTimeFromGmt(reminder.eventTime) + reminder.repeatInterval
        }
        var mHour = calendar.get(Calendar.HOUR_OF_DAY)
        if (hours.isNotEmpty()) {
            while (hours.contains(mHour)) {
                calendar.timeInMillis = calendar.timeInMillis + reminder.repeatInterval
                mHour = calendar.get(Calendar.HOUR_OF_DAY)
            }
            return calendar.timeInMillis
        }
        var eventTime = calendar.timeInMillis
        if (fromHour != "" && toHour != "") {
            val fromDate = TimeUtil.getDate(fromHour)
            val toDate = TimeUtil.getDate(toHour)
            if (fromDate != null && toDate != null) {
                calendar.time = fromDate
                var hour = calendar.get(Calendar.HOUR_OF_DAY)
                var minute = calendar.get(Calendar.MINUTE)
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                val start = calendar.timeInMillis
                calendar.time = toDate
                hour = calendar.get(Calendar.HOUR_OF_DAY)
                minute = calendar.get(Calendar.MINUTE)
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                val end = calendar.timeInMillis
                while (isRange(eventTime, start, end)) {
                    eventTime += reminder.repeatInterval
                }
            }
        }
        return eventTime
    }

    private fun isRange(time: Long, start: Long, end: Long): Boolean {
        return if (start > end) {
            time >= start || time < end
        } else {
            time in start..end
        }
    }

    fun generateDateTime(eventTime: String, repeat: Long): Long {
        return if (TextUtils.isEmpty(eventTime)) {
            0
        } else {
            var time = TimeUtil.getDateTimeFromGmt(eventTime)
            while (time < System.currentTimeMillis()) {
                time += repeat
            }
            time
        }
    }

    fun getRemaining(dateTime: String?, delay: Int): String {
        if (TextUtils.isEmpty(dateTime)) {
            return getRemaining(0)
        }
        val time = TimeUtil.getDateTimeFromGmt(dateTime)
        return getRemaining(time + delay * MINUTE)
    }

    fun getRemaining(eventTime: Long): String {
        val difference = eventTime - System.currentTimeMillis()
        val days = difference / DAY
        var hours = (difference - DAY * days) / HOUR
        var minutes = (difference - DAY * days - HOUR * hours) / MINUTE
        hours = if (hours < 0) -hours else hours
        val result = StringBuilder()
        val lang = Locale.getDefault().toString().toLowerCase()
        if (difference > DAY) {
            if (lang.startsWith("uk") || lang.startsWith("ru")) {
                var last = days
                while (last > 10) {
                    last -= 10
                }
                if (last == 1L && days != 11L) {
                    result.append(String.format(getString(R.string.x_day), days.toString()))
                } else if (last < 5 && (days < 12 || days > 14)) {
                    result.append(String.format(getString(R.string.x_dayzz), days.toString()))
                } else {
                    result.append(String.format(getString(R.string.x_days), days.toString()))
                }
            } else {
                if (days < 2) {
                    result.append(String.format(getString(R.string.x_day), days.toString()))
                } else {
                    result.append(String.format(getString(R.string.x_days), days.toString()))
                }
            }
        } else if (difference > HOUR) {
            hours += days * 24
            if (lang.startsWith("uk") || lang.startsWith("ru")) {
                var last = hours
                while (last > 10) {
                    last -= 10
                }
                if (last == 1L && hours != 11L) {
                    result.append(String.format(getString(R.string.x_hour), hours.toString()))
                } else if (last < 5 && (hours < 12 || hours > 14)) {
                    result.append(String.format(getString(R.string.x_hourzz), hours.toString()))
                } else {
                    result.append(String.format(getString(R.string.x_hours), hours.toString()))
                }
            } else {
                if (hours < 2) {
                    result.append(String.format(getString(R.string.x_hour), hours.toString()))
                } else {
                    result.append(String.format(getString(R.string.x_hours), hours.toString()))
                }
            }
        } else if (difference > MINUTE) {
            minutes += hours * 60
            if (lang.startsWith("uk") || lang.startsWith("ru")) {
                var last = minutes
                while (last > 10) {
                    last -= 10
                }
                if (last == 1L && minutes != 11L) {
                    result.append(String.format(getString(R.string.x_minute), minutes.toString()))
                } else if (last < 5 && (minutes < 12 || minutes > 14)) {
                    result.append(String.format(getString(R.string.x_minutezz), minutes.toString()))
                } else {
                    result.append(String.format(getString(R.string.x_minutes), minutes.toString()))
                }
            } else {
                if (hours < 2) {
                    result.append(String.format(getString(R.string.x_minute), minutes.toString()))
                } else {
                    result.append(String.format(getString(R.string.x_minutes), minutes.toString()))
                }
            }
        } else if (difference > 0) {
            result.append(getString(R.string.less_than_minute))
        } else {
            result.append(getString(R.string.overdue))
        }
        return result.toString()
    }

    private fun getString(@StringRes res: Int): String {
        return context.getString(res)
    }

    fun getNextWeekdayTime(startTime: Long, weekdays: List<Int>, delay: Long): Long {
        val cc = Calendar.getInstance()
        cc.timeInMillis = startTime
        cc.set(Calendar.SECOND, 0)
        cc.set(Calendar.MILLISECOND, 0)
        return if (delay > 0) {
            startTime + delay * MINUTE
        } else {
            while (true) {
                val mDay = cc.get(Calendar.DAY_OF_WEEK)
                if (weekdays[mDay - 1] == 1 && cc.timeInMillis > System.currentTimeMillis()) {
                    break
                }
                cc.timeInMillis = cc.timeInMillis + DAY
            }
            cc.timeInMillis
        }
    }

    fun getNextWeekdayTime(reminder: Reminder): Long {
        val weekdays = reminder.weekdays ?: return 0
        val beforeValue = reminder.remindBefore
        val cc = Calendar.getInstance()
        if (reminder.eventTime != "") {
            cc.timeInMillis = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
        }
        cc.set(Calendar.SECOND, 0)
        cc.set(Calendar.MILLISECOND, 0)
        while (true) {
            val mDay = cc.get(Calendar.DAY_OF_WEEK)
            if (weekdays[mDay - 1] == 1 && cc.timeInMillis - beforeValue > System.currentTimeMillis()) {
                break
            }
            cc.timeInMillis = cc.timeInMillis + AlarmManager.INTERVAL_DAY
        }
        return cc.timeInMillis
    }

    fun getNextMonthDayTime(reminder: Reminder): Long {
        val dayOfMonth = reminder.dayOfMonth
        var fromTime = System.currentTimeMillis()
        if (reminder.eventTime != "") {
            fromTime = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
        }
        val beforeValue = reminder.remindBefore
        if (dayOfMonth == 0) {
            return getLastMonthDayTime(fromTime, beforeValue)
        }
        val cc = Calendar.getInstance()
        cc.timeInMillis = fromTime
        cc.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        if (cc.timeInMillis - beforeValue > System.currentTimeMillis()) {
            return cc.timeInMillis
        }
        cc.set(Calendar.MONTH, cc.get(Calendar.MONTH) + 1)
        while (cc.get(Calendar.DAY_OF_MONTH) != dayOfMonth) {
            cc.timeInMillis = cc.timeInMillis + AlarmManager.INTERVAL_DAY
        }
        return cc.timeInMillis
    }

    private fun getLastMonthDayTime(fromTime: Long, beforeValue: Long): Long {
        val cc = Calendar.getInstance()
        cc.timeInMillis = fromTime
        while (true) {
            val lastDay = cc.getActualMaximum(Calendar.DAY_OF_MONTH)
            cc.set(Calendar.DAY_OF_MONTH, lastDay)
            if (cc.timeInMillis - beforeValue > System.currentTimeMillis()) {
                break
            }
            cc.set(Calendar.DAY_OF_MONTH, 1)
            cc.add(Calendar.MONTH, 1)
        }
        cc.set(Calendar.SECOND, 0)
        cc.set(Calendar.MILLISECOND, 0)
        return cc.timeInMillis
    }

    fun getNextDateTime(timeLong: Long): Array<String> {
        val date: String
        val time: String
        if (timeLong == 0L) {
            date = ""
            time = ""
        } else {
            val cl = Calendar.getInstance()
            cl.timeInMillis = timeLong
            val mTime = cl.time
            date = TimeUtil.DATE_FORMAT.format(mTime)
            time = TimeUtil.getTime(mTime, Prefs.getInstance(context).getBoolean(PrefsConstants.IS_24_TIME_FORMAT))
        }
        return arrayOf(date, time)
    }

    fun getNextYearDayTime(reminder: Reminder): Long {
        val dayOfMonth = reminder.dayOfMonth
        val monthOfYear = reminder.monthOfYear
        var fromTime = System.currentTimeMillis()
        val beforeValue = reminder.remindBefore
        if (reminder.eventTime != "") {
            fromTime = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
        }
        val cc = Calendar.getInstance()
        cc.timeInMillis = fromTime
        cc.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        cc.set(Calendar.MONTH, monthOfYear)
        while (cc.timeInMillis - beforeValue < System.currentTimeMillis()) {
            cc.set(Calendar.YEAR, cc.get(Calendar.YEAR) + 1)
        }
        return cc.timeInMillis
    }

    companion object {

        const val SECOND: Long = 1000
        const val MINUTE = 60 * SECOND
        const val HOUR = MINUTE * 60
        private const val HALF_DAY = HOUR * 12
        const val DAY = HALF_DAY * 2
        private var instance: TimeCount? = null

        fun getInstance(context: Context): TimeCount {
            if (instance == null) {
                instance = TimeCount(context.applicationContext)
            }
            return instance!!
        }

        fun isCurrent(eventTime: String?): Boolean {
            return TimeUtil.getDateTimeFromGmt(eventTime) > System.currentTimeMillis()
        }
    }
}
