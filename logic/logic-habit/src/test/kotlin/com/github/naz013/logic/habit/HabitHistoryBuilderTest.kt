package com.github.naz013.logic.habit

import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitCheckIn
import com.github.naz013.domain.habit.HabitFrequency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class HabitHistoryBuilderTest {
  private val builder = HabitHistoryBuilder()
  private val stamp = LocalDateTime.of(2026, 1, 1, 0, 0)

  // Wednesday; its ISO week starts Monday 2026-10-12.
  private val today = LocalDate.of(2026, 10, 14)

  private fun habit(frequency: HabitFrequency, target: Int) = Habit(
    id = "h",
    frequency = frequency,
    targetCount = target,
    createdAt = stamp,
    updatedAt = stamp
  )

  private fun checkIn(date: LocalDate, count: Int) = HabitCheckIn(
    habitId = "h",
    date = date,
    count = count,
    createdAt = stamp,
    updatedAt = stamp
  )

  @Test
  fun `returns nothing for a non-positive number of weeks`() {
    assertTrue(builder.build(habit(HabitFrequency.DAILY, 1), emptyList(), today, 0).isEmpty())
  }

  @Test
  fun `starts on a Monday and ends today without future days`() {
    val days = builder.build(habit(HabitFrequency.DAILY, 1), emptyList(), today, 3)

    assertEquals(DayOfWeek.MONDAY, days.first().date.dayOfWeek)
    assertEquals(LocalDate.of(2026, 9, 28), days.first().date)
    assertEquals(today, days.last().date)
    assertEquals(17, days.size)
  }

  @Test
  fun `daily level scales with the count over the target`() {
    val items = listOf(
      checkIn(today, 4),
      checkIn(today.minusDays(1), 2),
      checkIn(today.minusDays(2), 1),
      checkIn(today.minusDays(3), 9)
    )
    val days = builder.build(habit(HabitFrequency.DAILY, 4), items, today, 2).associateBy { it.date }

    assertEquals(4, days.getValue(today).level)
    assertEquals(2, days.getValue(today.minusDays(1)).level)
    assertEquals(1, days.getValue(today.minusDays(2)).level)
    assertEquals(HabitDay.MAX_LEVEL, days.getValue(today.minusDays(3)).level)
  }

  @Test
  fun `daily level has a floor of one for partial progress and a cap at the maximum`() {
    val items = listOf(checkIn(today, 1), checkIn(today.minusDays(1), 20))
    val days = builder.build(habit(HabitFrequency.DAILY, 10), items, today, 2).associateBy { it.date }

    assertEquals(1, days.getValue(today).level)
    assertEquals(HabitDay.MAX_LEVEL, days.getValue(today.minusDays(1)).level)
  }

  @Test
  fun `weekly level is partial until the week target is met`() {
    val partial = builder.build(habit(HabitFrequency.WEEKLY, 3), listOf(checkIn(today, 1)), today, 1)
    val reached = builder.build(
      habit(HabitFrequency.WEEKLY, 2),
      listOf(checkIn(today, 1), checkIn(today.minusDays(1), 1)),
      today,
      1
    )

    assertEquals(2, partial.single { it.date == today }.level)
    assertEquals(HabitDay.MAX_LEVEL, reached.single { it.date == today }.level)
    assertEquals(0, reached.single { it.date == today.minusDays(2) }.level)
  }
}
