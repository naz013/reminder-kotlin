package com.elementary.tasks.reminder.build.formatter.ical

import com.github.naz013.ui.notification.settings.Formatter

class ICalGenericIntFormatter : Formatter<Int>() {
  override fun format(value: Int): String = value.toString()
}
