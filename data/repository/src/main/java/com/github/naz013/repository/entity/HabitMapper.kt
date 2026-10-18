package com.github.naz013.repository.entity

import com.github.naz013.domain.habit.Habit
import com.github.naz013.domain.habit.HabitCheckIn
import com.github.naz013.domain.habit.HabitFrequency
import com.github.naz013.domain.reminder.v2.SyncMetadata
import com.github.naz013.domain.sync.SyncState
import org.threeten.bp.LocalDate

internal fun Habit.toEntity(): HabitEntity = HabitEntity(
  id = id,
  name = name,
  description = description,
  frequency = frequency.name,
  targetCount = targetCount,
  color = color,
  icon = icon,
  isPinned = isPinned,
  createdAt = createdAt.toEpochMillisUtc(),
  updatedAt = updatedAt.toEpochMillisUtc(),
  version = sync.version,
  syncState = sync.syncState.name
)

internal fun HabitEntity.toDomain(): Habit = Habit(
  id = id,
  name = name,
  description = description,
  frequency = runCatching { HabitFrequency.valueOf(frequency) }.getOrDefault(HabitFrequency.DAILY),
  targetCount = targetCount,
  color = color,
  icon = icon,
  isPinned = isPinned,
  createdAt = createdAt.toLocalDateTimeUtc(),
  updatedAt = updatedAt.toLocalDateTimeUtc(),
  sync = SyncMetadata(version = version, syncState = SyncState.valueOf(syncState))
)

internal fun HabitCheckIn.toEntity(): HabitCheckInEntity = HabitCheckInEntity(
  habitId = habitId,
  date = date.toString(),
  count = count,
  isCompleted = isCompleted,
  createdAt = createdAt.toEpochMillisUtc(),
  updatedAt = updatedAt.toEpochMillisUtc()
)

internal fun HabitCheckInEntity.toDomain(): HabitCheckIn = HabitCheckIn(
  habitId = habitId,
  date = LocalDate.parse(date),
  count = count,
  isCompleted = isCompleted,
  createdAt = createdAt.toLocalDateTimeUtc(),
  updatedAt = updatedAt.toLocalDateTimeUtc()
)
