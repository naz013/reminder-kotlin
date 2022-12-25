package com.backdoor.engine.misc

import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

internal object TimeUtil {

  private val GMT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)

  fun getGmtFromDateTime(dateTime: LocalDateTime?): String {
    if (dateTime == null) return ""
    return dateTime.format(GMT_DATE_FORMAT.withZone(ZoneId.of("GMT")))
  }
}
