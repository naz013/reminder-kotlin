package com.github.naz013.icalendar.parser.common

import com.github.naz013.icalendar.UtcDateTime

internal class UtcDateTimeParser {

  fun parse(input: String): UtcDateTime? {
    if (input.isEmpty()) return null
    return runCatching { UtcDateTime(input) }.getOrNull()
  }
}
