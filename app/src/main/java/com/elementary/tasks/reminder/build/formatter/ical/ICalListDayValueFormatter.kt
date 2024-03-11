package com.elementary.tasks.reminder.build.formatter.ical

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.datetime.recurrence.DayValue
import com.elementary.tasks.reminder.build.formatter.Formatter
import com.elementary.tasks.reminder.create.fragments.recur.adapter.ParamToTextAdapter

class ICalListDayValueFormatter(
  private val context: Context,
  private val paramToTextAdapter: ParamToTextAdapter
) : Formatter<List<DayValue>>() {

  override fun format(values: List<DayValue>): String {
    return if (values.isEmpty()) {
      context.getString(R.string.builder_not_selected)
    } else {
      values.joinToString(", ") { paramToTextAdapter.getDayFullText(it) }
    }
  }
}
