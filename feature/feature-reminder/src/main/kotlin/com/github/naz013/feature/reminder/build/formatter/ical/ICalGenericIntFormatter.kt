package com.github.naz013.feature.reminder.build.formatter.ical

import com.github.naz013.ui.notification.settings.Formatter

internal class ICalGenericIntFormatter : Formatter<Int>() {
  override fun format(value: Int): String = value.toString()
}
