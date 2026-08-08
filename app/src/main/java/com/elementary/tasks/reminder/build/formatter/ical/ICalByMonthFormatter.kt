package com.elementary.tasks.reminder.build.formatter.ical

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.formatter.Formatter
import com.github.naz013.datecalc.DateTimeManager
import org.threeten.bp.LocalDate

class ICalByMonthFormatter(
  private val context: Context,
  private val dateTimeManager: DateTimeManager,
) : Formatter<List<Int>>() {
  private val date = LocalDate.now().withDayOfMonth(15)

  override fun format(months: List<Int>): String =
    if (months.isEmpty()) {
      context.getString(R.string.builder_not_selected)
    } else {
      months.joinToString(", ") { getValue(it) }
    }

  private fun getValue(month: Int): String = date.withMonth(month).let { dateTimeManager.formatMonth(it) }
}
