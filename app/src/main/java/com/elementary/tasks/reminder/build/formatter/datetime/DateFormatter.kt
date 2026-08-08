package com.elementary.tasks.reminder.build.formatter.datetime

import com.elementary.tasks.reminder.build.formatter.Formatter
import com.github.naz013.datecalc.DateTimeManager
import org.threeten.bp.LocalDate

class DateFormatter(
  private val dateTimeManager: DateTimeManager,
) : Formatter<LocalDate>() {
  override fun format(date: LocalDate): String = dateTimeManager.getDate(date)
}
