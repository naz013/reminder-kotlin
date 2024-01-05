package com.elementary.tasks.reminder.build.formatter.datetime

import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.reminder.build.formatter.Formatter
import org.threeten.bp.LocalDate

class DateFormatter(
  private val dateTimeManager: DateTimeManager
) : Formatter<LocalDate>() {
  override fun format(date: LocalDate): String {
    return dateTimeManager.getDate(date)
  }
}
