package com.github.naz013.repository.entity

import com.github.naz013.domain.pomodoro.PomodoroSessionRecord

internal fun PomodoroSessionRecord.toEntity(): PomodoroSessionEntity = PomodoroSessionEntity(
  id = id,
  linkedReminderId = linkedReminderId,
  startedAt = startedAt.toEpochMillisUtc(),
  plannedFocusSeconds = plannedFocusSeconds,
  actualFocusSeconds = actualFocusSeconds,
  wasCompleted = wasCompleted
)

internal fun PomodoroSessionEntity.toDomain(): PomodoroSessionRecord = PomodoroSessionRecord(
  id = id,
  linkedReminderId = linkedReminderId,
  startedAt = startedAt.toLocalDateTimeUtc(),
  plannedFocusSeconds = plannedFocusSeconds,
  actualFocusSeconds = actualFocusSeconds,
  wasCompleted = wasCompleted
)
