package com.elementary.tasks.dayview.weekheader

import com.elementary.tasks.core.protocol.StartDayOfWeekProtocol
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.Prefs
import org.threeten.bp.LocalDate

class WeekFactory(
  private val prefs: Prefs,
  private val dateTimeManager: DateTimeManager
) {

  fun createWeek(date: LocalDate): List<WeekDay> {
    val startDay = StartDayOfWeekProtocol(prefs.startDay).getForCalendar()

    val currentDayOfWeek = date.dayOfWeek.value

    val dt = if (currentDayOfWeek != startDay) {
      moveBackUntilDayOfWeek(date, startDay)
    } else {
      date
    }

    return (0..6).toList().map {
      dt.plusDays(it.toLong())
    }.map {
      WeekDay(
        localDate = it,
        weekday = dateTimeManager.formatCalendarWeekday(it),
        date = dateTimeManager.formatCalendarDay(it),
        isSelected = it == date
      )
    }
  }

  private fun moveBackUntilDayOfWeek(date: LocalDate, dayOfWeek: Int): LocalDate {
    return if (date.dayOfWeek.value == dayOfWeek) {
      date
    } else {
      moveBackUntilDayOfWeek(date.minusDays(1L), dayOfWeek)
    }
  }
}
