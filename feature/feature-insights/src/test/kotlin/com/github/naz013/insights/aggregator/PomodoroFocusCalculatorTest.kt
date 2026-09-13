package com.github.naz013.insights.aggregator

import com.github.naz013.domain.pomodoro.PomodoroSessionRecord
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class PomodoroFocusCalculatorTest {

  private val today = LocalDate.of(2026, 8, 2)

  private fun record(
    date: LocalDate,
    actualFocusSeconds: Int = 1500,
    wasCompleted: Boolean = true,
  ) = PomodoroSessionRecord(
    id = "$date-$wasCompleted-$actualFocusSeconds",
    linkedReminderId = null,
    startedAt = date.atTime(LocalTime.NOON),
    plannedFocusSeconds = 1500,
    actualFocusSeconds = actualFocusSeconds,
    wasCompleted = wasCompleted,
  )

  @Test
  fun `total focus seconds sums every record regardless of completion`() {
    val records = listOf(
      record(today, actualFocusSeconds = 600, wasCompleted = false),
      record(today.minusDays(1), actualFocusSeconds = 1500, wasCompleted = true),
    )

    val result = PomodoroFocusCalculator.calculate(records, today)

    assertEquals(2100, result.totalFocusSeconds)
  }

  @Test
  fun `an interrupted session does not count toward the streak`() {
    val records = listOf(record(today, wasCompleted = false))

    val result = PomodoroFocusCalculator.calculate(records, today)

    assertEquals(0, result.currentStreakDays)
  }

  @Test
  fun `current streak counts consecutive completed days ending today`() {
    val records = listOf(
      record(today.minusDays(2)),
      record(today.minusDays(1)),
      record(today),
    )

    val result = PomodoroFocusCalculator.calculate(records, today)

    assertEquals(3, result.currentStreakDays)
  }

  @Test
  fun `current streak still counts when today has no completed session yet`() {
    val records = listOf(
      record(today.minusDays(2)),
      record(today.minusDays(1)),
    )

    val result = PomodoroFocusCalculator.calculate(records, today)

    assertEquals(2, result.currentStreakDays)
  }

  @Test
  fun `current streak is broken by a gap of more than one day`() {
    val records = listOf(
      record(today.minusDays(5)),
      record(today.minusDays(1)),
      record(today),
    )

    val result = PomodoroFocusCalculator.calculate(records, today)

    assertEquals(2, result.currentStreakDays)
  }

  @Test
  fun `longest streak can exceed the current streak`() {
    val records = listOf(
      record(today.minusDays(10)),
      record(today.minusDays(9)),
      record(today.minusDays(8)),
      record(today.minusDays(7)),
      record(today),
    )

    val result = PomodoroFocusCalculator.calculate(records, today)

    assertEquals(4, result.longestStreakDays)
    assertEquals(1, result.currentStreakDays)
  }

  @Test
  fun `multiple completed sessions on the same day only count once toward the streak`() {
    val records = listOf(
      record(today),
      record(today),
      record(today.minusDays(1)),
    )

    val result = PomodoroFocusCalculator.calculate(records, today)

    assertEquals(2, result.currentStreakDays)
  }
}
