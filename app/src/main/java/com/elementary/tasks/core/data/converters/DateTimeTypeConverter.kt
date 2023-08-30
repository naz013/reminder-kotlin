package com.elementary.tasks.core.data.converters

import androidx.room.TypeConverter
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

class DateTimeTypeConverter {

  @TypeConverter
  fun toString(dateTime: LocalDateTime): String {
    return dateTime.format(FORMATTER)
  }

  @TypeConverter
  fun toDateTime(s: String?): LocalDateTime {
    if (s == null) return LocalDateTime.now()
    return LocalDateTime.parse(s, FORMATTER)
  }

  companion object {
    private val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)
  }
}
