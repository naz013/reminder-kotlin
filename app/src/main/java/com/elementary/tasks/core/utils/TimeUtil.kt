package com.elementary.tasks.core.utils

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.text.TextUtils
import androidx.annotation.ColorRes
import com.elementary.tasks.R
import hirondelle.date4j.DateTime
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
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

object TimeUtil {

    private const val GMT = "GMT"

    val BIRTH_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val FORMAT_24 = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    private val TIME_STAMP_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ", Locale.getDefault())
    private val FORMAT_12 = SimpleDateFormat("dd MMM yyyy, K:mm a", Locale.getDefault())
    val FULL_DATE_FORMAT = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    private val FULL_DATE_TIME_24 = SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault())
    private val FULL_DATE_TIME_12 = SimpleDateFormat("EEE, dd MMM yyyy K:mm a", Locale.getDefault())
    val DATE_FORMAT = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val TIME_24 = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val TIME_12 = SimpleDateFormat("K:mm a", Locale.getDefault())
    val SIMPLE_DATE = SimpleDateFormat("d MMMM", Locale.getDefault())
    private val SIMPLE_DATE_TIME = SimpleDateFormat("d MMMM, HH:mm", Locale.getDefault())
    private val SIMPLE_DATE_TIME_12 = SimpleDateFormat("d MMMM, K:mm a", Locale.getDefault())

    private val GMT_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.getDefault())
    private val FIRE_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    val birthFormat = SimpleDateFormat("dd|MM", Locale.getDefault())
    private val MONTH_YEAR_FORMAT = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    private val DAY_FORMAT = SimpleDateFormat("dd", Locale.getDefault())
    private val MONTH_FORMAT = SimpleDateFormat("MMM", Locale.getDefault())
    private val YEAR_FORMAT = SimpleDateFormat("yyyy", Locale.getDefault())

    fun getPlaceDateTimeFromGmt(dateTime: String?): DMY {
        var date: Date

        try {
            GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
            date = GMT_DATE_FORMAT.parse(dateTime)
        } catch (e: ParseException) {
            date = Date()
        } catch (e: NumberFormatException) {
            date = Date()
        } catch (e: ArrayIndexOutOfBoundsException) {
            date = Date()
        }

        var day = ""
        var month = ""
        var year = ""

        try {
            day = DAY_FORMAT.format(date)
            month = MONTH_FORMAT.format(date)
            year = YEAR_FORMAT.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }
        return DMY(day, month, year)
    }

    val gmtDateTime: String
        get() {
            GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
            return try {
                GMT_DATE_FORMAT.format(Date())
            } catch (e: Exception) {
                ""
            }
        }

    fun getFireFormatted(prefs: Prefs, gmt: String?): String? {
        if (TextUtils.isEmpty(gmt)) return null
        try {
            FIRE_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
            val date = FIRE_DATE_FORMAT.parse(gmt)
            return if (prefs.is24HourFormatEnabled) {
                FORMAT_24.format(date)
            } else {
                FORMAT_12.format(date)
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }

        return null
    }

    fun showTimePicker(context: Context, theme: Int, is24: Boolean,
                       hour: Int, minute: Int, listener: TimePickerDialog.OnTimeSetListener): TimePickerDialog {
        val dialog = TimePickerDialog(context, theme, listener, hour, minute, is24)
        dialog.show()
        return dialog
    }

    fun showTimePicker(context: Context, is24: Boolean, listener: TimePickerDialog.OnTimeSetListener,
                       hour: Int, minute: Int): TimePickerDialog {
        val dialog = TimePickerDialog(context, listener, hour, minute, is24)
        dialog.show()
        return dialog
    }

    fun showDatePicker(context: Context, theme: Int, prefs: Prefs,
                       year: Int, month: Int, dayOfMonth: Int, listener: DatePickerDialog.OnDateSetListener): DatePickerDialog {
        val dialog = DatePickerDialog(context, theme, listener, year, month, dayOfMonth)
        if (Module.isLollipop) {
            dialog.datePicker.firstDayOfWeek = prefs.startDay + 1
        }
        dialog.show()
        return dialog
    }

    fun showDatePicker(context: Context, prefs: Prefs, listener: DatePickerDialog.OnDateSetListener,
                       year: Int, month: Int, dayOfMonth: Int): DatePickerDialog {
        val dialog = DatePickerDialog(context, listener, year, month, dayOfMonth)
        if (Module.isLollipop) {
            dialog.datePicker.firstDayOfWeek = prefs.startDay + 1
        }
        dialog.show()
        return dialog
    }

    fun getFutureBirthdayDate(birthdayTime: Long, fullDate: String): DateItem? {
        var date: Date? = null
        try {
            date = TimeUtil.BIRTH_DATE_FORMAT.parse(fullDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }

        if (date != null) {
            val calendar = Calendar.getInstance()
            calendar.time = date
            val bDay = calendar.get(Calendar.DAY_OF_MONTH)
            val bMonth = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            calendar.timeInMillis = birthdayTime
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.MONTH, bMonth)
            calendar.set(Calendar.DAY_OF_MONTH, bDay)
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                calendar.add(Calendar.YEAR, 1)
            }
            return DateItem(calendar, year)
        }
        return null
    }

    fun getBirthdayTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        return TIME_24.format(calendar.time)
    }

    fun getBirthdayTime(time: String?): Long {
        val calendar = Calendar.getInstance()
        if (time != null) {
            try {
                val date = TIME_24.parse(time)
                calendar.time = date
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                if (calendar.timeInMillis < System.currentTimeMillis()) {
                    calendar.timeInMillis = calendar.timeInMillis + AlarmManager.INTERVAL_DAY
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            } catch (e: ArrayIndexOutOfBoundsException) {
                e.printStackTrace()
            }

        }
        return calendar.timeInMillis
    }

    fun getBirthdayCalendar(time: String?): Calendar {
        val calendar = Calendar.getInstance()
        if (time != null) {
            try {
                val date = TIME_24.parse(time)
                calendar.time = date
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                if (calendar.timeInMillis < System.currentTimeMillis()) {
                    calendar.timeInMillis = calendar.timeInMillis + AlarmManager.INTERVAL_DAY
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            } catch (e: ArrayIndexOutOfBoundsException) {
                e.printStackTrace()
            }

        }
        return calendar
    }

    fun getGmtFromDateTime(date: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
        return try {
            GMT_DATE_FORMAT.format(calendar.time)
        } catch (e: Exception) {
            ""
        }
    }

    fun getDateTimeFromGmt(dateTime: String?): Long {
        if (TextUtils.isEmpty(dateTime)) {
            return 0
        }
        val calendar = Calendar.getInstance()
        try {
            GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
            val date = GMT_DATE_FORMAT.parse(dateTime)
            calendar.time = date
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }

        return calendar.timeInMillis
    }

    fun getFullDateTime(date: Long, is24: Boolean, isLog: Boolean): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        return if (isLog) {
            TIME_STAMP_FORMAT.format(calendar.time)
        } else {
            if (is24) {
                FULL_DATE_TIME_24.format(calendar.time)
            } else {
                FULL_DATE_TIME_12.format(calendar.time)
            }
        }
    }

    fun getVoiceDateTime(date: String?, is24: Boolean, locale: Int, language: Language): String? {
        if (TextUtils.isEmpty(date)) {
            return null
        }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = getDateTimeFromGmt(date)
        val loc = Locale(language.getTextLanguage(locale))
        var format: DateFormat = SimpleDateFormat("EEEE, MMMM dd yyyy K:mm a", loc)
        if (locale == 0) {
            if (is24) {
                format = SimpleDateFormat("EEEE, MMMM dd yyyy HH:mm", loc)
            }
        } else {
            format = if (is24) {
                SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", loc)
            } else {
                SimpleDateFormat("EEEE, dd MMMM yyyy K:mm a", loc)
            }
        }
        return format.format(calendar.time)
    }

    fun getFullDateTime(date: String?): String {
        if (TextUtils.isEmpty(date)) {
            return "No event time"
        }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = getDateTimeFromGmt(date)
        return TIME_STAMP_FORMAT.format(calendar.time)
    }

    fun getRealDateTime(gmt: String?, delay: Int, is24: Boolean): String {
        if (TextUtils.isEmpty(gmt)) {
            return ""
        }
        val calendar = Calendar.getInstance()
        try {
            GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
            val date = GMT_DATE_FORMAT.parse(gmt)
            calendar.time = date
            calendar.timeInMillis = calendar.timeInMillis + delay * TimeCount.MINUTE
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }

        return if (is24) {
            FULL_DATE_TIME_24.format(calendar.time)
        } else {
            FULL_DATE_TIME_12.format(calendar.time)
        }
    }

    fun getDateTimeFromGmt(dateTime: String?, is24: Boolean): String {
        val calendar = Calendar.getInstance()
        try {
            GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
            val date = GMT_DATE_FORMAT.parse(dateTime)
            calendar.time = date
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }

        return if (is24) {
            FULL_DATE_TIME_24.format(calendar.time)
        } else {
            FULL_DATE_TIME_12.format(calendar.time)
        }
    }

    fun getSimpleDate(gmtDate: String?): String {
        return getSimpleDate(getDateTimeFromGmt(gmtDate))
    }

    fun getSimpleDate(date: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        return SIMPLE_DATE.format(calendar.time)
    }

    fun getMonthYear(date: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        return MONTH_YEAR_FORMAT.format(calendar.time)
    }

    fun getDate(date: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        return DATE_FORMAT.format(calendar.time)
    }

    fun getSimpleDateTime(date: Long, is24: Boolean): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        return if (is24) {
            SIMPLE_DATE_TIME.format(calendar.time)
        } else {
            SIMPLE_DATE_TIME_12.format(calendar.time)
        }
    }

    fun getDate(date: Date): String {
        return FULL_DATE_FORMAT.format(date)
    }

    fun getDate(date: Date, format: DateFormat): String {
        format.timeZone = TimeZone.getDefault()
        return format.format(date)
    }

    fun getDate(date: String): Date? {
        return try {
            TIME_24.parse(date)
        } catch (e: Exception) {
            null
        } catch (e: java.lang.Exception) {
            null
        } catch (e: ArrayIndexOutOfBoundsException) {
            null
        }

    }

    fun getAge(dateOfBirth: String?): Int {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var date: Date? = null
        try {
            date = format.parse(dateOfBirth)
        } catch (e: ParseException) {
            e.printStackTrace()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        if (date != null) {
            calendar.time = date
        }
        val yearOfBirth = calendar.get(Calendar.YEAR)
        val calendar1 = Calendar.getInstance()
        calendar1.timeInMillis
        val currentYear = calendar1.get(Calendar.YEAR)
        return currentYear - yearOfBirth
    }

    fun getDateTime(date: Date, is24: Boolean): String {
        return if (is24) {
            FORMAT_24.format(date)
        } else {
            FORMAT_12.format(date)
        }
    }

    fun getTime(date: Date, is24: Boolean): String {
        return if (is24) {
            TIME_24.format(date)
        } else {
            TIME_12.format(date)
        }
    }

    private fun getAge(year: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        val mYear = calendar.get(Calendar.YEAR)
        return mYear - year
    }

    fun getDate(year: Int, month: Int, day: Int): Date {
        val cal1 = Calendar.getInstance()
        cal1.set(Calendar.YEAR, year)
        cal1.set(Calendar.MONTH, month)
        cal1.set(Calendar.DAY_OF_MONTH, day)
        cal1.set(Calendar.HOUR_OF_DAY, 0)
        cal1.set(Calendar.MINUTE, 0)
        cal1.set(Calendar.SECOND, 0)
        cal1.set(Calendar.MILLISECOND, 0)
        return cal1.time
    }

    fun generateAfterString(time: Long): String {
        val s: Long = 1000
        val m = s * 60
        val h = m * 60
        val hours = time / h
        val minutes = (time - hours * h) / m
        val seconds = (time - hours * h - minutes * m) / s
        val hourStr: String
        hourStr = if (hours < 10) {
            "0$hours"
        } else {
            hours.toString()
        }
        val minuteStr: String
        minuteStr = if (minutes < 10) {
            "0$minutes"
        } else {
            minutes.toString()
        }
        val secondStr: String
        secondStr = if (seconds < 10) {
            "0$seconds"
        } else {
            seconds.toString()
        }
        return hourStr + minuteStr + secondStr
    }

    fun generateViewAfterString(time: Long): String {
        val s: Long = 1000
        val m = s * 60
        val h = m * 60
        val hours = time / h
        val minutes = (time - hours * h) / m
        val seconds = (time - hours * h - minutes * m) / s
        val hourStr: String
        hourStr = if (hours < 10) {
            "0$hours"
        } else {
            hours.toString()
        }
        val minuteStr: String
        minuteStr = if (minutes < 10) {
            "0$minutes"
        } else {
            minutes.toString()
        }
        val secondStr: String
        secondStr = if (seconds < 10) {
            "0$seconds"
        } else {
            seconds.toString()
        }
        return "$hourStr:$minuteStr:$secondStr"
    }

    fun getAgeFormatted(mContext: Context, date: String?): String {
        val years = getAge(date)
        val result = StringBuilder()
        val lang = Locale.getDefault().language.toLowerCase()
        if (lang.startsWith("uk") || lang.startsWith("ru")) {
            var last = years.toLong()
            while (last > 10) {
                last -= 10
            }
            if (last == 1L && years != 11) {
                result.append(String.format(mContext.getString(R.string.x_year), years.toString()))
            } else if (last < 5 && (years < 12 || years > 14)) {
                result.append(String.format(mContext.getString(R.string.x_yearzz), years.toString()))
            } else {
                result.append(String.format(mContext.getString(R.string.x_years), years.toString()))
            }
        } else {
            if (years < 2) {
                result.append(String.format(mContext.getString(R.string.x_year), years.toString()))
            } else {
                result.append(String.format(mContext.getString(R.string.x_years), years.toString()))
            }
        }
        return result.toString()
    }

    fun getAgeFormatted(mContext: Context, date: Int): String {
        val years = getAge(date)
        val result = StringBuilder()
        val lang = Locale.getDefault().toString().toLowerCase()
        if (lang.startsWith("uk") || lang.startsWith("ru")) {
            var last = years.toLong()
            while (last > 10) {
                last -= 10
            }
            if (last == 1L && years != 11) {
                result.append(String.format(mContext.getString(R.string.x_year), years.toString()))
            } else if (last < 5 && (years < 12 || years > 14)) {
                result.append(String.format(mContext.getString(R.string.x_yearzz), years.toString()))
            } else {
                result.append(String.format(mContext.getString(R.string.x_years), years.toString()))
            }
        } else {
            if (years < 2) {
                result.append(String.format(mContext.getString(R.string.x_year), years.toString()))
            } else {
                result.append(String.format(mContext.getString(R.string.x_years), years.toString()))
            }
        }
        return result.toString()
    }

    fun getColor(context: Context, @ColorRes res: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.resources.getColor(res, null)
        } else {
            context.resources.getColor(res)
        }
    }

    fun convertDateTimeToDate(dateTime: DateTime): Date {
        val year = dateTime.year!!
        val datetimeMonth = dateTime.month!!
        val day = dateTime.day!!
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(year, datetimeMonth - 1, day)
        return calendar.time
    }

    fun convertToDateTime(eventTime: Long): DateTime {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.timeInMillis = eventTime
        var year = calendar.get(Calendar.YEAR)
        val javaMonth = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return try {
            DateTime(year, javaMonth + 1, day, 0, 0, 0, 0)
        } catch (e: Exception) {
            calendar.timeInMillis = System.currentTimeMillis()
            year = calendar.get(Calendar.YEAR)
            try {
                DateTime(year, javaMonth + 1, day, 0, 0, 0, 0)
            } catch (e1: Exception) {
                DateTime(year, javaMonth + 1, day - 1, 0, 0, 0, 0)
            }

        }
    }

    data class DateItem(val calendar: Calendar, val year: Int)

    data class DMY(val day: String, val month: String, val year: String)
}
