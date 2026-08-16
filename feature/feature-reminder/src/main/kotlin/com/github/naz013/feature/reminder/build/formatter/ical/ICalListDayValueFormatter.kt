package com.github.naz013.feature.reminder.build.formatter.ical

import android.content.Context
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.build.adapter.ParamToTextAdapter
import com.github.naz013.ui.notification.settings.Formatter
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
