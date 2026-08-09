package com.elementary.tasks.birthdays.create

import com.github.naz013.datecalc.DateTimeManager
import org.threeten.bp.LocalDate

class UiBirthdayDateFormatter(
  private val dateTimeManager: DateTimeManager,
) {

  fun getDateFormatted(
    date: LocalDate,
    showYear: Boolean
  ): String {
    return if (showYear) {
      dateTimeManager.formatBirthdayFullDateForUi(date)
    } else {
      dateTimeManager.formatBirthdayDateForUi(date)
    }
  }
}
