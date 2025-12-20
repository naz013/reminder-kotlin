package com.elementary.tasks.dayview.weekheader

import com.elementary.tasks.calendar.dayview.weekheader.WeekDay
import com.elementary.tasks.calendar.dayview.weekheader.WeekFactory
import com.elementary.tasks.calendar.occurrence.GetOccurrencesByDayUseCase
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.datetime.DateTimePreferences
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import java.util.Locale

class WeekFactoryTest {

  private lateinit var prefs: Prefs
  private lateinit var dateTimePreferences: DateTimePreferences
  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var weekFactory: WeekFactory
  private lateinit var getOccurrencesByDayUseCase: GetOccurrencesByDayUseCase

  @Before
  fun setUp() {
    prefs = mockk()
    every { prefs.appLanguage } returns 1

    dateTimePreferences = mockk()
    every { dateTimePreferences.locale } returns Locale.US

    dateTimeManager = DateTimeManager(
      nowDateTimeProvider = mockk(),
      dateTimePreferences = dateTimePreferences
    )

    getOccurrencesByDayUseCase = mockk()
    coEvery { getOccurrencesByDayUseCase.invoke(any()) } returns emptyList()

    weekFactory = WeekFactory(prefs, dateTimeManager, getOccurrencesByDayUseCase)
  }

  @Test
  fun testWeekBuild_whenStartDayIsSunday_andCurrentDayIsSame() = runTest {
    every { prefs.startDay }.returns(0)

    val localDate = LocalDate.of(2023, 8, 20)
    val weekdays = weekFactory.createWeek(localDate)

    println(weekdays)

    val expected = listOf(
      WeekDay(weekday = "Sun", date = "20", isSelected = true, localDate = localDate, hasEvents = false),
      WeekDay(weekday = "Mon", date = "21", isSelected = false, localDate = localDate.plusDays(1), hasEvents = false),
      WeekDay(weekday = "Tue", date = "22", isSelected = false, localDate = localDate.plusDays(2), hasEvents = false),
      WeekDay(weekday = "Wed", date = "23", isSelected = false, localDate = localDate.plusDays(3), hasEvents = false),
      WeekDay(weekday = "Thu", date = "24", isSelected = false, localDate = localDate.plusDays(4), hasEvents = false),
      WeekDay(weekday = "Fri", date = "25", isSelected = false, localDate = localDate.plusDays(5), hasEvents = false),
      WeekDay(weekday = "Sat", date = "26", isSelected = false, localDate = localDate.plusDays(6), hasEvents = false)
    )

    Assert.assertEquals(expected, weekdays)
  }

  @Test
  fun testWeekBuild_whenStartDayIsSunday_andCurrentDayIsNotTheSame() = runTest {
    every { prefs.startDay }.returns(0)

    val localDate = LocalDate.of(2023, 8, 22)
    val startDate = LocalDate.of(2023, 8, 20)
    val weekdays = weekFactory.createWeek(localDate)

    println(weekdays)

    val expected = listOf(
      WeekDay(weekday = "Sun", date = "20", isSelected = false, localDate = startDate, hasEvents = false),
      WeekDay(weekday = "Mon", date = "21", isSelected = false, localDate = startDate.plusDays(1), hasEvents = false),
      WeekDay(weekday = "Tue", date = "22", isSelected = true, localDate = startDate.plusDays(2), hasEvents = false),
      WeekDay(weekday = "Wed", date = "23", isSelected = false, localDate = startDate.plusDays(3), hasEvents = false),
      WeekDay(weekday = "Thu", date = "24", isSelected = false, localDate = startDate.plusDays(4), hasEvents = false),
      WeekDay(weekday = "Fri", date = "25", isSelected = false, localDate = startDate.plusDays(5), hasEvents = false),
      WeekDay(weekday = "Sat", date = "26", isSelected = false, localDate = startDate.plusDays(6), hasEvents = false)
    )

    Assert.assertEquals(expected, weekdays)
  }

  @Test
  fun testWeekBuild_whenStartDayIsMonday_andCurrentDayIsSame() = runTest {
    every { prefs.startDay }.returns(1)

    val localDate = LocalDate.of(2023, 8, 21)
    val weekdays = weekFactory.createWeek(localDate)

    println(weekdays)

    val expected = listOf(
      WeekDay(weekday = "Mon", date = "21", isSelected = true, localDate = localDate, hasEvents = false),
      WeekDay(weekday = "Tue", date = "22", isSelected = false, localDate = localDate.plusDays(1), hasEvents = false),
      WeekDay(weekday = "Wed", date = "23", isSelected = false, localDate = localDate.plusDays(2), hasEvents = false),
      WeekDay(weekday = "Thu", date = "24", isSelected = false, localDate = localDate.plusDays(3), hasEvents = false),
      WeekDay(weekday = "Fri", date = "25", isSelected = false, localDate = localDate.plusDays(4), hasEvents = false),
      WeekDay(weekday = "Sat", date = "26", isSelected = false, localDate = localDate.plusDays(5), hasEvents = false),
      WeekDay(weekday = "Sun", date = "27", isSelected = false, localDate = localDate.plusDays(6), hasEvents = false)
    )

    Assert.assertEquals(expected, weekdays)
  }

  @Test
  fun testWeekBuild_whenStartDayIsMonday_andCurrentDayIsNotTheSame() = runTest {
    every { prefs.startDay }.returns(1)

    val startDate = LocalDate.of(2023, 8, 21)
    val localDate = LocalDate.of(2023, 8, 22)
    val weekdays = weekFactory.createWeek(localDate)

    println(weekdays)

    val expected = listOf(
      WeekDay(weekday = "Mon", date = "21", isSelected = false, localDate = startDate, hasEvents = false),
      WeekDay(weekday = "Tue", date = "22", isSelected = true, localDate = startDate.plusDays(1), hasEvents = false),
      WeekDay(weekday = "Wed", date = "23", isSelected = false, localDate = startDate.plusDays(2), hasEvents = false),
      WeekDay(weekday = "Thu", date = "24", isSelected = false, localDate = startDate.plusDays(3), hasEvents = false),
      WeekDay(weekday = "Fri", date = "25", isSelected = false, localDate = startDate.plusDays(4), hasEvents = false),
      WeekDay(weekday = "Sat", date = "26", isSelected = false, localDate = startDate.plusDays(5), hasEvents = false),
      WeekDay(weekday = "Sun", date = "27", isSelected = false, localDate = startDate.plusDays(6), hasEvents = false)
    )

    Assert.assertEquals(expected, weekdays)
  }
}
