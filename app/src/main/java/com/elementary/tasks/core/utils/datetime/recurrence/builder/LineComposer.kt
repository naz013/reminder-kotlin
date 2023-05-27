package com.elementary.tasks.core.utils.datetime.recurrence.builder

class LineComposer {

  fun compose(lines: List<String>): String? {
    if (lines.isEmpty()) return null
    return StringBuilder().apply {
      lines.forEach { append(it) }
    }.toString()
  }
}
