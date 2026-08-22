package com.github.naz013.insights.aggregator

import com.github.naz013.domain.routine.RoutineExecutionRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class RoutineStreakCalculatorTest {

  private val today = LocalDate.of(2026, 8, 2)

  private fun record(
    routineId: String,
    date: LocalDate,
    completedStepIds: List<String> = listOf("s1"),
    totalStepsCount: Int = 1
  ) = RoutineExecutionRecord(
    id = "$routineId-$date-${completedStepIds.size}",
    routineId = routineId,
    executedAt = date.atTime(LocalTime.NOON),
    totalTimeSpentSeconds = 0,
    completedStepIds = completedStepIds,
    totalStepsCount = totalStepsCount
  )

  @Test
  fun `a record with zero completed steps does not count toward the streak`() {
    val records = listOf(record("o1", today, completedStepIds = emptyList(), totalStepsCount = 3))

    val result = RoutineStreakCalculator.calculate(records, today)

    assertEquals(0, result.single().currentStreakDays)
    assertNull(result.single().lastCompletedDate)
  }

  @Test
  fun `a record with some but not all steps completed does not count toward the streak`() {
    val records = listOf(record("o1", today, completedStepIds = listOf("s1"), totalStepsCount = 3))

    val result = RoutineStreakCalculator.calculate(records, today)

    assertEquals(0, result.single().currentStreakDays)
  }

  @Test
  fun `a fully completed record counts toward the streak`() {
    val records = listOf(record("o1", today, completedStepIds = listOf("s1", "s2"), totalStepsCount = 2))

    val result = RoutineStreakCalculator.calculate(records, today)

    assertEquals(1, result.single().currentStreakDays)
    assertEquals(today, result.single().lastCompletedDate)
  }

  @Test
  fun `current streak counts consecutive fully-completed days ending today`() {
    val records = listOf(
      record("o1", today.minusDays(2)),
      record("o1", today.minusDays(1)),
      record("o1", today),
    )

    val result = RoutineStreakCalculator.calculate(records, today)

    assertEquals(3, result.single().currentStreakDays)
  }

  @Test
  fun `current streak still counts when today has not been completed yet`() {
    val records = listOf(
      record("o1", today.minusDays(2)),
      record("o1", today.minusDays(1)),
    )

    val result = RoutineStreakCalculator.calculate(records, today)

    assertEquals(2, result.single().currentStreakDays)
  }

  @Test
  fun `current streak is broken by a gap of more than one day`() {
    val records = listOf(
      record("o1", today.minusDays(5)),
      record("o1", today.minusDays(1)),
      record("o1", today),
    )

    val result = RoutineStreakCalculator.calculate(records, today)

    assertEquals(2, result.single().currentStreakDays)
  }

  @Test
  fun `longest streak can exceed the current streak`() {
    val records = listOf(
      record("o1", today.minusDays(10)),
      record("o1", today.minusDays(9)),
      record("o1", today.minusDays(8)),
      record("o1", today.minusDays(7)),
      record("o1", today),
    )

    val result = RoutineStreakCalculator.calculate(records, today)

    assertEquals(4, result.single().longestStreakDays)
    assertEquals(1, result.single().currentStreakDays)
  }

  @Test
  fun `computes streaks independently per routineId`() {
    val records = listOf(
      record("o1", today),
      record("o2", today.minusDays(10)),
    )

    val result = RoutineStreakCalculator.calculate(records, today).associateBy { it.routineId }

    assertEquals(1, result.getValue("o1").currentStreakDays)
    assertEquals(0, result.getValue("o2").currentStreakDays)
  }

  @Test
  fun `multiple completed records on the same day only count once toward the streak`() {
    val records = listOf(
      record("o1", today),
      record("o1", today),
      record("o1", today.minusDays(1)),
    )

    val result = RoutineStreakCalculator.calculate(records, today)

    assertTrue(result.single().currentStreakDays == 2)
  }
}
