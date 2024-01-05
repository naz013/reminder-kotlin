package com.elementary.tasks.reminder.build.formatter.ical

import com.elementary.tasks.reminder.build.formatter.Formatter

class ICalGenericIntFormatter : Formatter<Int>() {

  override fun format(value: Int): String {
    return value.toString()
  }
}
