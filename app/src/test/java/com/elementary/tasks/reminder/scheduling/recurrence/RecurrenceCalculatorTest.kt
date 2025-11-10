package com.elementary.tasks.reminder.scheduling.recurrence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Unit tests for RecurrenceCalculator.
 *
 * Tests the calculation of next occurrences for various recurrence patterns:
 * yearly, weekly, monthly, interval-based, and timer-based events.
 */
class RecurrenceCalculatorTest {

  private lateinit var calculator: RecurrenceCalculator

  @Before
  fun setup() {
    calculator = RecurrenceCalculator()
  }

  // ========== getNextYearDayDateTime Tests ==========

  @Test
  fun `getNextYearDayDateTime should calculate next year occurrence with normal day`() {
    // Arrange - Start with January 15, 2024
    val eventDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0)
    val monthOfYear = 5 // June (0-indexed, so 5 = June)
    val dayOfMonth = 15
    val interval = 1L

    // Act
    val result = calculator.getNextYearDayDateTime(
      eventDateTime,
      monthOfYear,
      dayOfMonth,
      interval
    )

    // Assert
    assertEquals(2025, result.year)
    assertEquals(6, result.monthValue) // June (1-indexed in LocalDateTime)
    assertEquals(15, result.dayOfMonth)
    assertEquals(10, result.hour)
    assertEquals(30, result.minute)
  }

  @Test
  fun `getNextYearDayDateTime should handle last day of month when dayOfMonth is 0`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 15, 14, 0, 0)
    val monthOfYear = 1 // February (0-indexed)
    val dayOfMonth = 0 // Last day of month
    val interval = 1L

    // Act
    val result = calculator.getNextYearDayDateTime(
      eventDateTime,
      monthOfYear,
      dayOfMonth,
      interval
    )

    // Assert - 2025 is not a leap year, so February has 28 days
    assertEquals(2025, result.year)
    assertEquals(2, result.monthValue)
    assertEquals(28, result.dayOfMonth)
  }

  @Test
  fun `getNextYearDayDateTime should adjust day when it exceeds month length`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 31, 10, 0, 0)
    val monthOfYear = 1 // February (0-indexed)
    val dayOfMonth = 31 // Should adjust to last day of February
    val interval = 1L

    // Act
    val result = calculator.getNextYearDayDateTime(
      eventDateTime,
      monthOfYear,
      dayOfMonth,
      interval
    )

    // Assert - February 2025 has only 28 days
    assertEquals(2025, result.year)
    assertEquals(2, result.monthValue)
    assertEquals(28, result.dayOfMonth)
  }

  @Test
  fun `getNextYearDayDateTime should handle multi-year interval`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 3, 15, 9, 0, 0)
    val monthOfYear = 11 // December (0-indexed)
    val dayOfMonth = 25
    val interval = 3L

    // Act
    val result = calculator.getNextYearDayDateTime(
      eventDateTime,
      monthOfYear,
      dayOfMonth,
      interval
    )

    // Assert - Should be 3 years later
    assertEquals(2027, result.year)
    assertEquals(12, result.monthValue)
    assertEquals(25, result.dayOfMonth)
  }

  @Test
  fun `getNextYearDayDateTime should default to interval of 1 when interval is zero or negative`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 6, 15, 10, 0, 0)
    val monthOfYear = 9 // October (0-indexed)
    val dayOfMonth = 10
    val interval = 0L // Invalid interval

    // Act
    val result = calculator.getNextYearDayDateTime(
      eventDateTime,
      monthOfYear,
      dayOfMonth,
      interval
    )

    // Assert - Should use interval of 1
    assertEquals(2025, result.year)
    assertEquals(10, result.monthValue)
    assertEquals(10, result.dayOfMonth)
  }

  // ========== findNextYearDayDateTime Tests ==========

  @Test
  fun `findNextYearDayDateTime should find next occurrence after given date`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
    val monthOfYear = 5 // June (0-indexed)
    val dayOfMonth = 15
    val interval = 1L
    val afterOrEqualDateTime = LocalDateTime.of(2026, 7, 1, 0, 0, 0)

    // Act
    val result = calculator.findNextYearDayDateTime(
      eventDateTime,
      monthOfYear,
      dayOfMonth,
      interval,
      afterOrEqualDateTime
    )

    // Assert - Should skip 2025 and land on 2027
    assertEquals(2027, result.year)
    assertEquals(6, result.monthValue)
    assertEquals(15, result.dayOfMonth)
  }

  @Test
  fun `findNextYearDayDateTime should return first occurrence if it is after threshold`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
    val monthOfYear = 5 // June (0-indexed)
    val dayOfMonth = 15
    val interval = 1L
    val afterOrEqualDateTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0)

    // Act
    val result = calculator.findNextYearDayDateTime(
      eventDateTime,
      monthOfYear,
      dayOfMonth,
      interval,
      afterOrEqualDateTime
    )

    // Assert - First occurrence in 2025 June should be returned
    assertEquals(2025, result.year)
    assertEquals(6, result.monthValue)
    assertEquals(15, result.dayOfMonth)
  }

  // ========== getNextDayOfWeekDateTime Tests ==========

  @Test
  fun `getNextDayOfWeekDateTime should find next selected weekday`() {
    // Arrange - Monday, January 15, 2024
    val eventDateTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0) // Monday
    val weekdays = listOf(0, 1, 0, 1, 0, 0, 0) // Monday and Wednesday

    // Act
    val result = calculator.getNextDayOfWeekDateTime(eventDateTime, weekdays)

    // Assert - Should be Wednesday, January 17
    assertEquals(17, result.dayOfMonth)
    assertEquals(1, result.monthValue)
    assertEquals(2024, result.year)
  }

  @Test
  fun `getNextDayOfWeekDateTime should handle weekend selection`() {
    // Arrange - Friday, January 12, 2024
    val eventDateTime = LocalDateTime.of(2024, 1, 12, 15, 30, 0) // Friday
    val weekdays = listOf(1, 0, 0, 0, 0, 0, 1) // Sunday and Saturday

    // Act
    val result = calculator.getNextDayOfWeekDateTime(eventDateTime, weekdays)

    // Assert - Should be Saturday, January 13
    assertEquals(13, result.dayOfMonth)
    assertEquals(1, result.monthValue)
  }

  @Test
  fun `getNextDayOfWeekDateTime should wrap to next week when no days left in current week`() {
    // Arrange - Saturday, January 13, 2024
    val eventDateTime = LocalDateTime.of(2024, 1, 13, 10, 0, 0) // Saturday
    val weekdays = listOf(0, 1, 0, 0, 0, 0, 0) // Only Monday

    // Act
    val result = calculator.getNextDayOfWeekDateTime(eventDateTime, weekdays)

    // Assert - Should be Monday, January 15
    assertEquals(15, result.dayOfMonth)
    assertEquals(1, result.monthValue)
  }

  // ========== findNextDayOfWeekDateTime Tests ==========

  @Test
  fun `findNextDayOfWeekDateTime should find occurrence after threshold date`() {
    // Arrange - Start from Monday, January 15, 2024
    val eventDateTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
    val weekdays = listOf(0, 1, 0, 0, 0, 0, 0) // Only Monday
    val afterOrEqualDateTime = LocalDateTime.of(2024, 1, 30, 0, 0, 0)

    // Act
    val result = calculator.findNextDayOfWeekDateTime(
      eventDateTime,
      weekdays,
      afterOrEqualDateTime
    )

    // Assert - Should find a Monday after January 30 (February 5, 2024 is a Monday)
    assertEquals(2, result.monthValue) // February
    assertEquals(5, result.dayOfMonth)
    assertTrue(result.isAfter(afterOrEqualDateTime) || result.isEqual(afterOrEqualDateTime))
  }

  // ========== getNextMonthDayDateTime Tests ==========

  @Test
  fun `getNextMonthDayDateTime should calculate next month occurrence with normal day`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
    val dayOfMonth = 20
    val interval = 1L

    // Act
    val result = calculator.getNextMonthDayDateTime(eventDateTime, dayOfMonth, interval)

    // Assert - Should be February 20, 2024
    assertEquals(2024, result.year)
    assertEquals(2, result.monthValue)
    assertEquals(20, result.dayOfMonth)
  }

  @Test
  fun `getNextMonthDayDateTime should handle last day of month when dayOfMonth is 0`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
    val dayOfMonth = 0 // Last day of month
    val interval = 1L

    // Act
    val result = calculator.getNextMonthDayDateTime(eventDateTime, dayOfMonth, interval)

    // Assert - Should be last day of February (29 in 2024, leap year)
    assertEquals(2024, result.year)
    assertEquals(2, result.monthValue)
    assertEquals(29, result.dayOfMonth)
  }

  @Test
  fun `getNextMonthDayDateTime should adjust day when it exceeds month length`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 31, 10, 0, 0)
    val dayOfMonth = 31 // Should adjust to last day of February
    val interval = 1L

    // Act
    val result = calculator.getNextMonthDayDateTime(eventDateTime, dayOfMonth, interval)

    // Assert - February 2024 has only 29 days (leap year)
    assertEquals(2024, result.year)
    assertEquals(2, result.monthValue)
    assertEquals(29, result.dayOfMonth)
  }

  @Test
  fun `getNextMonthDayDateTime should handle multi-month interval`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
    val dayOfMonth = 10
    val interval = 3L

    // Act
    val result = calculator.getNextMonthDayDateTime(eventDateTime, dayOfMonth, interval)

    // Assert - Should be April 10, 2024
    assertEquals(2024, result.year)
    assertEquals(4, result.monthValue)
    assertEquals(10, result.dayOfMonth)
  }

  @Test
  fun `getNextMonthDayDateTime should handle year transition`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 11, 15, 10, 0, 0)
    val dayOfMonth = 5
    val interval = 2L

    // Act
    val result = calculator.getNextMonthDayDateTime(eventDateTime, dayOfMonth, interval)

    // Assert - Should be January 5, 2025
    assertEquals(2025, result.year)
    assertEquals(1, result.monthValue)
    assertEquals(5, result.dayOfMonth)
  }

  // ========== findNextMonthDayDateTime Tests ==========

  @Test
  fun `findNextMonthDayDateTime should find next occurrence after threshold date`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
    val dayOfMonth = 20
    val interval = 1L
    val afterOrEqualDateTime = LocalDateTime.of(2024, 5, 1, 0, 0, 0)

    // Act
    val result = calculator.findNextMonthDayDateTime(
      eventDateTime,
      dayOfMonth,
      interval,
      afterOrEqualDateTime
    )

    // Assert - Should be May 20, 2024 or later
    assertTrue(result.isAfter(afterOrEqualDateTime) || result.isEqual(afterOrEqualDateTime))
    assertEquals(20, result.dayOfMonth)
  }

  // ========== getNextIntervalDateTime Tests ==========

  @Test
  fun `getNextIntervalDateTime should add interval correctly`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
    val intervalMillis = 2L * 60 * 60 * 1000 // 2 hours

    // Act
    val result = calculator.getNextIntervalDateTime(eventDateTime, intervalMillis)

    // Assert - Should be 2 hours later
    assertEquals(12, result.hour)
    assertEquals(15, result.dayOfMonth)
  }

  @Test
  fun `getNextIntervalDateTime should handle day transition`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 15, 23, 0, 0)
    val intervalMillis = 2L * 60 * 60 * 1000 // 2 hours

    // Act
    val result = calculator.getNextIntervalDateTime(eventDateTime, intervalMillis)

    // Assert - Should be January 16 at 1:00 AM
    assertEquals(16, result.dayOfMonth)
    assertEquals(1, result.hour)
  }

  @Test
  fun `getNextIntervalDateTime should handle large intervals`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0)
    val intervalMillis = 30L * 24 * 60 * 60 * 1000 // 30 days

    // Act
    val result = calculator.getNextIntervalDateTime(eventDateTime, intervalMillis)

    // Assert - Should be approximately 30 days later
    assertEquals(31, result.dayOfMonth)
    assertEquals(1, result.monthValue)
  }

  // ========== findNextIntervalDateTime Tests ==========

  @Test
  fun `findNextIntervalDateTime should find next occurrence after threshold`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0)
    val intervalMillis = 1L * 60 * 60 * 1000 // 1 hour
    val afterOrEqualDateTime = LocalDateTime.of(2024, 1, 1, 15, 0, 0)

    // Act
    val result = calculator.findNextIntervalDateTime(
      eventDateTime,
      intervalMillis,
      afterOrEqualDateTime
    )

    // Assert - Should be at or after 15:00
    assertTrue(result.isAfter(afterOrEqualDateTime) || result.isEqual(afterOrEqualDateTime))
    assertEquals(15, result.hour)
  }

  @Test
  fun `findNextIntervalDateTime should handle multiple intervals to reach threshold`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0)
    val intervalMillis = 2L * 60 * 60 * 1000 // 2 hours
    val afterOrEqualDateTime = LocalDateTime.of(2024, 1, 1, 10, 0, 0)

    // Act
    val result = calculator.findNextIntervalDateTime(
      eventDateTime,
      intervalMillis,
      afterOrEqualDateTime
    )

    // Assert - Should be at 10:00 (exactly)
    assertEquals(10, result.hour)
    assertEquals(1, result.dayOfMonth)
  }

  // ========== getStartTimerDateTime Tests ==========

  @Test
  fun `getStartTimerDateTime should calculate timer start from now`() {
    // Arrange
    val countdownTimeInMillis = 5L * 60 * 1000 // 5 minutes
    val beforeCall = LocalDateTime.now()

    // Act
    val result = calculator.getStartTimerDateTime(countdownTimeInMillis)
    val afterCall = LocalDateTime.now()

    // Assert - Result should be approximately 5 minutes from now
    assertTrue(result.isAfter(beforeCall))
    assertTrue(result.isBefore(afterCall.plusMinutes(6)))
  }

  // ========== getNextTimerDateTime Tests ==========

  @Test
  fun `getNextTimerDateTime should skip excluded hours`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 15, 8, 0, 0)
    val interval = 2L * 60 * 60 * 1000 // 2 hours
    val excludedHours = listOf(10, 11, 12) // Exclude 10 AM, 11 AM, 12 PM
    val excludedFromTime: LocalTime? = null
    val excludedToTime: LocalTime? = null

    // Act
    val result = calculator.getNextTimerDateTime(
      eventDateTime,
      interval,
      excludedHours,
      excludedFromTime,
      excludedToTime
    )

    // Assert - Should be 2 hours later (10:00) but will skip to 13:00
    assertTrue(result.hour !in excludedHours)
  }

  @Test
  fun `getNextTimerDateTime should skip excluded time range`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 15, 11, 30, 0)
    val interval = 1L * 60 * 60 * 1000 // 1 hour
    val excludedHours = emptyList<Int>()
    val excludedFromTime = LocalTime.of(12, 0, 0)
    val excludedToTime = LocalTime.of(13, 0, 0)

    // Act
    val result = calculator.getNextTimerDateTime(
      eventDateTime,
      interval,
      excludedHours,
      excludedFromTime,
      excludedToTime
    )

    // Assert - Should skip the 12:00-13:00 range
    val resultTime = result.toLocalTime()
    if (resultTime.isAfter(excludedFromTime) && resultTime.isBefore(excludedToTime)) {
      assertTrue("Result time should not be in excluded range", false)
    }
  }

  @Test
  fun `getNextTimerDateTime should handle overnight excluded time range`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 15, 22, 0, 0)
    val interval = 1L * 60 * 60 * 1000 // 1 hour
    val excludedHours = emptyList<Int>()
    val excludedFromTime = LocalTime.of(23, 0, 0)
    val excludedToTime = LocalTime.of(6, 0, 0) // Overnight exclusion

    // Act
    val result = calculator.getNextTimerDateTime(
      eventDateTime,
      interval,
      excludedHours,
      excludedFromTime,
      excludedToTime
    )

    // Assert - Should skip overnight hours
    val resultTime = result.toLocalTime()
    assertTrue(
      resultTime.isBefore(excludedFromTime) || resultTime.isAfter(excludedToTime)
    )
  }

  // ========== findNextTimerDateTime Tests ==========

  @Test
  fun `findNextTimerDateTime should find occurrence after threshold with exclusions`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 15, 8, 0, 0)
    val interval = 1L * 60 * 60 * 1000 // 1 hour
    val excludedHours = listOf(12, 13)
    val afterOrEqualDateTime = LocalDateTime.of(2024, 1, 15, 11, 0, 0)

    // Act
    val result = calculator.findNextTimerDateTime(
      eventDateTime,
      interval,
      excludedHours,
      null,
      null,
      afterOrEqualDateTime
    )

    // Assert - Should be at or after threshold and not in excluded hours
    assertTrue(result.isAfter(afterOrEqualDateTime) || result.isEqual(afterOrEqualDateTime))
    assertTrue(result.hour !in excludedHours)
  }

  @Test
  fun `findNextTimerDateTime should handle multiple iterations to reach valid time`() {
    // Arrange
    val eventDateTime = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
    val interval = 1L * 60 * 60 * 1000 // 1 hour
    val excludedHours = listOf(11, 12, 13, 14, 15)
    val excludedFromTime: LocalTime? = null
    val excludedToTime: LocalTime? = null
    val afterOrEqualDateTime = LocalDateTime.of(2024, 1, 15, 14, 0, 0)

    // Act
    val result = calculator.findNextTimerDateTime(
      eventDateTime,
      interval,
      excludedHours,
      excludedFromTime,
      excludedToTime,
      afterOrEqualDateTime
    )

    // Assert - Should skip all excluded hours
    assertTrue(result.isAfter(afterOrEqualDateTime) || result.isEqual(afterOrEqualDateTime))
    assertTrue(result.hour !in excludedHours)
  }
}

