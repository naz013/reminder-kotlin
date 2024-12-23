package com.elementary.tasks.dayview.weekheader

import com.elementary.tasks.calendar.dayview.weekheader.WeekDay
import com.elementary.tasks.calendar.dayview.weekheader.WeekFactory
import com.github.naz013.common.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.Prefs
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate

class WeekFactoryTest {

  private lateinit var prefs: Prefs
  private lateinit var dateTimeManager: DateTimeManager
  private lateinit var weekFactory: WeekFactory

  @Before
  fun setUp() {
    prefs = mockk()
    every { prefs.appLanguage }.returns(1)

    dateTimeManager = DateTimeManager(
      prefs = prefs,
      textProvider = mockk(),
      language = mockk(),
      nowDateTimeProvider = mockk()
    )
    weekFactory = WeekFactory(prefs, dateTimeManager)
  }

  @Test
  fun testWeekBuild_whenStartDayIsSunday_andCurrentDayIsSame() {
    every { prefs.startDay }.returns(0)

    val localDate = LocalDate.of(2023, 8, 20)
    val weekdays = weekFactory.createWeek(localDate)

    println(weekdays)

    val expected = listOf(
      WeekDay(weekday = "Sun", date = "20", isSelected = true, localDate = localDate),
      WeekDay(weekday = "Mon", date = "21", isSelected = false, localDate = localDate.plusDays(1)),
      WeekDay(weekday = "Tue", date = "22", isSelected = false, localDate = localDate.plusDays(2)),
      WeekDay(weekday = "Wed", date = "23", isSelected = false, localDate = localDate.plusDays(3)),
      WeekDay(weekday = "Thu", date = "24", isSelected = false, localDate = localDate.plusDays(4)),
      WeekDay(weekday = "Fri", date = "25", isSelected = false, localDate = localDate.plusDays(5)),
      WeekDay(weekday = "Sat", date = "26", isSelected = false, localDate = localDate.plusDays(6))
    )

    Assert.assertEquals(expected, weekdays)
  }

  @Test
  fun testWeekBuild_whenStartDayIsSunday_andCurrentDayIsNotTheSame() {
    every { prefs.startDay }.returns(0)

    val localDate = LocalDate.of(2023, 8, 22)
    val startDate = LocalDate.of(2023, 8, 20)
    val weekdays = weekFactory.createWeek(localDate)

    println(weekdays)

    val expected = listOf(
      WeekDay(weekday = "Sun", date = "20", isSelected = false, localDate = startDate),
      WeekDay(weekday = "Mon", date = "21", isSelected = false, localDate = startDate.plusDays(1)),
      WeekDay(weekday = "Tue", date = "22", isSelected = true, localDate = startDate.plusDays(2)),
      WeekDay(weekday = "Wed", date = "23", isSelected = false, localDate = startDate.plusDays(3)),
      WeekDay(weekday = "Thu", date = "24", isSelected = false, localDate = startDate.plusDays(4)),
      WeekDay(weekday = "Fri", date = "25", isSelected = false, localDate = startDate.plusDays(5)),
      WeekDay(weekday = "Sat", date = "26", isSelected = false, localDate = startDate.plusDays(6))
    )

    Assert.assertEquals(expected, weekdays)
  }

  @Test
  fun testWeekBuild_whenStartDayIsMonday_andCurrentDayIsSame() {
    every { prefs.startDay }.returns(1)

    val localDate = LocalDate.of(2023, 8, 21)
    val weekdays = weekFactory.createWeek(localDate)

    println(weekdays)

    val expected = listOf(
      WeekDay(weekday = "Mon", date = "21", isSelected = true, localDate = localDate),
      WeekDay(weekday = "Tue", date = "22", isSelected = false, localDate = localDate.plusDays(1)),
      WeekDay(weekday = "Wed", date = "23", isSelected = false, localDate = localDate.plusDays(2)),
      WeekDay(weekday = "Thu", date = "24", isSelected = false, localDate = localDate.plusDays(3)),
      WeekDay(weekday = "Fri", date = "25", isSelected = false, localDate = localDate.plusDays(4)),
      WeekDay(weekday = "Sat", date = "26", isSelected = false, localDate = localDate.plusDays(5)),
      WeekDay(weekday = "Sun", date = "27", isSelected = false, localDate = localDate.plusDays(6))
    )

    Assert.assertEquals(expected, weekdays)
  }

  @Test
  fun testWeekBuild_whenStartDayIsMonday_andCurrentDayIsNotTheSame() {
    every { prefs.startDay }.returns(1)

    val startDate = LocalDate.of(2023, 8, 21)
    val localDate = LocalDate.of(2023, 8, 22)
    val weekdays = weekFactory.createWeek(localDate)

    println(weekdays)

    val expected = listOf(
      WeekDay(weekday = "Mon", date = "21", isSelected = false, localDate = startDate),
      WeekDay(weekday = "Tue", date = "22", isSelected = true, localDate = startDate.plusDays(1)),
      WeekDay(weekday = "Wed", date = "23", isSelected = false, localDate = startDate.plusDays(2)),
      WeekDay(weekday = "Thu", date = "24", isSelected = false, localDate = startDate.plusDays(3)),
      WeekDay(weekday = "Fri", date = "25", isSelected = false, localDate = startDate.plusDays(4)),
      WeekDay(weekday = "Sat", date = "26", isSelected = false, localDate = startDate.plusDays(5)),
      WeekDay(weekday = "Sun", date = "27", isSelected = false, localDate = startDate.plusDays(6))
    )

    Assert.assertEquals(expected, weekdays)
  }
}
