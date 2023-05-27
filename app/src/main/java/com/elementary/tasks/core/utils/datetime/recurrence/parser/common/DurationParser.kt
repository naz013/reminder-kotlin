package com.elementary.tasks.core.utils.datetime.recurrence.parser.common

import com.elementary.tasks.core.utils.datetime.recurrence.Duration

class DurationParser {

  fun parse(input: String): Duration? {
    if (input.isEmpty()) return null
    return runCatching { Duration(input) }.getOrNull()
  }
}
