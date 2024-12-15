package com.elementary.tasks.reminder.build.formatter.datetime

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.config.DayOfMonth
import com.elementary.tasks.reminder.build.formatter.Formatter

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class DayOfMonthFormatter(
  private val context: Context
) : Formatter<Int>() {
  override fun format(dayOfMonth: Int): String {
    return if (dayOfMonth == DayOfMonth.LastDayOfMonth) {
      context.getString(R.string.builder_last_day_of_month)
    } else {
      "$dayOfMonth"
    }
  }
}
