package com.elementary.tasks.core.utils.datetime.recurrence.parser.common

import com.elementary.tasks.core.utils.datetime.recurrence.UtcDateTime

class UtcDateTimeParser {

  fun parse(input: String): UtcDateTime? {
    if (input.isEmpty()) return null
    return runCatching { UtcDateTime(input) }.getOrNull()
  }
}
