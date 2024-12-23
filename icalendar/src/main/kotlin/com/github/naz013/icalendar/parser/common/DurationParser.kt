package com.github.naz013.icalendar.parser.common

import com.github.naz013.icalendar.Duration

internal class DurationParser {

  fun parse(input: String): Duration? {
    if (input.isEmpty()) return null
    return runCatching { Duration(input) }.getOrNull()
  }
}
