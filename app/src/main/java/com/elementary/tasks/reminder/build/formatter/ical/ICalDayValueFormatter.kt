package com.elementary.tasks.reminder.build.formatter.ical

import com.elementary.tasks.core.utils.datetime.recurrence.DayValue
import com.elementary.tasks.reminder.build.formatter.Formatter
import com.elementary.tasks.reminder.create.fragments.recur.adapter.ParamToTextAdapter

class ICalDayValueFormatter(
  private val paramToTextAdapter: ParamToTextAdapter
) : Formatter<DayValue>() {

  override fun format(value: DayValue): String {
    return paramToTextAdapter.getDayFullText(value)
  }
}
