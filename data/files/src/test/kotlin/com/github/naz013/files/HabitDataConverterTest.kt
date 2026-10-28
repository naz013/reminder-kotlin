package com.github.naz013.files

import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitCheckIn
import com.github.naz013.domain.habit.HabitFrequency
import com.github.naz013.domain.reminder.v2.SyncMetadata
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.files.model.HabitJson
import com.google.gson.Gson
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

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
  fun `habits data type is pro only with the hb1 extension`() {
    assertEquals(".hb1", DataType.Habits.fileExtension)
    assertTrue(DataType.Habits.isProOnly)
  }

  @Test
  fun `habit json defaults to a daily frequency with one target`() {
    val json = HabitJson(id = "x", createdAt = "", updatedAt = "")

    assertEquals("DAILY", json.frequency)
    assertEquals(1, json.targetCount)
  }

  @Test
  fun `detects raw habit json as a Habit`() {
    val raw = """{"id":"h-1","name":"Run","frequency":"WEEKLY","targetCount":3,"color":0,""" +
      """"isPinned":false,"createdAt":"2026-10-01T08:00:00","updatedAt":"2026-10-01T08:00:00","versionId":2}"""

    val result = parseRaw(raw)

    assertTrue(result is Habit)
    assertEquals(HabitFrequency.WEEKLY, (result as Habit).frequency)
  }

  @Test
  fun `round trips a habit reminder time and only-if-not-done flag`() {
    val habit = Habit(
      reminderTime = LocalTime.of(7, 30),
      remindOnlyIfNotDone = false,
      createdAt = LocalDateTime.of(2026, 10, 1, 8, 0),
      updatedAt = LocalDateTime.of(2026, 10, 1, 8, 0)
    )

    val result = habit.toJson().toDomain()

    assertEquals(LocalTime.of(7, 30), result.reminderTime)
    assertEquals(false, result.remindOnlyIfNotDone)
  }

  @Test
  fun `defaults to no reminder and only-if-not-done for old habit json without the fields`() {
    val raw = """{"id":"h-1","name":"Run","frequency":"DAILY","targetCount":1,"color":0,""" +
      """"isPinned":false,"createdAt":"2026-10-01T08:00:00","updatedAt":"2026-10-01T08:00:00","versionId":2}"""

    val result = parseRaw(raw) as Habit

    assertNull(result.reminderTime)
    assertTrue(result.remindOnlyIfNotDone)
  }

  @Test
  fun `ignores an unparsable reminder time in a habit file`() {
    val json = Habit(
      reminderTime = LocalTime.of(7, 30),
      createdAt = LocalDateTime.of(2026, 10, 1, 8, 0),
      updatedAt = LocalDateTime.of(2026, 10, 1, 8, 0)
    ).toJson().copy(reminderTime = "bad")

    assertNull(json.toDomain().reminderTime)
  }

  @Test
  fun `detects raw check-in json as a HabitCheckIn`() {
    val raw = """{"habitId":"h-1","date":"2026-10-18","count":1,"isCompleted":true,""" +
      """"createdAt":"2026-10-18T07:00:00","updatedAt":"2026-10-18T07:00:00"}"""

    val result = parseRaw(raw)

    assertTrue(result is HabitCheckIn)
    assertEquals(LocalDate.of(2026, 10, 18), (result as HabitCheckIn).date)
  }

  private fun parseRaw(raw: String): Any {
    val json = JsonParser.parseString(raw).asJsonObject
    val clazz = detectClass(json)
    return Gson().fromJson(json, clazz).toDomain()
  }
}
