package com.elementary.tasks.core.utils

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.TextUtils
import com.elementary.tasks.R
import com.github.naz013.calendarext.dropMilliseconds
import com.github.naz013.calendarext.dropSeconds
import com.github.naz013.calendarext.getDayOfMonth
import com.github.naz013.calendarext.getHourOfDay
import com.github.naz013.calendarext.getMinute
import com.github.naz013.calendarext.getMonth
import com.github.naz013.calendarext.getYear
import com.github.naz013.calendarext.newCalendar
import com.github.naz013.calendarext.setDate
import com.github.naz013.calendarext.setHourOfDay
import com.github.naz013.calendarext.setMinute
import com.github.naz013.calendarext.setTime
import com.github.naz013.calendarext.toCalendar
import com.github.naz013.calendarext.toDate
import com.github.naz013.calendarext.toDateWithException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hirondelle.date4j.DateTime
import timber.log.Timber
import java.text.DateFormat
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

  private fun dateTime12(lang: Int = 0): SimpleDateFormat = localizedDateFormat("dd MMM yyyy, h:mm a", lang)

  fun fullDate(lang: Int = 0): SimpleDateFormat = localizedDateFormat("EEE, dd MMM yyyy", lang)

  private fun fullDateTime24(lang: Int = 0): SimpleDateFormat = localizedDateFormat("EEE, dd MMM yyyy HH:mm", lang)

  private fun fullDateTime12(lang: Int = 0): SimpleDateFormat = localizedDateFormat("EEE, dd MMM yyyy h:mm a", lang)

  private fun time24(lang: Int = 0): SimpleDateFormat = localizedDateFormat("HH:mm", lang)

  private fun time12(lang: Int = 0): SimpleDateFormat = localizedDateFormat("h:mm a", lang)

  fun simpleDate(lang: Int = 0): SimpleDateFormat = localizedDateFormat("d MMMM", lang)

  fun date(lang: Int = 0): SimpleDateFormat = localizedDateFormat("dd MMM yyyy", lang)

  fun day(lang: Int = 0): SimpleDateFormat = localizedDateFormat("dd", lang)

  fun month(lang: Int = 0): SimpleDateFormat = localizedDateFormat("MMM", lang)

  fun year(lang: Int = 0): SimpleDateFormat = localizedDateFormat("yyyy", lang)

  fun getBirthdayDayMonthList(start: Long = System.currentTimeMillis(), duration: Int = 1): List<String> {
    val list = mutableListOf<String>()
    val calendar = newCalendar()
    for (n in 0 until duration) {
      calendar.timeInMillis = start + (AlarmManager.INTERVAL_DAY * n)
      list.add("${calendar.getDayOfMonth()}|${calendar.getMonth()}")
    }
    Timber.d("getBirthdayDayMonthList: $list")
    return list
  }

  fun getDayStart(millis: Long = System.currentTimeMillis()): String {
    return newCalendar(millis).apply {
      this.setHourOfDay(0)
      this.setMinute(0)
      this.dropSeconds()
      this.dropMilliseconds()
    }.map { getGmtFromDateTime(it.timeInMillis) }
  }

  fun getDayEnd(millis: Long = System.currentTimeMillis()): String {
    return getDayStart(millis + AlarmManager.INTERVAL_DAY)
  }

  fun getPlaceDateTimeFromGmt(dateTime: String?, lang: Int = 0): DMY {
    val date = dateTime?.toDate(GMT_DATE_FORMAT, TimeZone.getTimeZone(GMT)) ?: Date()
    return try {
      DMY(day(lang).format(date), month(lang).format(date), year(lang).format(date))
    } catch (e: Exception) {
      DMY()
    }
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

  fun getFireMillis(gmt: String?): Long {
    if (gmt.isNullOrEmpty()) return 0
    try {
      FIRE_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
      val date = FIRE_DATE_FORMAT.parse(gmt) ?: return 0
      return date.time
    } catch (e: Exception) {
      return 0
    }
  }

  fun getFireFormatted(prefs: Prefs, gmt: String?): String? {
    return gmt?.toDate(FIRE_DATE_FORMAT, TimeZone.getTimeZone(GMT)).takeIf {
      it != null
    }?.let {
      if (prefs.is24HourFormat) {
        dateTime24(prefs.appLanguage).format(it)
      } else {
        dateTime12(prefs.appLanguage).format(it)
      }
    }
  }

  fun showTimePicker(context: Context, is24: Boolean, hour: Int, minute: Int,
                     listener: TimePickerDialog.OnTimeSetListener): TimePickerDialog {
    val dialog = TimePickerDialog(context, listener, hour, minute, is24)
    dialog.show()
    return dialog
  }

  fun showTimePicker(context: Context, is24: Boolean, old: Calendar?,
                     listener: (Calendar) -> Unit): TimePickerDialog {
    val calendar = old ?: newCalendar()
    val dialog = TimePickerDialog(
      context,
      { _, hourOfDay, minute ->
        newCalendar()
          .setTime(hourOfDay, minute)
          .also { listener.invoke(it) }
      },
      calendar.getHourOfDay(),
      calendar.getMinute(),
      is24
    )
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

  fun showDatePicker(context: Context, prefs: Prefs, old: Calendar?,
                     listener: (Calendar) -> Unit): DatePickerDialog {
    val calendar = old ?: newCalendar()
    val dialog = DatePickerDialog(
      context,
      { _, year, monthOfYear, dayOfMonth ->
        newCalendar()
          .setDate(year, monthOfYear, dayOfMonth)
          .also { listener.invoke(it) }
      },
      calendar.getYear(),
      calendar.getMonth(),
      calendar.getDayOfMonth()
    )
    dialog.datePicker.firstDayOfWeek = prefs.startDay + 1
    dialog.show()
    return dialog
  }

  fun getFutureBirthdayDate(birthdayTime: Long, fullDate: String): DateItem? {
    return fullDate.toDate(BIRTH_DATE_FORMAT).let { date ->
      val calendar = newCalendar(date)
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
      DateItem(calendar.timeInMillis, year)
    }
  }

  fun getBirthdayTime(hour: Int, minute: Int): String {
    return newCalendar().apply {
      this.setTime(hour, minute)
    }.map { TIME_24.format(it.time) }
  }

  fun getBirthdayVisualTime(time: String?, is24: Boolean, lang: Int = 0): String {
    return time?.toDate(TIME_24, TimeZone.getDefault()).takeIf {
      it != null
    }?.let {
      if (is24) {
        time24(lang).format(it)
      } else {
        time12(lang).format(it)
      }
    } ?: ""
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
    return newCalendar(millis).map { Pair(it.getHourOfDay(), it.getMinute()) }
  }

  private fun toMillis(time24: String): Long {
    return try {
      time24.toDateWithException(TIME_24, TimeZone.getDefault()).toCalendar().let {
        val hour = it.getHourOfDay()
        val minute = it.getMinute()
        it.timeInMillis = System.currentTimeMillis()
        it.setHourOfDay(hour)
        it.setMinute(minute)
        it.dropSeconds()
        it.dropMilliseconds()
        it.timeInMillis
      }
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
    return newCalendar().takeIf { time != null }?.apply {
      var millis = toMillis(time ?: "")
      if (millis < System.currentTimeMillis()) {
        millis += AlarmManager.INTERVAL_DAY
      }
      this.timeInMillis = millis
    } ?: newCalendar()
  }

  fun getGmtFromDateTime(date: Long): String {
    GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
    return try {
      GMT_DATE_FORMAT.format(Date(date))
    } catch (e: Exception) {
      ""
    }
  }

  fun Calendar.toGmt(): String {
    GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
    return try {
      GMT_DATE_FORMAT.format(time)
    } catch (e: Exception) {
      ""
    }
  }

  fun getDateTimeFromGmt(dateTime: String?): Long {
    if (dateTime.isNullOrEmpty()) return 0
    return try {
      newCalendar(dateTime.toDate(GMT_DATE_FORMAT, TimeZone.getTimeZone(GMT))).timeInMillis
    } catch (e: Exception) {
      FirebaseCrashlytics.getInstance().recordException(e)
      0
    }
  }

  fun String?.fromGmt(def: Calendar = newCalendar()): Calendar {
    if (isNullOrEmpty()) return def
    return try {
      newCalendar(toDate(GMT_DATE_FORMAT, TimeZone.getTimeZone(GMT)))
    } catch (e: Exception) {
      def
    }
  }

  fun isAfterDate(gmt1: String?, gmt2: String?): Boolean {
    if (gmt1.isNullOrEmpty()) return false
    if (gmt2.isNullOrEmpty()) return true
    val millis1 = getDateTimeFromGmt(gmt1)
    val millis2 = getDateTimeFromGmt(gmt2)
    return millis1 > millis2
  }

  fun logTime(date: Long = System.currentTimeMillis()): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(date))
  }

  fun getFullDateTime(input: Calendar?, is24: Boolean, lang: Int = 0): String {
    val calendar = input ?: newCalendar()
    return if (is24) {
      fullDateTime24(lang).format(calendar.time)
    } else {
      fullDateTime12(lang).format(calendar.time)
    }
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
    if (TextUtils.isEmpty(date)) return null
    val loc = Locale(language.getTextLanguage(locale))
    val format = if (locale == 0) {
      if (is24) SimpleDateFormat("EEEE, MMMM dd yyyy HH:mm", loc)
      else SimpleDateFormat("EEEE, MMMM dd yyyy h:mm a", loc)
    } else {
      if (is24) SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", loc)
      else SimpleDateFormat("EEEE, dd MMMM yyyy h:mm a", loc)
    }
    return format.format(newCalendar(getDateTimeFromGmt(date)).time)
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
      FirebaseCrashlytics.getInstance().recordException(e)
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
      FirebaseCrashlytics.getInstance().recordException(e)
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
    return simpleDate(lang).format(newCalendar(date).time)
  }

  fun getDate(date: Long, lang: Int = 0): String {
    return date(lang).format(newCalendar(date).time)
  }

  private fun toGoogleTaskDate(date: Date, lang: Int = 0): String {
    return fullDate(lang).format(date)
  }

  fun toGoogleTaskDate(millis: Long, lang: Int = 0): String {
    return toGoogleTaskDate(Date(millis), lang)
  }

  fun Calendar.toGoogleTaskDate(lang: Int = 0): String {
    return toGoogleTaskDate(time, lang)
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
    }
  }

  fun getReadableBirthDate(dateOfBirth: String?, lang: Int = 0): String {
    if (dateOfBirth.isNullOrEmpty()) return ""
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return try {
      dateOfBirth.toDate(format).let {
        date(lang).format(it)
      }
    } catch (e: Exception) {
      FirebaseCrashlytics.getInstance().recordException(e)
      dateOfBirth
    }
  }

  fun getAge(dateOfBirth: String?): Int {
    if (dateOfBirth.isNullOrEmpty()) return 0
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return newCalendar().getYear() - newCalendar(dateOfBirth.toDate(format)).getYear()
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

  fun Calendar.toTime(is24: Boolean, lang: Int = 0): String {
    return getTime(time, is24, lang)
  }

  private fun getAge(year: Int, at: Long = System.currentTimeMillis()): Int {
    return newCalendar(at).getYear() - year
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
    return newCalendar().also {
      it.setDate(dateTime.year, dateTime.month - 1, dateTime.day)
    }.time
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

  data class DMY(val day: String = "", val month: String = "", val year: String = "")

  data class HM(val hour: Int, val minute: Int)
}
