package com.github.naz013.appwidgets.calendar

import android.content.Context
import android.text.format.DateUtils
import com.github.naz013.appwidgets.AppWidgetPreferences
import com.github.naz013.appwidgets.calendar.data.CalendarAppWidgetState
import com.github.naz013.appwidgets.calendar.data.UiCalendarDay
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.calendar.StartDayOfWeekProtocol
import com.github.naz013.ui.common.theme.ThemeProvider
import org.threeten.bp.LocalDate
import java.util.Calendar
import java.util.Formatter
import java.util.GregorianCalendar
import java.util.Locale

internal class CalendarAppWidgetViewModel(
  private val context: Context,
  private val prefsProvider: CalendarWidgetPrefsProvider,
  private val widgetDataProvider: WidgetDataProvider,
  private val dateTimeManager: DateTimeManager,
  private val appWidgetPreferences: AppWidgetPreferences,
  private val themeProvider: ThemeProvider
) {

  suspend fun getState(): CalendarAppWidgetState {
    val year = prefsProvider.getResolvedYear()
    val month = prefsProvider.getMonth() + 1
    return CalendarAppWidgetState(
      widgetId = prefsProvider.widgetId,
      backgroundColorIndex = prefsProvider.getBackground(),
      monthYearText = formatMonthYear(year, month),
      weekdays = buildWeekdays(),
      days = buildDays(year, month),
      todayMarkColor = themeProvider.themedColor(appWidgetPreferences.todayColor),
      reminderMarkColor = themeProvider.themedColor(appWidgetPreferences.reminderColor),
      birthdayMarkColor = themeProvider.themedColor(appWidgetPreferences.birthdayColor)
    )
  }

  private fun formatMonthYear(year: Int, month: Int): String {
    val cal = GregorianCalendar()
    cal.set(Calendar.MONTH, month - 1)
    cal.set(Calendar.YEAR, year)
    val monthYearStringBuilder = StringBuilder(50)
    val monthYearFormatter = Formatter(monthYearStringBuilder, Locale.getDefault())
    val monthYearFlag = (
      DateUtils.FORMAT_SHOW_DATE or
        DateUtils.FORMAT_NO_MONTH_DAY or
        DateUtils.FORMAT_SHOW_YEAR
      )
    return DateUtils.formatDateRange(
      context,
      monthYearFormatter,
      cal.timeInMillis,
      cal.timeInMillis,
      monthYearFlag
    ).toString().uppercase()
  }

  private fun buildWeekdays(): List<String> {
    var date = if (appWidgetPreferences.startDay == 0) {
      LocalDate.of(2022, 12, 25)
    } else {
      LocalDate.of(2022, 12, 26)
    }
    return (0 until 7).map {
      val text = dateTimeManager.formatCalendarWeekday(date).uppercase()
      date = date.plusDays(1)
      text
    }
  }

  private suspend fun buildDays(year: Int, month: Int): List<UiCalendarDay> {
    val dateList = buildDateList(year, month)

    val birthdayTime = dateTimeManager.getBirthdayLocalTime()
    birthdayTime?.also { widgetDataProvider.setTime(it) }
    if (appWidgetPreferences.isRemindersInCalendarEnabled) {
      widgetDataProvider.setFuture(appWidgetPreferences.isFutureEventEnabled)
    }
    widgetDataProvider.prepare()

    val today = LocalDate.now()
    return dateList.map { date ->
      UiCalendarDay(
        date = date,
        dayText = date.dayOfMonth.toString(),
        isCurrentMonth = date.monthValue == month,
        isToday = date == today,
        hasReminder = widgetDataProvider.hasReminder(date),
        hasBirthday = widgetDataProvider.hasBirthday(date.dayOfMonth, date.monthValue)
      )
    }
  }

  private fun buildDateList(year: Int, month: Int): List<LocalDate> {
    val dateList = mutableListOf<LocalDate>()
    val firstDateOfMonth = LocalDate.of(year, month, 1)
    val lastDateOfMonth = firstDateOfMonth.plusDays(firstDateOfMonth.lengthOfMonth() - 1L)

    var weekdayOfFirstDate = firstDateOfMonth.dayOfWeek.value
    val startDayOfWeek = StartDayOfWeekProtocol(appWidgetPreferences.startDay).getForCalendar()
    if (weekdayOfFirstDate < startDayOfWeek) {
      weekdayOfFirstDate += 7
    }

    while (weekdayOfFirstDate > 0) {
      val dateTime = firstDateOfMonth.minusDays(weekdayOfFirstDate - startDayOfWeek.toLong())
      if (!dateTime.isBefore(firstDateOfMonth)) {
        break
      }
      dateList.add(dateTime)
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
    val size = dateList.size
    val numOfDays = 42 - size
    val lastDateTime = dateList[size - 1]
    for (i in 1L..numOfDays) {
      dateList.add(lastDateTime.plusDays(i))
    }
    return dateList
  }
}
