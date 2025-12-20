package com.elementary.tasks.calendar.dayview.weekheader

import com.elementary.tasks.calendar.occurrence.GetOccurrencesByDayUseCase
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.calendar.StartDayOfWeekProtocol
import org.threeten.bp.LocalDate

class WeekFactory(
  private val prefs: Prefs,
  private val dateTimeManager: DateTimeManager,
  private val getOccurrencesByDayUseCase: GetOccurrencesByDayUseCase
) {

  suspend fun createWeek(date: LocalDate): List<WeekDay> {
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
        isSelected = it == date,
        hasEvents = getOccurrencesByDayUseCase(it).isNotEmpty()
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
