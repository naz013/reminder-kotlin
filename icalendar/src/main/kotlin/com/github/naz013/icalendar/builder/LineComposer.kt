package com.github.naz013.icalendar.builder

internal class LineComposer {

  fun compose(lines: List<String>): String? {
    if (lines.isEmpty()) return null
    return StringBuilder().apply {
      lines.forEach { append(it) }
    }.toString()
  }
}
