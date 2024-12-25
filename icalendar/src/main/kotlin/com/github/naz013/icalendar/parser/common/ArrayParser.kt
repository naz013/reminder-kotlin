package com.github.naz013.icalendar.parser.common

internal class ArrayParser {

  fun parse(input: String): List<String> {
    if (input.isEmpty()) return emptyList()
    return input.split(",")
  }
}
