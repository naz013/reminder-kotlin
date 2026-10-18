package com.github.naz013.logic.habit

import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitCheckIn
import com.github.naz013.domain.habit.HabitFrequency
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class HabitStreakCalculatorTest {
  private val calculator = HabitStreakCalculator()
  private val stamp = LocalDateTime.of(2026, 1, 1, 0, 0)

  // 2026-10-18 is a Sunday; its ISO week starts Monday 2026-10-12.
  private val today = LocalDate.of(2026, 10, 18)

  private fun habit(frequency: HabitFrequency, target: Int = 1) = Habit(
    id = "h",
    frequency = frequency,
    targetCount = target,
    createdAt = stamp,
    updatedAt = stamp
  )

  private fun checkIn(date: LocalDate, count: Int = 1) = HabitCheckIn(
    habitId = "h",
    date = date,
    count = count,
    isCompleted = true,
    createdAt = stamp,
    updatedAt = stamp
  )

  @Test
  fun `daily streak is zero with no check-ins`() {
    assertEquals(0, calculator.streak(habit(HabitFrequency.DAILY), emptyList(), today))
  }

  @Test
  fun `daily streak counts consecutive days ending today`() {
    val items = listOf(today, today.minusDays(1), today.minusDays(2)).map { checkIn(it) }

    assertEquals(3, calculator.streak(habit(HabitFrequency.DAILY), items, today))
  }

  @Test
  fun `daily streak is not broken when today is not done yet`() {
    val items = listOf(today.minusDays(1), today.minusDays(2)).map { checkIn(it) }

    assertEquals(2, calculator.streak(habit(HabitFrequency.DAILY), items, today))
  }

  @Test
  fun `daily streak is zero when neither today nor yesterday is done`() {
    val items = listOf(today.minusDays(2), today.minusDays(3)).map { checkIn(it) }

    assertEquals(0, calculator.streak(habit(HabitFrequency.DAILY), items, today))
  }

  @Test
  fun `daily streak stops at the first missed day`() {
    val items = listOf(today, today.minusDays(1), today.minusDays(3)).map { checkIn(it) }

    assertEquals(2, calculator.streak(habit(HabitFrequency.DAILY), items, today))
  }

  @Test
  fun `daily streak ignores days below the target count`() {
    val items = listOf(checkIn(today, 3), checkIn(today.minusDays(1), 2), checkIn(today.minusDays(2), 3))

    assertEquals(1, calculator.streak(habit(HabitFrequency.DAILY, target = 3), items, today))
  }

  @Test
  fun `daily streak treats an unfinished today as neutral for multi-count habits`() {
    val items = listOf(checkIn(today, 1), checkIn(today.minusDays(1), 2), checkIn(today.minusDays(2), 2))

    assertEquals(2, calculator.streak(habit(HabitFrequency.DAILY, target = 2), items, today))
  }

  @Test
  fun `weekly streak sums counts within an ISO week`() {
    val thisWeek = listOf(checkIn(LocalDate.of(2026, 10, 12)), checkIn(LocalDate.of(2026, 10, 18)))
    val lastWeek = listOf(checkIn(LocalDate.of(2026, 10, 5)), checkIn(LocalDate.of(2026, 10, 11)))

    val streak = calculator.streak(habit(HabitFrequency.WEEKLY, target = 2), thisWeek + lastWeek, today)

    assertEquals(2, streak)
  }

  @Test
  fun `weekly streak is not broken by an unfinished current week`() {
    val lastWeek = listOf(checkIn(LocalDate.of(2026, 10, 5)), checkIn(LocalDate.of(2026, 10, 6)))
    val thisWeek = listOf(checkIn(LocalDate.of(2026, 10, 12)))

    val streak = calculator.streak(habit(HabitFrequency.WEEKLY, target = 2), lastWeek + thisWeek, today)

    assertEquals(1, streak)
  }

  @Test
  fun `weekly streak stops at a week below target`() {
    val items = listOf(
      checkIn(LocalDate.of(2026, 10, 13)),
      checkIn(LocalDate.of(2026, 10, 6)),
      checkIn(LocalDate.of(2026, 9, 22))
    )

    assertEquals(2, calculator.streak(habit(HabitFrequency.WEEKLY), items, today))
  }

  @Test
  fun `weekly streak crosses a year boundary`() {
    val newYearToday = LocalDate.of(2026, 1, 2)
    val items = listOf(
      checkIn(LocalDate.of(2025, 12, 29)),
      checkIn(LocalDate.of(2025, 12, 22)),
      checkIn(LocalDate.of(2025, 12, 15))
    )

    assertEquals(3, calculator.streak(habit(HabitFrequency.WEEKLY), items, newYearToday))
  }

  @Test
  fun `weekly streak treats Sunday as the last day of the week`() {
    val sundayCheckIn = checkIn(LocalDate.of(2026, 10, 11))

    val streak = calculator.streak(habit(HabitFrequency.WEEKLY), listOf(sundayCheckIn), today)

    assertEquals(1, streak)
  }

  @Test
  fun `a target of zero is treated as one`() {
    val items = listOf(checkIn(today))

    assertEquals(1, calculator.streak(habit(HabitFrequency.DAILY, target = 0), items, today))
  }

  @Test
  fun `period count is today for daily and the ISO week total for weekly`() {
    val items = listOf(
      checkIn(today, 2),
      checkIn(LocalDate.of(2026, 10, 12), 3),
      checkIn(LocalDate.of(2026, 10, 11), 5)
    )

    assertEquals(2, calculator.periodCount(habit(HabitFrequency.DAILY), items, today))
    assertEquals(5, calculator.periodCount(habit(HabitFrequency.WEEKLY), items, today))
  }
}
