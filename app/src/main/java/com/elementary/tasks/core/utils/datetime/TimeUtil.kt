package com.elementary.tasks.core.utils.datetime

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.TextUtils
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.map
import com.elementary.tasks.core.utils.params.Prefs
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
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@Deprecated("Use DateTimeManager")
object TimeUtil {

  private const val GMT = "GMT"

  val BIRTH_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
  val BIRTH_FORMAT = SimpleDateFormat("dd|MM", Locale.US)
  private val NEW_GMT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)
  private val GMT_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.US)
  private val FIRE_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
  private val TIME_24 = SimpleDateFormat("HH:mm", Locale.US)

  private fun localizedDateFormat(pattern: String, lang: Int = 0): SimpleDateFormat = SimpleDateFormat(pattern,
    Language.getScreenLanguage(lang)
  )

  private fun dateTime24(lang: Int = 0): SimpleDateFormat = localizedDateFormat("dd MMM yyyy, HH:mm", lang)

  private fun dateTime12(lang: Int = 0): SimpleDateFormat = localizedDateFormat("dd MMM yyyy, h:mm a", lang)

  private fun fullDateTime24(lang: Int = 0): SimpleDateFormat = localizedDateFormat("EEE, dd MMM yyyy HH:mm", lang)

  private fun fullDateTime12(lang: Int = 0): SimpleDateFormat = localizedDateFormat("EEE, dd MMM yyyy h:mm a", lang)

  private fun time24(lang: Int = 0): SimpleDateFormat = localizedDateFormat("HH:mm", lang)

  private fun time12(lang: Int = 0): SimpleDateFormat = localizedDateFormat("h:mm a", lang)

  @Deprecated("Use DateTimeManager")
  fun simpleDate(lang: Int = 0): SimpleDateFormat = localizedDateFormat("d MMMM", lang)

  @Deprecated("Use DateTimeManager")
  fun date(lang: Int = 0): SimpleDateFormat = localizedDateFormat("dd MMM yyyy", lang)

  @Deprecated("Use DateTimeManager")
  fun day(lang: Int = 0): SimpleDateFormat = localizedDateFormat("dd", lang)

  @Deprecated("Use DateTimeManager")
  fun month(lang: Int = 0): SimpleDateFormat = localizedDateFormat("MMM", lang)

  @Deprecated("Use DateTimeManager")
  fun year(lang: Int = 0): SimpleDateFormat = localizedDateFormat("yyyy", lang)

  @Deprecated("Use DateTimeManager")
  fun getPlaceDateTimeFromGmt(dateTime: String?, lang: Int = 0): DMY {
    val date = dateTime?.toDate(GMT_DATE_FORMAT, TimeZone.getTimeZone(GMT)) ?: Date()
    return try {
      DMY(day(lang).format(date), month(lang).format(date), year(lang).format(date))
    } catch (e: Exception) {
      DMY()
    }
  }

  @Deprecated("Use DateTimeManager")
  val gmtDateTime: String
    get() {
      GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
      return try {
        GMT_DATE_FORMAT.format(Date())
      } catch (e: Exception) {
        ""
      }
    }

  @Deprecated("Use DateTimeManager")
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

  @Deprecated("Use DateTimeManager")
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

  @Deprecated("Use DateTimeManager")
  fun showTimePicker(context: Context, is24: Boolean, hour: Int, minute: Int,
                     listener: TimePickerDialog.OnTimeSetListener): TimePickerDialog {
    val dialog = TimePickerDialog(context, listener, hour, minute, is24)
    dialog.show()
    return dialog
  }

  @Deprecated("Use DateTimeManager")
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

  @Deprecated("Use DateTimeManager")
  fun showDatePicker(context: Context, prefs: Prefs, year: Int, month: Int, dayOfMonth: Int,
                     listener: DatePickerDialog.OnDateSetListener): DatePickerDialog {
    val dialog = DatePickerDialog(context, listener, year, month, dayOfMonth)
    dialog.datePicker.firstDayOfWeek = prefs.startDay + 1
    dialog.show()
    return dialog
  }

  @Deprecated("Use DateTimeManager")
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

  @Deprecated("Use DateTimeManager")
  fun getBirthdayTime(hour: Int, minute: Int): String {
    return newCalendar().apply {
      this.setTime(hour, minute)
    }.map { TIME_24.format(it.time) }
  }

  @Deprecated("Use DateTimeManager")
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

  @Deprecated("Use DateTimeManager")
  fun millisToEndDnd(from: String?, to: String?, current: Long): Long {
    return doNotDisturbRange(from, to).last - current
  }

  @Deprecated("Use DateTimeManager")
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

  @Deprecated("Use DateTimeManager")
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

  @Deprecated("Use DateTimeManager")
  fun getBirthdayCalendar(time: String?): Calendar {
    return newCalendar().takeIf { time != null }?.apply {
      var millis = toMillis(time ?: "")
      if (millis < System.currentTimeMillis()) {
        millis += AlarmManager.INTERVAL_DAY
      }
      this.timeInMillis = millis
    } ?: newCalendar()
  }

  @Deprecated("Use DateTimeManager")
  fun getGmtFromDateTime(date: Long): String {
    GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
    return try {
      GMT_DATE_FORMAT.format(Date(date))
    } catch (e: Exception) {
      ""
    }
  }

  @Deprecated("Use DateTimeManager")
  fun Calendar.toGmt(): String {
    GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
    return try {
      GMT_DATE_FORMAT.format(time)
    } catch (e: Exception) {
      ""
    }
  }

  @Deprecated("Use DateTimeManager")
  fun getDateTimeFromGmt(dateTime: String?): Long {
    if (dateTime.isNullOrEmpty()) return 0
    return try {
      newCalendar(dateTime.toDate(GMT_DATE_FORMAT, TimeZone.getTimeZone(GMT))).timeInMillis
    } catch (e: Exception) {
      FirebaseCrashlytics.getInstance().recordException(e)
      0
    }
  }

  @Deprecated("Use DateTimeManager")
  fun isAfterDate(gmt1: String?, gmt2: String?): Boolean {
    if (gmt1.isNullOrEmpty()) return false
    if (gmt2.isNullOrEmpty()) return true
    val millis1 = getDateTimeFromGmt(gmt1)
    val millis2 = getDateTimeFromGmt(gmt2)
    return millis1 > millis2
  }

  @Deprecated("Use DateTimeManager")
  fun logTime(date: Long = System.currentTimeMillis()): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(date))
  }

  @Deprecated("Use DateTimeManager")
  fun getFullDateTime(input: Calendar?, is24: Boolean, lang: Int = 0): String {
    val calendar = input ?: newCalendar()
    return if (is24) {
      fullDateTime24(lang).format(calendar.time)
    } else {
      fullDateTime12(lang).format(calendar.time)
    }
  }

  @Deprecated("Use DateTimeManager")
  fun getFullDateTime(date: Long, is24: Boolean, lang: Int = 0): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = date
    return if (is24) {
      fullDateTime24(lang).format(calendar.time)
    } else {
      fullDateTime12(lang).format(calendar.time)
    }
  }

  @Deprecated("Use DateTimeManager")
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

  @Deprecated("Use DateTimeManager")
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

  @Deprecated("Use DateTimeManager")
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

  @Deprecated("Use DateTimeManager")
  fun getSimpleDate(gmtDate: String?, lang: Int = 0): String {
    return getSimpleDate(getDateTimeFromGmt(gmtDate), lang)
  }

  @Deprecated("Use DateTimeManager")
  fun getSimpleDate(date: Long, lang: Int = 0): String {
    return simpleDate(lang).format(newCalendar(date).time)
  }

  @Deprecated("Use DateTimeManager")
  fun getDate(date: String): Date? {
    return try {
      TIME_24.parse(date)
    } catch (e: Exception) {
      null
    }
  }

  @Deprecated("Use DateTimeManager")
  fun getDateTime(date: Date, is24: Boolean, lang: Int = 0): String {
    return if (is24) {
      dateTime24(lang).format(date)
    } else {
      dateTime12(lang).format(date)
    }
  }

  @Deprecated("Use DateTimeManager")
  fun getTime(date: Date, is24: Boolean, lang: Int = 0): String {
    return if (is24) {
      time24(lang).format(date)
    } else {
      time12(lang).format(date)
    }
  }

  @Deprecated("Use DateTimeManager")
  fun convertDateTimeToDate(dateTime: DateTime): Date {
    return newCalendar().also {
      it.setDate(dateTime.year, dateTime.month - 1, dateTime.day)
    }.time
  }

  @Deprecated("Use DateTimeManager")
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

  @Deprecated("Use DateTimeManager")
  data class DMY(val day: String = "", val month: String = "", val year: String = "")

  @Deprecated("Use DateTimeManager")
  data class HM(val hour: Int, val minute: Int)
}
