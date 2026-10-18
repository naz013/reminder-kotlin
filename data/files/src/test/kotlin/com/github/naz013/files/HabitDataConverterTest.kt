package com.github.naz013.files

import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitCheckIn
import com.github.naz013.domain.habit.HabitFrequency
import com.github.naz013.domain.reminder.v2.SyncMetadata
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.files.model.HabitJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class HabitDataConverterTest {

  @Test
  fun `round trips a weekly habit and marks it synced`() {
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
      sync = SyncMetadata(version = 7L, syncState = SyncState.WaitingForUpload)
    )

    val result = habit.toJson().toDomain()

    assertEquals(habit.copy(sync = SyncMetadata(version = 7L, syncState = SyncState.Synced)), result)
  }

  @Test
  fun `falls back to daily for an unknown frequency in a habit file`() {
    val json = Habit(
      createdAt = LocalDateTime.of(2026, 10, 1, 8, 0),
      updatedAt = LocalDateTime.of(2026, 10, 1, 8, 0)
    ).toJson().copy(frequency = "MONTHLY")

    assertEquals(HabitFrequency.DAILY, json.toDomain().frequency)
  }

  @Test
  fun `round trips a habit check-in`() {
    val checkIn = HabitCheckIn(
      habitId = "h-1",
      date = LocalDate.of(2026, 10, 18),
      count = 2,
      isCompleted = true,
      createdAt = LocalDateTime.of(2026, 10, 18, 7, 0),
      updatedAt = LocalDateTime.of(2026, 10, 18, 7, 5)
    )

    val result = checkIn.toJson().toDomain()

    assertEquals(checkIn, result)
  }

  @Test
  fun `habits data type is pro only`() {
    assertEquals(".hb1", DataType.Habits.fileExtension)
    assertTrue(DataType.Habits.isProOnly)
    assertTrue(HabitJson(id = "x", createdAt = "", updatedAt = "").frequency == "DAILY")
  }
}
