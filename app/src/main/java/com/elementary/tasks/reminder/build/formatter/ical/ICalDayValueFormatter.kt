package com.elementary.tasks.reminder.build.formatter.ical

import com.elementary.tasks.reminder.build.adapter.ParamToTextAdapter
import com.elementary.tasks.reminder.build.formatter.Formatter
import com.github.naz013.icalendar.DayValue

class ICalDayValueFormatter(
  private val paramToTextAdapter: ParamToTextAdapter,
) : Formatter<DayValue>() {
  override fun format(value: DayValue): String = paramToTextAdapter.getDayFullText(value)
}
