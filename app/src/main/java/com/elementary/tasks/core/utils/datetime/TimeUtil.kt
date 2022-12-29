package com.elementary.tasks.core.utils.datetime

import android.text.TextUtils
import com.elementary.tasks.core.utils.Language
import com.github.naz013.calendarext.newCalendar
import com.github.naz013.calendarext.toDate
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.text.SimpleDateFormat
import java.util.*

@Deprecated("Use DateTimeManager")
object TimeUtil {

  private const val GMT = "GMT"

  private val GMT_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.US)

  private fun localizedDateFormat(pattern: String, lang: Int = 0): SimpleDateFormat = SimpleDateFormat(pattern,
    Language.getScreenLanguage(lang)
  )

  private fun fullDateTime24(lang: Int = 0): SimpleDateFormat = localizedDateFormat("EEE, dd MMM yyyy HH:mm", lang)

  private fun fullDateTime12(lang: Int = 0): SimpleDateFormat = localizedDateFormat("EEE, dd MMM yyyy h:mm a", lang)

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
}
