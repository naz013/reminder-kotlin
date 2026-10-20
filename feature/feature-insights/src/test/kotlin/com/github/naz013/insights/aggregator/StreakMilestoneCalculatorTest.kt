package com.github.naz013.insights.aggregator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StreakMilestoneCalculatorTest {

  @Test
  fun `no milestone is reached at zero days and the first one is 7 days away`() {
    val result = StreakMilestoneCalculator.calculate(0)

    assertNull(result.reachedDays)
    assertEquals(7, result.nextDays)
    assertEquals(7, result.daysToNext)
    assertEquals(0f, result.progress, FLOAT_DELTA)
  }

  @Test
  fun `progress toward the first milestone is partial at six days`() {
    val result = StreakMilestoneCalculator.calculate(6)

    assertNull(result.reachedDays)
    assertEquals(7, result.nextDays)
    assertEquals(1, result.daysToNext)
    assertEquals(6f / 7f, result.progress, FLOAT_DELTA)
  }

  @Test
  fun `seven days reaches the first milestone and starts the next segment at zero progress`() {
    val result = StreakMilestoneCalculator.calculate(7)

    assertEquals(7, result.reachedDays)
    assertEquals(30, result.nextDays)
    assertEquals(23, result.daysToNext)
    assertEquals(0f, result.progress, FLOAT_DELTA)
  }

  @Test
  fun `twenty nine days is one day short of the 30 day milestone`() {
    val result = StreakMilestoneCalculator.calculate(29)

    assertEquals(7, result.reachedDays)
    assertEquals(30, result.nextDays)
    assertEquals(1, result.daysToNext)
    assertEquals(22f / 23f, result.progress, FLOAT_DELTA)
  }

  @Test
  fun `thirty days reaches the second milestone and starts the 100 day segment`() {
    val result = StreakMilestoneCalculator.calculate(30)

    assertEquals(30, result.reachedDays)
    assertEquals(100, result.nextDays)
    assertEquals(70, result.daysToNext)
    assertEquals(0f, result.progress, FLOAT_DELTA)
  }

  @Test
  fun `ninety nine days is one day short of the 100 day milestone`() {
    val result = StreakMilestoneCalculator.calculate(99)

    assertEquals(30, result.reachedDays)
    assertEquals(100, result.nextDays)
    assertEquals(1, result.daysToNext)
    assertEquals(69f / 70f, result.progress, FLOAT_DELTA)
  }

  @Test
  fun `one hundred days reaches the final milestone with no next target and full progress`() {
    val result = StreakMilestoneCalculator.calculate(100)

    assertEquals(100, result.reachedDays)
    assertNull(result.nextDays)
    assertEquals(0, result.daysToNext)
    assertEquals(1f, result.progress, FLOAT_DELTA)
  }

  @Test
  fun `streaks beyond one hundred days stay at the final milestone`() {
    val result = StreakMilestoneCalculator.calculate(150)

    assertEquals(100, result.reachedDays)
    assertNull(result.nextDays)
    assertEquals(0, result.daysToNext)
    assertEquals(1f, result.progress, FLOAT_DELTA)
  }

  @Test
  fun `negative streaks are treated as zero days`() {
    val result = StreakMilestoneCalculator.calculate(-3)

    assertNull(result.reachedDays)
    assertEquals(7, result.nextDays)
    assertEquals(7, result.daysToNext)
    assertEquals(0f, result.progress, FLOAT_DELTA)
  }

  private companion object {
    const val FLOAT_DELTA = 1e-6f
  }
}
