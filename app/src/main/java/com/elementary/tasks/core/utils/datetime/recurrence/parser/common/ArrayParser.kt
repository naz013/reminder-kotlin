package com.elementary.tasks.core.utils.datetime.recurrence.parser.common

class ArrayParser {

  fun parse(input: String): List<String> {
    if (input.isEmpty()) return emptyList()
    return input.split(",")
  }
}
