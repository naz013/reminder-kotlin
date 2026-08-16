package com.github.naz013.feature.reminder.build.formatter.datetime

import com.github.naz013.ui.notification.settings.Formatter
import com.github.naz013.datecalc.DateTimeManager
import org.threeten.bp.LocalDate

class DayOfYearFormatter(
  private val dateTimeManager: DateTimeManager,
) : Formatter<Int>() {
  override fun format(dayOfYear: Int): String {
    val date = LocalDate.ofYearDay(LocalDate.now().year, dayOfYear)
    return "$dayOfYear - ${dateTimeManager.formatDayMonth(date)}"
  }
}
