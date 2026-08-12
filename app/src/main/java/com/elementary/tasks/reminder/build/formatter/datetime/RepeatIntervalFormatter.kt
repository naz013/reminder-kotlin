package com.elementary.tasks.reminder.build.formatter.datetime

import com.github.naz013.ui.notification.settings.Formatter

class RepeatIntervalFormatter : Formatter<Long>() {
  override fun format(interval: Long): String = interval.toString()
}
