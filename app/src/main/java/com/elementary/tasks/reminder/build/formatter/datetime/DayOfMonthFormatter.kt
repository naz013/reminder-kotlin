package com.elementary.tasks.reminder.build.formatter.datetime

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.formatter.Formatter

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class DayOfMonthFormatter(
  private val context: Context
) : Formatter<Int>() {
  override fun format(dayOfMonth: Int): String {
    return if (dayOfMonth == 0) {
      context.getString(R.string.builder_last_day_of_month)
    } else {
      "$dayOfMonth"
    }
  }
}
