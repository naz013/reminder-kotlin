package com.github.naz013.feature.reminder.build.formatter.datetime

import com.github.naz013.ui.notification.settings.Formatter
import com.github.naz013.datecalc.DateTimeManager
import org.threeten.bp.LocalDate

class DateFormatter(
  private val dateTimeManager: DateTimeManager,
) : Formatter<LocalDate>() {
  override fun format(date: LocalDate): String = dateTimeManager.getDate(date)
}
