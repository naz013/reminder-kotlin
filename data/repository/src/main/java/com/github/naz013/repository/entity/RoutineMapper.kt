package com.github.naz013.repository.entity

import com.github.naz013.domain.reminder.v2.RecurrenceRule
import com.github.naz013.domain.reminder.v2.SyncMetadata
import com.github.naz013.domain.routine.Routine
import com.github.naz013.domain.routine.RoutineExecutionRecord
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.google.gson.Gson

private const val TAG = "RoutineMapper"

private val gson = Gson()

internal fun Routine.toEntity(): RoutineEntity {
  val (recurrenceType, recurrencePayload) = recurrence.toColumns()
  return RoutineEntity(
    id = id,
    title = title,
    description = description,
    color = color,
    isPinned = isPinned,
    icon = icon,
    steps = steps,
    autoAdvance = autoAdvance,
    soundAlertsEnabled = soundAlertsEnabled,
    recurrenceType = recurrenceType,
    recurrencePayload = recurrencePayload,
    lastResetAt = lastResetAt?.toEpochMillisUtc(),
    createdAt = createdAt.toEpochMillisUtc(),
    updatedAt = updatedAt.toEpochMillisUtc(),
    version = sync.version,
    syncState = sync.syncState.name
  )
}

internal fun RoutineEntity.toDomain(): Routine = Routine(
  id = id,
  title = title,
  description = description,
  color = color,
  isPinned = isPinned,
  icon = icon,
  steps = steps,
  autoAdvance = autoAdvance,
  soundAlertsEnabled = soundAlertsEnabled,
  recurrence = toRecurrenceRule(recurrenceType, recurrencePayload),
  lastResetAt = lastResetAt?.toLocalDateTimeUtc(),
  createdAt = createdAt.toLocalDateTimeUtc(),
  updatedAt = updatedAt.toLocalDateTimeUtc(),
  sync = SyncMetadata(version = version, syncState = SyncState.valueOf(syncState))
)

internal fun RoutineExecutionRecord.toEntity(): RoutineExecutionEntity = RoutineExecutionEntity(
  id = id,
  routineId = routineId,
  executedAt = executedAt.toEpochMillisUtc(),
  totalTimeSpentSeconds = totalTimeSpentSeconds,
  completedStepIds = completedStepIds,
  totalStepsCount = totalStepsCount
)

internal fun RoutineExecutionEntity.toDomain(): RoutineExecutionRecord = RoutineExecutionRecord(
  id = id,
  routineId = routineId,
  executedAt = executedAt.toLocalDateTimeUtc(),
  totalTimeSpentSeconds = totalTimeSpentSeconds,
  completedStepIds = completedStepIds,
  totalStepsCount = totalStepsCount
)

/** Mirrors [ReminderV2Mapper]'s type+payload split for the same [RecurrenceRule] sealed class -
 * a bare Room `@TypeConverters` Gson converter risks the R8 field-stripping crash documented on
 * [RecurrenceRule] itself. `null` (on-demand routine, no recurrence) is stored as "NONE"/"". */
private fun RecurrenceRule?.toColumns(): Pair<String, String> = when (this) {
  null -> "NONE" to ""
  is RecurrenceRule.Once -> "ONCE" to ""
  is RecurrenceRule.Countdown -> "COUNTDOWN" to gson.toJson(this)
  is RecurrenceRule.Daily -> "DAILY" to gson.toJson(this)
  is RecurrenceRule.Weekly -> "WEEKLY" to gson.toJson(this)
  is RecurrenceRule.Monthly -> "MONTHLY" to gson.toJson(this)
  is RecurrenceRule.RelativeMonthly -> "RELATIVE_MONTHLY" to gson.toJson(this)
  is RecurrenceRule.Yearly -> "YEARLY" to gson.toJson(this)
  is RecurrenceRule.LocationEnter -> "LOCATION_ENTER" to ""
  is RecurrenceRule.LocationExit -> "LOCATION_EXIT" to ""
  is RecurrenceRule.ICalendar -> "ICALENDAR" to gson.toJson(this)
}

/** Falls back to `null` (on-demand) instead of throwing on a payload it can't parse - one
 * unreadable row must not take down the whole routine list. */
private fun toRecurrenceRule(type: String, payload: String): RecurrenceRule? = runCatching {
  when (type) {
    "NONE" -> null
    "ONCE" -> RecurrenceRule.Once
    "COUNTDOWN" -> gson.fromJson(payload, RecurrenceRule.Countdown::class.java)
    "DAILY" -> gson.fromJson(payload, RecurrenceRule.Daily::class.java)
    "WEEKLY" -> gson.fromJson(payload, RecurrenceRule.Weekly::class.java).also {
      requireNotNull(it.weekdays) { "weekdays is null" }
    }
    "MONTHLY" -> gson.fromJson(payload, RecurrenceRule.Monthly::class.java)
    "RELATIVE_MONTHLY" -> gson.fromJson(payload, RecurrenceRule.RelativeMonthly::class.java)
    "YEARLY" -> gson.fromJson(payload, RecurrenceRule.Yearly::class.java)
    "LOCATION_ENTER" -> RecurrenceRule.LocationEnter
    "LOCATION_EXIT" -> RecurrenceRule.LocationExit
    "ICALENDAR" -> gson.fromJson(payload, RecurrenceRule.ICalendar::class.java).also {
      requireNotNull(it.rrule) { "rrule is null" }
    }
    else -> null
  }
}.getOrElse { e ->
  Logger.e(TAG, "Failed to parse recurrence rule, type=$type, payload=$payload", e)
  null
}
