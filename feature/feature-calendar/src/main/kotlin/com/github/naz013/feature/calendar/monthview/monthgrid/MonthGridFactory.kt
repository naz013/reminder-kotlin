package com.github.naz013.feature.calendar.monthview.monthgrid

import com.github.naz013.feature.calendar.CalendarPreferences
import com.github.naz013.domain.calendar.StartDayOfWeekProtocol
import org.threeten.bp.LocalDate

/**
 * Builds the 42-cell (6 weeks x 7 days) grid for a month, back/forward-filling with the
 * adjacent months so every week row is complete, respecting [CalendarPreferences.startDay].
 */
class MonthGridFactory(
  private val calendarPreferences: CalendarPreferences,
) {
  fun buildGrid(monthDate: LocalDate): List<MonthGridCell> {
    val startDayOfWeek = StartDayOfWeekProtocol(calendarPreferences.startDay).getForCalendar()
    val year = monthDate.year
    val month = monthDate.monthValue
    val firstDateOfMonth = LocalDate.of(year, month, 1)
    val lastDateOfMonth = firstDateOfMonth.plusDays(firstDateOfMonth.lengthOfMonth() - 1L)

    val dateList = mutableListOf<LocalDate>()

    var weekdayOfFirstDate = firstDateOfMonth.dayOfWeek.value
    if (weekdayOfFirstDate < startDayOfWeek) {
      weekdayOfFirstDate += 7
    }
    while (weekdayOfFirstDate > 0) {
      val date = firstDateOfMonth.minusDays(weekdayOfFirstDate - startDayOfWeek.toLong())
      if (!date.isBefore(firstDateOfMonth)) {
        break
      }
      dateList.add(date)
      weekdayOfFirstDate--
    }

    for (i in 0L until lastDateOfMonth.dayOfMonth) {
      dateList.add(firstDateOfMonth.plusDays(i))
    }

    var endDayOfWeek = startDayOfWeek - 1
    if (endDayOfWeek == 0) {
      endDayOfWeek = 7
    }
    if (lastDateOfMonth.dayOfWeek.value != endDayOfWeek) {
      var i = 1L
      while (true) {
        val nextDay = lastDateOfMonth.plusDays(i)
        dateList.add(nextDay)
        i++
        if (nextDay.dayOfWeek.value == endDayOfWeek) {
          break
        }
      }
    }

    val lastDate = dateList.lastOrNull() ?: return emptyList()
    val missingDays = GRID_SIZE - dateList.size
    for (i in 1..missingDays) {
      dateList.add(lastDate.plusDays(i.toLong()))
    }

    val today = LocalDate.now()
    return dateList.map { date ->
      MonthGridCell(
        date = date,
        isCurrentMonth = date.year == year && date.monthValue == month,
        isToday = date == today,
      )
    }
  }

  companion object {
    private const val GRID_SIZE = 42
  }
}
