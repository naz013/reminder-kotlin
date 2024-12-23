package com.elementary.tasks.reminder.build.formatter.ical

import android.content.Context
import com.elementary.tasks.R
import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.reminder.build.formatter.Formatter
import org.threeten.bp.LocalDate

class ICalByMonthFormatter(
  private val context: Context,
  private val dateTimeManager: DateTimeManager
) : Formatter<List<Int>>() {

  private val date = LocalDate.now().withDayOfMonth(15)

  override fun format(months: List<Int>): String {
    return if (months.isEmpty()) {
      context.getString(R.string.builder_not_selected)
    } else {
      months.joinToString(", ") { getValue(it) }
    }
  }

  private fun getValue(month: Int): String {
    return date.withMonth(month).let { dateTimeManager.formatMonth(it) }
  }
}
