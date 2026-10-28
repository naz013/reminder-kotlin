package com.github.naz013.icalendar.parser.common

import com.github.naz013.domain.reminder.v2.toKnownTimeZoneIdOrNull

internal class TimeZoneIdParser {

  /** Reads `TZID=` from the property name part (before the first colon). Unknown ids give null (floating). */
  fun parse(propertyPart: String): String? {
    return TZID_REGEX.find(propertyPart)?.groupValues?.get(1).toKnownTimeZoneIdOrNull()
  }

  companion object {
    private val TZID_REGEX = Regex(";TZID=\"?([^;:\"]+)\"?")
  }
}
