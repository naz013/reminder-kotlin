package com.elementary.tasks.birthdays.create

import com.github.naz013.common.datetime.DateTimeManager
import org.threeten.bp.LocalDate

class UiBirthdayDateFormatter(
  private val dateTimeManager: DateTimeManager
) {

  private var showYear: Boolean = true

  fun getDateFormatted(date: LocalDate): String {
    return if (showYear) {
      dateTimeManager.formatBirthdayFullDateForUi(date)
    } else {
      dateTimeManager.formatBirthdayDateForUi(date)
    }
  }

  fun changeShowYear(showYear: Boolean) {
    this.showYear = showYear
  }
}
