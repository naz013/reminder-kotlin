package com.backdoor.engine.misc

import org.apache.commons.lang3.StringUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale

internal object TimeUtil {
  private val GMT_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZZZ", Locale.US)
  private const val GMT = "GMT"
  fun getGmtFromDateTime(date: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = date
    GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
    return GMT_DATE_FORMAT.format(calendar.time)
  }

  fun getDateTimeFromGmt(dateTime: String?): String {
    if (StringUtils.isEmpty(dateTime)) {
      return ""
    }
    val calendar = Calendar.getInstance()
    try {
      GMT_DATE_FORMAT.timeZone = TimeZone.getTimeZone(GMT)
      val date = GMT_DATE_FORMAT.parse(dateTime)
      calendar.time = date
    } catch (e: ParseException) {
      e.printStackTrace()
    }
    GMT_DATE_FORMAT.timeZone = TimeZone.getDefault()
    return GMT_DATE_FORMAT.format(calendar.time)
  }
}