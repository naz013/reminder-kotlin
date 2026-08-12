package com.elementary.tasks.reminder.build.formatter.ical

import com.elementary.tasks.reminder.build.adapter.ParamToTextAdapter
import com.github.naz013.ui.notification.settings.Formatter
import com.github.naz013.icalendar.DayValue

class ICalDayValueFormatter(
  private val paramToTextAdapter: ParamToTextAdapter,
) : Formatter<DayValue>() {
  override fun format(value: DayValue): String = paramToTextAdapter.getDayFullText(value)
}
