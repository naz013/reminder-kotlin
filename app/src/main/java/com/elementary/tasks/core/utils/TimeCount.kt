package com.elementary.tasks.core.utils

import android.app.AlarmManager
import android.content.Context
import android.text.TextUtils
import androidx.annotation.StringRes
import com.elementary.tasks.R
import com.elementary.tasks.core.data.models.Reminder
import timber.log.Timber
import java.util.*

object TimeCount {

    const val SECOND: Long = 1000
    const val MINUTE: Long = 60 * SECOND
    const val HOUR: Long = MINUTE * 60
    private const val HALF_DAY: Long = HOUR * 12
    const val DAY: Long = HALF_DAY * 2
    const val WEEK: Long = DAY * 7

    fun isCurrent(eventTime: String?): Boolean {
        return TimeUtil.getDateTimeFromGmt(eventTime) > System.currentTimeMillis()
    }

    fun isCurrent(millis: Long): Boolean {
        return millis > System.currentTimeMillis()
    }

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

    fun generateDateTime(eventTime: String, repeat: Long, fromTime: Long = System.currentTimeMillis()): Long {
        return if (TextUtils.isEmpty(eventTime)) {
            0
        } else {
            var time = TimeUtil.getDateTimeFromGmt(eventTime)
            while (time <= fromTime) {
                time += repeat
            }
            time
        }
    }

    fun getRemaining(context: Context, dateTime: String?, delay: Int, lang: Int): String {
        if (TextUtils.isEmpty(dateTime)) {
            return getRemaining(context, 0, lang)
        }
        val time = TimeUtil.getDateTimeFromGmt(dateTime)
        return getRemaining(context, time + delay * MINUTE, lang)
    }

    fun getRemaining(context: Context, eventTime: Long, lang: Int = 0): String {
        val difference = eventTime - System.currentTimeMillis()
        val days = difference / DAY
        var hours = (difference - DAY * days) / HOUR
        var minutes = (difference - DAY * days - HOUR * hours) / MINUTE
        hours = if (hours < 0) -hours else hours
        val result = StringBuilder()
        val language = Language.getScreenLanguage(lang).toString().toLowerCase()
        if (difference > DAY) {
            if (language.startsWith("uk") || language.startsWith("ru")) {
                var last = days
                while (last > 10) {
                    last -= 10
                }
                if (last == 1L && days != 11L) {
                    result.append(String.format(getString(context, R.string.x_day), days.toString()))
                } else if (last < 5 && (days < 12 || days > 14)) {
                    result.append(String.format(getString(context, R.string.x_dayzz), days.toString()))
                } else {
                    result.append(String.format(getString(context, R.string.x_days), days.toString()))
                }
            } else {
                if (days < 2) {
                    result.append(String.format(getString(context, R.string.x_day), days.toString()))
                } else {
                    result.append(String.format(getString(context, R.string.x_days), days.toString()))
                }
            }
        } else if (difference > HOUR) {
            hours += days * 24
            if (language.startsWith("uk") || language.startsWith("ru")) {
                var last = hours
                while (last > 10) {
                    last -= 10
                }
                if (last == 1L && hours != 11L) {
                    result.append(String.format(getString(context, R.string.x_hour), hours.toString()))
                } else if (last < 5 && (hours < 12 || hours > 14)) {
                    result.append(String.format(getString(context, R.string.x_hourzz), hours.toString()))
                } else {
                    result.append(String.format(getString(context, R.string.x_hours), hours.toString()))
                }
            } else {
                if (hours < 2) {
                    result.append(String.format(getString(context, R.string.x_hour), hours.toString()))
                } else {
                    result.append(String.format(getString(context, R.string.x_hours), hours.toString()))
                }
            }
        } else if (difference > MINUTE) {
            minutes += hours * 60
            if (language.startsWith("uk") || language.startsWith("ru")) {
                var last = minutes
                while (last > 10) {
                    last -= 10
                }
                if (last == 1L && minutes != 11L) {
                    result.append(String.format(getString(context, R.string.x_minute), minutes.toString()))
                } else if (last < 5 && (minutes < 12 || minutes > 14)) {
                    result.append(String.format(getString(context, R.string.x_minutezz), minutes.toString()))
                } else {
                    result.append(String.format(getString(context, R.string.x_minutes), minutes.toString()))
                }
            } else {
                if (hours < 2) {
                    result.append(String.format(getString(context, R.string.x_minute), minutes.toString()))
                } else {
                    result.append(String.format(getString(context, R.string.x_minutes), minutes.toString()))
                }
            }
        } else if (difference > 0) {
            result.append(getString(context, R.string.less_than_minute))
        } else {
            result.append(getString(context, R.string.overdue))
        }
        return result.toString()
    }

    private fun getString(context: Context, @StringRes res: Int): String {
        return context.getString(res)
    }

    fun getDiffDays(eventTime: Long, base: Long = System.currentTimeMillis()): Long {
        val difference = eventTime - base
        return if (difference >= 0) {
            difference / DAY
        } else {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = eventTime
            calendar.add(Calendar.YEAR, 1)
            getDiffDays(calendar.timeInMillis, base)
        }
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

    fun getNextWeekdayTime(reminder: Reminder, fromTime: Long = System.currentTimeMillis()): Long {
        val weekdays = reminder.weekdays
        val beforeValue = reminder.remindBefore
        val cc = Calendar.getInstance()
        if (reminder.eventTime != "") {
            cc.timeInMillis = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
        }
        cc.set(Calendar.SECOND, 0)
        cc.set(Calendar.MILLISECOND, 0)
        while (true) {
            val mDay = cc.get(Calendar.DAY_OF_WEEK)
            if (weekdays[mDay - 1] == 1 && cc.timeInMillis - beforeValue > fromTime) {
                break
            }
            cc.timeInMillis = cc.timeInMillis + AlarmManager.INTERVAL_DAY
        }
        return cc.timeInMillis
    }

    fun getNextMonthDayTime(reminder: Reminder, fromTime: Long = System.currentTimeMillis()): Long {
        val dayOfMonth = reminder.dayOfMonth
        val beforeValue = reminder.remindBefore

        Timber.d("getNextMonthDayTime: $dayOfMonth, before -> $beforeValue")

        val cc = Calendar.getInstance()
        if (reminder.eventTime != "") {
            cc.timeInMillis = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
            val hour = cc.get(Calendar.HOUR_OF_DAY)
            val minute = cc.get(Calendar.MINUTE)
            cc.timeInMillis = fromTime
            cc.set(Calendar.HOUR_OF_DAY, hour)
            cc.set(Calendar.MINUTE, minute)
        }
        if (dayOfMonth == 0) {
            return getLastMonthDayTime(fromTime, reminder)
        } else if (dayOfMonth > 28) {
            return getSmartMonthDayTime(fromTime, reminder)
        }

        cc.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        cc.set(Calendar.SECOND, 0)
        cc.set(Calendar.MILLISECOND, 0)
        var interval = reminder.repeatInterval.toInt()
        if (interval <= 0) {
            interval = 1
        }
        var isAfter = cc.timeInMillis - beforeValue > fromTime
        if (cc.get(Calendar.DAY_OF_MONTH) == dayOfMonth && isAfter) {
            cc.set(Calendar.SECOND, 0)
            cc.set(Calendar.MILLISECOND, 0)
            return cc.timeInMillis
        }
        while (true) {
            isAfter = cc.timeInMillis - beforeValue > fromTime
            if (cc.get(Calendar.DAY_OF_MONTH) == dayOfMonth && isAfter) {
                break
            }
            cc.add(Calendar.MONTH, interval)
        }
        cc.set(Calendar.SECOND, 0)
        cc.set(Calendar.MILLISECOND, 0)
        return cc.timeInMillis
    }

    private fun getSmartMonthDayTime(fromTime: Long, reminder: Reminder): Long {
        val dayOfMonth = reminder.dayOfMonth
        val beforeValue = reminder.remindBefore

        val cc = Calendar.getInstance()
        if (reminder.eventTime != "") {
            cc.timeInMillis = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
            val hour = cc.get(Calendar.HOUR_OF_DAY)
            val minute = cc.get(Calendar.MINUTE)
            cc.timeInMillis = fromTime
            cc.set(Calendar.HOUR_OF_DAY, hour)
            cc.set(Calendar.MINUTE, minute)
        }
        cc.set(Calendar.SECOND, 0)
        cc.set(Calendar.MILLISECOND, 0)
        var interval = reminder.repeatInterval.toInt()
        if (interval <= 0) {
            interval = 1
        }
        var lastDay = cc.getActualMaximum(Calendar.DAY_OF_MONTH)
        if (dayOfMonth <= lastDay) {
            cc.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        } else {
            cc.set(Calendar.DAY_OF_MONTH, lastDay)
        }
        var isAfter = cc.timeInMillis - beforeValue > fromTime
        if (isAfter) {
            cc.set(Calendar.SECOND, 0)
            cc.set(Calendar.MILLISECOND, 0)
            return cc.timeInMillis
        }
        while (true) {
            isAfter = cc.timeInMillis - beforeValue > fromTime
            if (isAfter) {
                break
            }
            cc.set(Calendar.DAY_OF_MONTH, 1)
            cc.add(Calendar.MONTH, interval)
            lastDay = cc.getActualMaximum(Calendar.DAY_OF_MONTH)
            if (dayOfMonth <= lastDay) {
                cc.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            } else {
                cc.set(Calendar.DAY_OF_MONTH, lastDay)
            }
        }
        cc.set(Calendar.SECOND, 0)
        cc.set(Calendar.MILLISECOND, 0)
        return cc.timeInMillis
    }

    private fun getLastMonthDayTime(fromTime: Long, reminder: Reminder): Long {
        val cc = Calendar.getInstance()
        if (reminder.eventTime != "") {
            cc.timeInMillis = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
            val hour = cc.get(Calendar.HOUR_OF_DAY)
            val minute = cc.get(Calendar.MINUTE)
            cc.timeInMillis = fromTime
            cc.set(Calendar.HOUR_OF_DAY, hour)
            cc.set(Calendar.MINUTE, minute)
        }
        cc.set(Calendar.SECOND, 0)
        cc.set(Calendar.MILLISECOND, 0)
        var interval = reminder.repeatInterval.toInt()
        if (interval <= 0) {
            interval = 1
        }
        while (true) {
            val lastDay = cc.getActualMaximum(Calendar.DAY_OF_MONTH)
            cc.set(Calendar.DAY_OF_MONTH, lastDay)
            if (cc.timeInMillis - reminder.remindBefore > fromTime) {
                break
            }
            cc.set(Calendar.DAY_OF_MONTH, 1)
            cc.add(Calendar.MONTH, interval)
        }
        cc.set(Calendar.SECOND, 0)
        cc.set(Calendar.MILLISECOND, 0)
        return cc.timeInMillis
    }

    fun getNextDateTime(timeLong: Long, prefs: Prefs): Array<String> {
        val date: String
        val time: String
        if (timeLong == 0L) {
            date = ""
            time = ""
        } else {
            val cl = Calendar.getInstance()
            cl.timeInMillis = timeLong
            val mTime = cl.time
            date = TimeUtil.date(prefs.appLanguage).format(mTime)
            time = TimeUtil.getTime(mTime, prefs.is24HourFormat, prefs.appLanguage)
        }
        return arrayOf(date, time)
    }

    fun getNextYearDayTime(reminder: Reminder, fromTime: Long = System.currentTimeMillis()): Long {
        val dayOfMonth = reminder.dayOfMonth
        val monthOfYear = reminder.monthOfYear
        val beforeValue = reminder.remindBefore
        val cc = Calendar.getInstance()
        if (reminder.eventTime != "") {
            cc.timeInMillis = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
            val hour = cc.get(Calendar.HOUR_OF_DAY)
            val minute = cc.get(Calendar.MINUTE)
            cc.timeInMillis = fromTime
            cc.set(Calendar.HOUR_OF_DAY, hour)
            cc.set(Calendar.MINUTE, minute)
        }
        cc.set(Calendar.MONTH, monthOfYear)
        var lastDay = cc.getActualMaximum(Calendar.DAY_OF_MONTH)
        if (dayOfMonth <= lastDay) {
            cc.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        } else {
            cc.set(Calendar.DAY_OF_MONTH, lastDay)
        }
        var isAfter = cc.timeInMillis - beforeValue > fromTime
        if (isAfter) {
            cc.set(Calendar.SECOND, 0)
            cc.set(Calendar.MILLISECOND, 0)
            return cc.timeInMillis
        }
        while (true) {
            isAfter = cc.timeInMillis - beforeValue > fromTime
            if (isAfter) {
                break
            }
            cc.set(Calendar.DAY_OF_MONTH, 1)
            cc.add(Calendar.YEAR, 1)
            lastDay = cc.getActualMaximum(Calendar.DAY_OF_MONTH)
            if (dayOfMonth <= lastDay) {
                cc.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            } else {
                cc.set(Calendar.DAY_OF_MONTH, lastDay)
            }
        }
        cc.set(Calendar.SECOND, 0)
        cc.set(Calendar.MILLISECOND, 0)
        return cc.timeInMillis
    }
}
