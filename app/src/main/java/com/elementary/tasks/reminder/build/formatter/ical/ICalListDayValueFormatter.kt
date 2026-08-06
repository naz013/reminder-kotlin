package com.elementary.tasks.reminder.build.formatter.ical

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.adapter.ParamToTextAdapter
import com.elementary.tasks.reminder.build.formatter.Formatter
import com.github.naz013.icalendar.DayValue

class ICalListDayValueFormatter(
  private val context: Context,
  private val paramToTextAdapter: ParamToTextAdapter,
) : Formatter<List<DayValue>>() {
  override fun format(values: List<DayValue>): String =
    if (values.isEmpty()) {
      context.getString(R.string.builder_not_selected)
    } else {
      values.joinToString(", ") { paramToTextAdapter.getDayFullText(it) }
    }
}
