package com.github.naz013.datecalc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.threeten.bp.DayOfWeek

class WeekDaysProtocolTest {

  @Test
  fun `getSelectedDaysOfWeek returns only the days marked as selected`() {
    val weekdays = listOf(0, 1, 0, 1, 0, 1, 0) // Mon, Wed, Fri

    val result = WeekDaysProtocol.getSelectedDaysOfWeek(weekdays)

    assertEquals(listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), result)
  }

  @Test
  fun `getSelectedDaysOfWeek returns an empty list when nothing is selected`() {
    val result = WeekDaysProtocol.getSelectedDaysOfWeek(listOf(0, 0, 0, 0, 0, 0, 0))

    assertEquals(emptyList<DayOfWeek>(), result)
  }

  @Test
  fun `getSelectedDaysOfWeek throws when fewer than seven entries are provided`() {
    assertThrows(IndexOutOfBoundsException::class.java) {
      WeekDaysProtocol.getSelectedDaysOfWeek(listOf(1, 1, 1))
    }
  }

  @Test
  fun `getWorkDays marks Monday through Friday`() {
    assertEquals(listOf(0, 1, 1, 1, 1, 1, 0), WeekDaysProtocol.getWorkDays())
  }

  @Test
  fun `getWeekend marks Saturday and Sunday`() {
    assertEquals(listOf(1, 0, 0, 0, 0, 0, 1), WeekDaysProtocol.getWeekend())
  }

  @Test
  fun `getAllDays marks every day`() {
    assertEquals(listOf(1, 1, 1, 1, 1, 1, 1), WeekDaysProtocol.getAllDays())
  }
}
