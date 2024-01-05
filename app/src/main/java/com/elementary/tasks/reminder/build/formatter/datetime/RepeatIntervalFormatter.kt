package com.elementary.tasks.reminder.build.formatter.datetime

import com.elementary.tasks.reminder.build.formatter.Formatter

class RepeatIntervalFormatter : Formatter<Long>() {
  override fun format(interval: Long): String {
    return interval.toString()
  }
}
