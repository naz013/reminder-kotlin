package com.github.naz013.logic.routine

import com.github.naz013.domain.routine.RoutineStep
import org.junit.Assert.assertEquals
import org.junit.Test

class RoutineDurationCalculatorTest {

  private val calculator = RoutineDurationCalculator()

  @Test
  fun `calculateTotalDuration sums durations of all steps`() {
    val steps = listOf(
      RoutineStep(durationSeconds = 300),
      RoutineStep(durationSeconds = 600),
      RoutineStep(durationSeconds = 0)
    )

    assertEquals(900, calculator.calculateTotalDuration(steps))
  }

  @Test
  fun `calculateRemainingDuration excludes completed steps`() {
    val steps = listOf(
      RoutineStep(durationSeconds = 300, isCompleted = true),
      RoutineStep(durationSeconds = 600, isCompleted = false),
      RoutineStep(durationSeconds = 120, isCompleted = false)
    )

    assertEquals(720, calculator.calculateRemainingDuration(steps))
  }

  @Test
  fun `formatDuration returns 0m for zero or negative seconds`() {
    assertEquals("0m", calculator.formatDuration(0))
    assertEquals("0m", calculator.formatDuration(-5))
  }

  @Test
  fun `formatDuration formats seconds only under a minute`() {
    assertEquals("45s", calculator.formatDuration(45))
  }

  @Test
  fun `formatDuration formats whole minutes`() {
    assertEquals("10m", calculator.formatDuration(600))
  }

  @Test
  fun `formatDuration formats whole hours`() {
    assertEquals("2h", calculator.formatDuration(7200))
  }

  @Test
  fun `formatDuration formats hours and minutes together`() {
    assertEquals("1h 10m", calculator.formatDuration(4200))
  }

  @Test
  fun `formatDuration drops leftover seconds once minutes are present`() {
    assertEquals("25m", calculator.formatDuration(1505))
  }
}
