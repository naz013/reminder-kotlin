package com.github.naz013.logic.habit.nudge

import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitFrequency
import com.github.naz013.logic.habit.HabitWithProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class HabitNudgeCalculatorTest {
  private val stamp = LocalDateTime.of(2026, 10, 1, 8, 0)
  private val calculator = HabitNudgeCalculator()

  private fun habit(
    frequency: HabitFrequency = HabitFrequency.DAILY,
    target: Int = 1,
    time: LocalTime? = LocalTime.of(20, 0),
    onlyIfNotDone: Boolean = true
  ) = Habit(
    id = "h-1",
    frequency = frequency,
    targetCount = target,
    reminderTime = time,
    remindOnlyIfNotDone = onlyIfNotDone,
    createdAt = stamp,
    updatedAt = stamp
  )

  private fun progress(habit: Habit, today: Int, period: Int) =
    HabitWithProgress(habit = habit, todayCount = today, periodCount = period, streak = 0)

  @Test
  fun `returns null when habit has no reminder time`() {
    val result = calculator.nextTrigger(habit(time = null), LocalDateTime.of(2026, 10, 20, 9, 0))

    assertNull(result)
  }

  @Test
  fun `returns today at reminder time when it is still ahead`() {
    val result = calculator.nextTrigger(habit(), LocalDateTime.of(2026, 10, 20, 9, 0))

    assertEquals(LocalDateTime.of(2026, 10, 20, 20, 0), result)
  }

  @Test
  fun `returns tomorrow when reminder time already passed`() {
    val result = calculator.nextTrigger(habit(), LocalDateTime.of(2026, 10, 20, 21, 0))

    assertEquals(LocalDateTime.of(2026, 10, 21, 20, 0), result)
  }

  @Test
  fun `returns tomorrow when now equals reminder time`() {
    val result = calculator.nextTrigger(habit(), LocalDateTime.of(2026, 10, 20, 20, 0))

    assertEquals(LocalDateTime.of(2026, 10, 21, 20, 0), result)
  }

  @Test
  fun `weekly habit is nudged on the next day too`() {
    val result = calculator.nextTrigger(
      habit(frequency = HabitFrequency.WEEKLY, target = 3),
      LocalDateTime.of(2026, 10, 25, 22, 0)
    )

    assertEquals(LocalDateTime.of(2026, 10, 26, 20, 0), result)
  }

  @Test
  fun `notifies when daily target is not reached`() {
    val h = habit(target = 2)

    assertTrue(calculator.shouldNotify(progress(h, today = 1, period = 1)))
  }

  @Test
  fun `skips when daily target is reached and only-if-not-done is set`() {
    val h = habit(target = 2)

    assertFalse(calculator.shouldNotify(progress(h, today = 2, period = 2)))
  }

  @Test
  fun `notifies when daily target is reached but only-if-not-done is off`() {
    val h = habit(onlyIfNotDone = false)

    assertTrue(calculator.shouldNotify(progress(h, today = 1, period = 1)))
  }

  @Test
  fun `skips weekly habit already checked in today`() {
    val h = habit(frequency = HabitFrequency.WEEKLY, target = 3)

    assertFalse(calculator.shouldNotify(progress(h, today = 1, period = 1)))
  }

  @Test
  fun `skips weekly habit whose weekly target is reached`() {
    val h = habit(frequency = HabitFrequency.WEEKLY, target = 3)

    assertFalse(calculator.shouldNotify(progress(h, today = 0, period = 3)))
  }

  @Test
  fun `notifies weekly habit with nothing logged today and target not reached`() {
    val h = habit(frequency = HabitFrequency.WEEKLY, target = 3)

    assertTrue(calculator.shouldNotify(progress(h, today = 0, period = 1)))
  }
}
