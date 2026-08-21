package com.github.naz013.insights.aggregator

import com.github.naz013.domain.routine.RoutineExecutionRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime

class RoutineStepDropoffCalculatorTest {

  private fun record(completedStepIds: List<String>, totalStepsCount: Int) = RoutineExecutionRecord(
    routineId = "o1",
    executedAt = LocalDateTime.of(2026, 8, 2, 9, 0),
    totalTimeSpentSeconds = 0,
    completedStepIds = completedStepIds,
    totalStepsCount = totalStepsCount
  )

  @Test
  fun `returns an empty list when there are no records`() {
    val result = RoutineStepDropoffCalculator.calculate(emptyList(), stepIds = listOf("s1"))

    assertTrue(result.isEmpty())
  }

  @Test
  fun `a step completed in every run has a 100 percent completion rate`() {
    val records = listOf(
      record(listOf("s1"), totalStepsCount = 1),
      record(listOf("s1"), totalStepsCount = 1),
    )

    val result = RoutineStepDropoffCalculator.calculate(records, stepIds = listOf("s1"))

    assertEquals(1f, result.single().completionRate)
    assertEquals(2, result.single().totalRuns)
    assertEquals(2, result.single().completedRuns)
  }

  @Test
  fun `a step never completed has a 0 percent completion rate`() {
    val records = listOf(
      record(listOf("s1"), totalStepsCount = 2),
      record(listOf("s1"), totalStepsCount = 2),
    )

    val result = RoutineStepDropoffCalculator.calculate(records, stepIds = listOf("s1", "s2"))

    val s2 = result.single { it.stepId == "s2" }
    assertEquals(0f, s2.completionRate)
    assertEquals(0, s2.completedRuns)
  }

  @Test
  fun `computes an independent completion rate per step`() {
    val records = listOf(
      record(listOf("s1", "s2"), totalStepsCount = 2),
      record(listOf("s1"), totalStepsCount = 2),
      record(listOf("s1"), totalStepsCount = 2),
      record(listOf("s1"), totalStepsCount = 2),
    )

    val result = RoutineStepDropoffCalculator.calculate(records, stepIds = listOf("s1", "s2"))
      .associateBy { it.stepId }

    assertEquals(1f, result.getValue("s1").completionRate)
    assertEquals(0.25f, result.getValue("s2").completionRate)
  }

  @Test
  fun `excludes step ids not passed in even if referenced by a record`() {
    val records = listOf(record(listOf("removed-step"), totalStepsCount = 1))

    val result = RoutineStepDropoffCalculator.calculate(records, stepIds = listOf("s1"))

    assertEquals(1, result.size)
    assertEquals("s1", result.single().stepId)
    assertEquals(0f, result.single().completionRate)
  }
}
