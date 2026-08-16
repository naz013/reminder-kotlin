package com.github.naz013.feature.reminder.build.formatter.datetime

import android.content.Context
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.DayOfMonth
import com.github.naz013.ui.notification.settings.Formatter

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class DayOfMonthFormatter(
  private val context: Context,
) : Formatter<Int>() {
  override fun format(dayOfMonth: Int): String =
    if (dayOfMonth == DayOfMonth.LastDayOfMonth) {
      context.getString(R.string.builder_last_day_of_month)
    } else {
      "$dayOfMonth"
    }
}
