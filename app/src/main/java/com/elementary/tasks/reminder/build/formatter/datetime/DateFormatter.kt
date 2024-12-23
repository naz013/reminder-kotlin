package com.elementary.tasks.reminder.build.formatter.datetime

import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.reminder.build.formatter.Formatter
import org.threeten.bp.LocalDate

class DateFormatter(
  private val dateTimeManager: DateTimeManager
) : Formatter<LocalDate>() {
  override fun format(date: LocalDate): String {
    return dateTimeManager.getDate(date)
  }
}
