package com.elementary.tasks.reminder.build.formatter.ical

import com.github.naz013.icalendar.DayValue
import com.elementary.tasks.reminder.build.formatter.Formatter
import com.elementary.tasks.reminder.build.adapter.ParamToTextAdapter

class ICalDayValueFormatter(
  private val paramToTextAdapter: ParamToTextAdapter
) : Formatter<DayValue>() {

  override fun format(value: DayValue): String {
    return paramToTextAdapter.getDayFullText(value)
  }
}
