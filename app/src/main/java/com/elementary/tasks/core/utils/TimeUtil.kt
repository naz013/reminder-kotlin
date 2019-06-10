package com.elementary.tasks.core.utils

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.TextUtils
import com.crashlytics.android.Crashlytics
import com.elementary.tasks.R
import hirondelle.date4j.DateTime
import timber.log.Timber
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object TimeUtil {

    private const val GMT = "GMT"

    val BIRTH_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val BIRTH_FORMAT = SimpleDateFormat("dd|MM", Locale.US)
    private val GMT_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.US)
    private val FIRE_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val TIME_24 = SimpleDateFormat("HH:mm", Locale.US)

    private fun localizedDateFormat(pattern: String, lang: Int = 0): SimpleDateFormat = SimpleDateFormat(pattern, Language.getScreenLanguage(lang))

    private fun dateTime24(lang: Int = 0): SimpleDateFormat = localizedDateFormat("dd MMM yyyy, HH:mm", lang)

    private fun dateTime12(lang: Int = 0): SimpleDateFormat = localizedDateFormat("dd MMM yyyy, hh:mm a", lang)

    fun fullDate(lang: Int = 0): SimpleDateFormat = localizedDateFormat("EEE, dd MMM yyyy", lang)

    private fun fullDateTime24(lang: Int = 0): SimpleDateFormat = localizedDateFormat("EEE, dd MMM yyyy HH:mm", lang)

    private fun fullDateTime12(lang: Int = 0): SimpleDateFormat = localizedDateFormat("EEE, dd MMM yyyy hh:mm a", lang)

    private fun time24(lang: Int = 0): SimpleDateFormat = localizedDateFormat("HH:mm", lang)

    private fun time12(lang: Int = 0): SimpleDateFormat = localizedDateFormat("hh:mm a", lang)

    fun simpleDate(lang: Int = 0): SimpleDateFormat = localizedDateFormat("d MMMM", lang)

    private fun simpleDateTime24(lang: Int = 0): SimpleDateFormat = localizedDateFormat("d MMMM, HH:mm", lang)

    private fun simpleDateTime12(lang: Int = 0): SimpleDateFormat = localizedDateFormat("d MMMM, hh:mm a", lang)

    fun date(lang: Int = 0): SimpleDateFormat = localizedDateFormat("dd MMM yyyy", lang)

    fun day(lang: Int = 0): SimpleDateFormat = localizedDateFormat("dd", lang)

    fun month(lang: Int = 0): SimpleDateFormat = localizedDateFormat("MMM", lang)

    fun year(lang: Int = 0): SimpleDateFormat = localizedDateFormat("yyyy", lang)

    fun getBirthdayDayMonth(millis: Long = System.currentTimeMillis()): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        return "$day|$month"
    }

    fun getDayStart(millis: Long = System.currentTimeMillis()): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return getGmtFromDateTime(calendar.timeInMillis)
    }

    fun getDayEnd(millis: Long = System.currentTimeMillis()): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis + AlarmManager.INTERVAL_DAY
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return getGmtFromDateTime(calendar.timeInMillis)
    }

    fun getPlaceDateTimeFromGmt(dateTime: String?, lang: Int = 0): DMY {
        var date: Date

        if (dateTime != null) {
            try {
                GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
                date = GMT_DATE_FORMAT.parse(dateTime) ?: Date()
            } catch (e: ParseException) {
                date = Date()
            } catch (e: NumberFormatException) {
                date = Date()
            } catch (e: ArrayIndexOutOfBoundsException) {
                date = Date()
            }
        } else {
            date = Date()
        }

        var day = ""
        var month = ""
        var year = ""

        try {
            day = TimeUtil.day(lang).format(date)
            month = TimeUtil.month(lang).format(date)
            year = TimeUtil.year(lang).format(date)
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
        if (gmt.isNullOrEmpty()) return null
        try {
            FIRE_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
            val date = FIRE_DATE_FORMAT.parse(gmt) ?: return null
            return if (prefs.is24HourFormat) {
                dateTime24(prefs.appLanguage).format(date)
            } else {
                dateTime12(prefs.appLanguage).format(date)
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

    fun showTimePicker(context: Context, is24: Boolean, hour: Int, minute: Int,
                       listener: TimePickerDialog.OnTimeSetListener): TimePickerDialog {
        val dialog = TimePickerDialog(context, listener, hour, minute, is24)
        dialog.show()
        return dialog
    }

    fun showTimePicker(context: Context, is24: Boolean, listener: TimePickerDialog.OnTimeSetListener,
                       hour: Int, minute: Int): TimePickerDialog {
        val dialog = TimePickerDialog(context, listener, hour, minute, is24)
        dialog.show()
        return dialog
    }

    fun showDatePicker(context: Context, prefs: Prefs, year: Int, month: Int, dayOfMonth: Int,
                       listener: DatePickerDialog.OnDateSetListener): DatePickerDialog {
        val dialog = DatePickerDialog(context, listener, year, month, dayOfMonth)
        dialog.datePicker.firstDayOfWeek = prefs.startDay + 1
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
            return DateItem(calendar.timeInMillis, year)
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

    fun getBirthdayVisualTime(time: String?, is24: Boolean, lang: Int = 0): String {
        if (time != null) {
            try {
                val date = TIME_24.parse(time) ?: return ""
                return if (is24) {
                    time24(lang).format(date)
                } else {
                    time12(lang).format(date)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return ""
    }

    fun millisToEndDnd(from: String?, to: String?, current: Long): Long {
        return doNotDisturbRange(from, to).last - current
    }

    fun doNotDisturbRange(from: String?, to: String?): LongRange {
        var fromMillis = 0L
        var toMillis = 0L
        if (from != null) {
            fromMillis = toMillis(from)
        }
        if (to != null) {
            toMillis = toMillis(to)
        }
        val fromHm = hourMinute(fromMillis)
        val toHm = hourMinute(toMillis)
        Timber.d("doNotDisturbRange: HM $fromHm, $toHm")
        val compare = compareHm(fromHm, toHm)
        if (compare < 0) {
            if (toMillis < fromMillis) {
                toMillis += AlarmManager.INTERVAL_DAY
            }
        } else if (compare == 0) {
            return LongRange(0, 0)
        }
        Timber.d("doNotDisturbRange: millis $fromMillis, $toMillis")
        return if (fromMillis > toMillis) {
            LongRange(toMillis, fromMillis)
        } else {
            LongRange(fromMillis, toMillis)
        }
    }

    private fun compareHm(from: Pair<Int, Int>, to: Pair<Int, Int>): Int {
        return when {
            from.first == to.first -> when {
                from.second == to.second -> 0
                from.second > to.second -> -1
                else -> 1
            }
            from.first > to.first -> -1
            else -> 1
        }
    }

    private fun hourMinute(millis: Long): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        return Pair(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
        )
    }

    private fun toMillis(time24: String): Long {
        return try {
            val calendar = Calendar.getInstance()
            val date = TIME_24.parse(time24) ?: return 0
            calendar.time = date
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        } catch (e: Exception) {
            0
        }
    }

    fun getBirthdayTime(time: String?): Long {
        if (time != null) {
            var millis = toMillis(time)
            if (millis < System.currentTimeMillis()) {
                millis += AlarmManager.INTERVAL_DAY
            }
            return millis
        }
        return System.currentTimeMillis()
    }

    fun getBirthdayCalendar(time: String?): Calendar {
        val calendar = Calendar.getInstance()
        if (time != null) {
            var millis = toMillis(time)
            if (millis < System.currentTimeMillis()) {
                millis += AlarmManager.INTERVAL_DAY
            }
            calendar.timeInMillis = millis
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
        if (dateTime.isNullOrEmpty()) {
            return 0
        }
        val calendar = Calendar.getInstance()
        try {
            GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
            val date = GMT_DATE_FORMAT.parse(dateTime) ?: return 0
            calendar.time = date
        } catch (e: Exception) {
            Crashlytics.logException(e)
            return 0
        }

        return calendar.timeInMillis
    }

    fun logTime(date: Long = System.currentTimeMillis()): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(date))
    }

    fun getFullDateTime(date: Long, is24: Boolean, lang: Int = 0): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        return if (is24) {
            fullDateTime24(lang).format(calendar.time)
        } else {
            fullDateTime12(lang).format(calendar.time)
        }
    }

    fun getVoiceDateTime(date: String?, is24: Boolean, locale: Int, language: Language): String? {
        if (TextUtils.isEmpty(date)) {
            return null
        }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = getDateTimeFromGmt(date)
        val loc = Locale(language.getTextLanguage(locale))
        val format = if (locale == 0) {
            if (is24) {
                SimpleDateFormat("EEEE, MMMM dd yyyy HH:mm", loc)
            } else {
                SimpleDateFormat("EEEE, MMMM dd yyyy hh:mm a", loc)
            }
        } else {
            if (is24) {
                SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", loc)
            } else {
                SimpleDateFormat("EEEE, dd MMMM yyyy hh:mm a", loc)
            }
        }
        return format.format(calendar.time)
    }

    fun getRealDateTime(gmt: String?, delay: Int, is24: Boolean, lang: Int = 0): String {
        if (gmt.isNullOrEmpty()) {
            return ""
        }
        val calendar = Calendar.getInstance()
        try {
            GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
            val date = GMT_DATE_FORMAT.parse(gmt) ?: return ""
            calendar.time = date
            calendar.timeInMillis = calendar.timeInMillis + delay * TimeCount.MINUTE
        } catch (e: Exception) {
            Crashlytics.logException(e)
            return ""
        }

        return if (is24) {
            fullDateTime24(lang).format(calendar.time)
        } else {
            fullDateTime12(lang).format(calendar.time)
        }
    }

    fun getDateTimeFromGmt(dateTime: String?, is24: Boolean, lang: Int = 0): String {
        if (dateTime.isNullOrEmpty()) return ""
        val calendar = Calendar.getInstance()
        try {
            GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
            val date = GMT_DATE_FORMAT.parse(dateTime) ?: return ""
            calendar.time = date
        } catch (e: Exception) {
            Crashlytics.logException(e)
        }

        return if (is24) {
            fullDateTime24(lang).format(calendar.time)
        } else {
            fullDateTime12(lang).format(calendar.time)
        }
    }

    fun getSimpleDate(gmtDate: String?, lang: Int = 0): String {
        return getSimpleDate(getDateTimeFromGmt(gmtDate), lang)
    }

    fun getSimpleDate(date: Long, lang: Int = 0): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        return simpleDate(lang).format(calendar.time)
    }

    fun getDate(date: Long, lang: Int = 0): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        return date(lang).format(calendar.time)
    }

    fun getSimpleDateTime(date: Long, is24: Boolean, lang: Int = 0): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        return if (is24) {
            simpleDateTime24(lang).format(calendar.time)
        } else {
            simpleDateTime12(lang).format(calendar.time)
        }
    }

    private fun getGoogleTaskDate(date: Date, lang: Int = 0): String {
        return fullDate(lang).format(date)
    }

    fun getGoogleTaskDate(millis: Long, lang: Int = 0): String {
        return getGoogleTaskDate(Date(millis), lang)
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
        }
    }

    fun getReadableBirthDate(dateOfBirth: String?, lang: Int = 0): String {
        if (dateOfBirth.isNullOrEmpty()) return ""
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var date: Date? = null
        try {
            date = format.parse(dateOfBirth)
        } catch (e: Exception) {
            Crashlytics.logException(e)
        }

        return if (date != null) {
            TimeUtil.date(lang).format(date)
        } else {
            dateOfBirth
        }
    }

    fun getAge(dateOfBirth: String?): Int {
        if (dateOfBirth.isNullOrEmpty()) return 0
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var date: Date? = null
        try {
            date = format.parse(dateOfBirth)
        } catch (e: Exception) {
            Crashlytics.logException(e)
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

    fun getDateTime(date: Date, is24: Boolean, lang: Int = 0): String {
        return if (is24) {
            dateTime24(lang).format(date)
        } else {
            dateTime12(lang).format(date)
        }
    }

    fun getTime(date: Date, is24: Boolean, lang: Int = 0): String {
        return if (is24) {
            time24(lang).format(date)
        } else {
            time12(lang).format(date)
        }
    }

    fun getTime(millis: Long, is24: Boolean, lang: Int = 0): String {
        return getTime(Date(millis), is24, lang)
    }

    private fun getAge(year: Int, at: Long = System.currentTimeMillis()): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = at
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

    fun getAgeFormatted(mContext: Context, date: String?, lang: Int = 0): String {
        val years = getAge(date)
        val result = StringBuilder()
        val language = Language.getScreenLanguage(lang).language.toLowerCase()
        if (language.startsWith("uk") || language.startsWith("ru")) {
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

    fun getAgeFormatted(mContext: Context, yearOfBirth: Int, at: Long = System.currentTimeMillis(), lang: Int = 0): String {
        val years = getAge(yearOfBirth, at)
        val result = StringBuilder()
        val language = Language.getScreenLanguage(lang).toString().toLowerCase()
        if (language.startsWith("uk") || language.startsWith("ru")) {
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

    fun convertDateTimeToDate(dateTime: DateTime): Date {
        val year = dateTime.year
        val datetimeMonth = dateTime.month
        val day = dateTime.day
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
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

    data class DateItem(val millis: Long, val year: Int)

    data class DMY(val day: String, val month: String, val year: String)

    data class HM(val hour: Int, val minute: Int)
}
