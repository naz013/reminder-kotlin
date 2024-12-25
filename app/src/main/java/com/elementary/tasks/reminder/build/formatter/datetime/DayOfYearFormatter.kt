package com.elementary.tasks.reminder.build.formatter.datetime

import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.reminder.build.formatter.Formatter
import org.threeten.bp.LocalDate

class DayOfYearFormatter(
  private val dateTimeManager: DateTimeManager
) : Formatter<Int>() {
  override fun format(dayOfYear: Int): String {
    val date = LocalDate.ofYearDay(LocalDate.now().year, dayOfYear)
    return "$dayOfYear - ${dateTimeManager.formatDayMonth(date)}"
  }
}
