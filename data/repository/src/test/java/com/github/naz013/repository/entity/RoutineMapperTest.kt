package com.github.naz013.repository.entity

import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.SyncMetadata
import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.routine.RoutineExecutionRecord
import com.github.naz013.domain.routine.RoutineStep
import com.github.naz013.domain.sync.SyncState
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class RoutineMapperTest {

  @Test
  fun `toEntity then toDomain round trips an on-demand routine with no recurrence`() {
    val routine = Routine(
      id = "id-1",
      title = "Post-workout stretch",
      steps = listOf(
        RoutineStep(id = "step-1", title = "Neck rolls", durationSeconds = 30, order = 0),
        RoutineStep(id = "step-2", title = "Hamstring stretch", durationSeconds = 60, order = 1)
      ),
      recurrence = null,
      createdAt = LocalDateTime.of(2026, 7, 22, 9, 0),
      updatedAt = LocalDateTime.of(2026, 7, 22, 9, 0)
    )

    val roundTripped = routine.toEntity().toDomain()

    assertEquals(routine, roundTripped)
    assertEquals(null, roundTripped.recurrence)
  }

  @Test
  fun `toEntity then toDomain round trips a daily recurring routine with timed steps`() {
    val routine = Routine(
      id = "id-2",
      title = "Morning routine",
      color = 5,
      isPinned = true,
      steps = listOf(
        RoutineStep(id = "step-1", title = "Meditate", scheduledTime = "07:00", durationSeconds = 300),
        RoutineStep(id = "step-2", title = "Journal", scheduledTime = "07:15", durationSeconds = 600)
      ),
      recurrence = RecurrenceRule.Daily(repeatInterval = 1L),
      reminderId = "reminder-1",
      lastResetAt = LocalDateTime.of(2026, 7, 21, 0, 0),
      createdAt = LocalDateTime.of(2026, 7, 20, 8, 0),
      updatedAt = LocalDateTime.of(2026, 7, 21, 8, 0),
      sync = SyncMetadata(version = 2L, syncState = SyncState.Synced)
    )

    val roundTripped = routine.toEntity().toDomain()

    assertEquals(routine, roundTripped)
  }

  @Test
  fun `toEntity then toDomain round trips a weekly recurrence with weekday selection`() {
    val routine = Routine(
      id = "id-3",
      title = "Weekday standup prep",
      recurrence = RecurrenceRule.Weekly(weekdays = listOf(1, 2, 3, 4, 5)),
      createdAt = LocalDateTime.of(2026, 7, 20, 8, 0),
      updatedAt = LocalDateTime.of(2026, 7, 20, 8, 0)
    )

    val roundTripped = routine.toEntity().toDomain()

    assertEquals(routine, roundTripped)
    assertEquals(RecurrenceRule.Weekly(weekdays = listOf(1, 2, 3, 4, 5)), roundTripped.recurrence)
  }

  @Test
  fun `toDomain falls back to null recurrence when the payload is malformed JSON`() {
    val entity = legacyEntity(recurrenceType = "DAILY", recurrencePayload = "not-json")

    val domain = entity.toDomain()

    assertEquals(null, domain.recurrence)
  }

  @Test
  fun `toEntity then toDomain round trips a routine execution record`() {
    val record = RoutineExecutionRecord(
      id = "exec-1",
      routineId = "id-2",
      executedAt = LocalDateTime.of(2026, 7, 21, 7, 45),
      totalTimeSpentSeconds = 900,
      completedStepIds = listOf("step-1"),
      totalStepsCount = 2
    )

    val roundTripped = record.toEntity().toDomain()

    assertEquals(record, roundTripped)
    assertEquals(1, roundTripped.completedStepsCount)
  }

  private fun legacyEntity(recurrenceType: String, recurrencePayload: String) = RoutineEntity(
    id = "id-legacy",
    recurrenceType = recurrenceType,
    recurrencePayload = recurrencePayload,
    createdAt = 0L,
    updatedAt = 0L,
    syncState = SyncState.Synced.name
  )
}
