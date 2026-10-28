package com.github.naz013.repository.entity

import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitCheckIn
import com.github.naz013.domain.habit.HabitFrequency
import com.github.naz013.domain.reminder.v2.SyncMetadata
import com.github.naz013.domain.sync.SyncState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class HabitMapperTest {

  @Test
  fun `round trips a weekly pinned habit with sync metadata`() {
    val habit = Habit(
      id = "h-1",
      name = "Run",
      description = "Morning run",
      frequency = HabitFrequency.WEEKLY,
      targetCount = 3,
      color = 4,
      icon = 2,
      isPinned = true,
      createdAt = LocalDateTime.of(2026, 10, 1, 8, 0),
      updatedAt = LocalDateTime.of(2026, 10, 2, 9, 30),
      sync = SyncMetadata(version = 5L, syncState = SyncState.Synced)
    )

    assertEquals(habit, habit.toEntity().toDomain())
  }

  @Test
  fun `round trips a reminder time stored as seconds of day`() {
    val habit = Habit(
      reminderTime = LocalTime.of(7, 30),
      remindOnlyIfNotDone = false,
      createdAt = LocalDateTime.of(2026, 10, 1, 8, 0),
      updatedAt = LocalDateTime.of(2026, 10, 1, 8, 0)
    )

    val entity = habit.toEntity()

    assertEquals(27000, entity.reminderTime)
    assertEquals(habit, entity.toDomain())
  }

  @Test
  fun `maps a habit without reminder to null time and only-if-not-done true`() {
    val entity = Habit(
      createdAt = LocalDateTime.of(2026, 10, 1, 8, 0),
      updatedAt = LocalDateTime.of(2026, 10, 1, 8, 0)
    ).toEntity()

    assertNull(entity.reminderTime)
    assertTrue(entity.remindOnlyIfNotDone)
    assertNull(entity.toDomain().reminderTime)
  }

  @Test
  fun `falls back to daily when stored frequency is unknown`() {
    val entity = Habit(
      createdAt = LocalDateTime.of(2026, 10, 1, 8, 0),
      updatedAt = LocalDateTime.of(2026, 10, 1, 8, 0)
    ).toEntity().copy(frequency = "MONTHLY")

    assertEquals(HabitFrequency.DAILY, entity.toDomain().frequency)
  }

  @Test
  fun `round trips a check-in and stores the date as ISO text`() {
    val checkIn = HabitCheckIn(
      habitId = "h-1",
      date = LocalDate.of(2026, 10, 18),
      count = 2,
      isCompleted = true,
      createdAt = LocalDateTime.of(2026, 10, 18, 7, 0),
      updatedAt = LocalDateTime.of(2026, 10, 18, 7, 5)
    )

    val entity = checkIn.toEntity()

    assertEquals("2026-10-18", entity.date)
    assertEquals(checkIn, entity.toDomain())
  }
}
