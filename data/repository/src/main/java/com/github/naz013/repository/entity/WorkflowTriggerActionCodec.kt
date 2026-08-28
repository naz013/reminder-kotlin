package com.github.naz013.repository.entity

import com.github.naz013.domain.workflow.ScheduleRecurrence
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowCondition
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.logging.Logger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

/**
 * Shared trigger/action (type, payload) encoding, reused by both [WorkflowRuleMapper] and
 * [WorkflowTemplateMapper] — a rule and a template store the exact same trigger/action shapes.
 */
internal val workflowGson = Gson()

private const val TAG = "WorkflowTriggerActionCodec"

/** [WorkflowTrigger.ScheduleReached.atDateTime] is deliberately NOT passed through
 * `workflowGson.toJson(this)` like every other payload field - plain `Gson()` has no registered
 * adapter for ThreeTenBP's `LocalDateTime`, so reflecting into its internal fields would be
 * unannotated and unsafe under R8 (see the class doc on [WorkflowTrigger]). Formatting it to a
 * plain ISO string first keeps the wire payload to types Gson already handles safely. */
private val scheduleDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
private data class ScheduleReachedColumns(val atDateTime: String, val recurrence: String)

internal fun WorkflowTrigger.toColumns(): Pair<String, String> = when (this) {
  is WorkflowTrigger.ReminderCreated -> "REMINDER_CREATED" to ""
  is WorkflowTrigger.ReminderCompleted -> "REMINDER_COMPLETED" to ""
  is WorkflowTrigger.ReminderSnoozedNTimes -> "REMINDER_SNOOZED_N_TIMES" to workflowGson.toJson(this)
  is WorkflowTrigger.GroupAllCompleted -> "GROUP_ALL_COMPLETED" to ""
  is WorkflowTrigger.LocationEntered -> "LOCATION_ENTERED" to ""
  is WorkflowTrigger.LocationExited -> "LOCATION_EXITED" to ""
  is WorkflowTrigger.ReminderAgeExceeded -> "REMINDER_AGE_EXCEEDED" to workflowGson.toJson(this)
  is WorkflowTrigger.ReminderUnacknowledgedFor -> "REMINDER_UNACKNOWLEDGED_FOR" to workflowGson.toJson(this)
  is WorkflowTrigger.ScheduleReached -> "SCHEDULE_REACHED" to workflowGson.toJson(
    ScheduleReachedColumns(atDateTime.format(scheduleDateTimeFormatter), recurrence.name)
  )
}

/** Falls back to [WorkflowTrigger.ReminderCompleted] (and logs) instead of throwing on a payload
 * it can't parse — one unreadable row must not take down the whole workflow-rule list. */
internal fun toWorkflowTrigger(type: String, payload: String): WorkflowTrigger = runCatching {
  when (type) {
    "REMINDER_CREATED" -> WorkflowTrigger.ReminderCreated
    "REMINDER_COMPLETED" -> WorkflowTrigger.ReminderCompleted
    "REMINDER_SNOOZED_N_TIMES" -> workflowGson.fromJson(payload, WorkflowTrigger.ReminderSnoozedNTimes::class.java)
    "GROUP_ALL_COMPLETED" -> WorkflowTrigger.GroupAllCompleted
    "LOCATION_ENTERED" -> WorkflowTrigger.LocationEntered
    "LOCATION_EXITED" -> WorkflowTrigger.LocationExited
    "REMINDER_AGE_EXCEEDED" -> workflowGson.fromJson(payload, WorkflowTrigger.ReminderAgeExceeded::class.java)
    "REMINDER_UNACKNOWLEDGED_FOR" -> workflowGson.fromJson(payload, WorkflowTrigger.ReminderUnacknowledgedFor::class.java)
    "SCHEDULE_REACHED" -> workflowGson.fromJson(payload, ScheduleReachedColumns::class.java).let {
      WorkflowTrigger.ScheduleReached(
        atDateTime = LocalDateTime.parse(it.atDateTime, scheduleDateTimeFormatter),
        recurrence = runCatching { ScheduleRecurrence.valueOf(it.recurrence) }.getOrDefault(ScheduleRecurrence.ONCE)
      )
    }
    else -> WorkflowTrigger.ReminderCompleted
  }
}.getOrElse { e ->
  Logger.e(TAG, "Failed to parse workflow trigger, type=$type, payload=$payload", e)
  WorkflowTrigger.ReminderCompleted
}

internal fun WorkflowAction.toColumns(): Pair<String, String> = when (this) {
  is WorkflowAction.ArchiveReminder -> "ARCHIVE_REMINDER" to ""
  is WorkflowAction.CompleteReminder -> "COMPLETE_REMINDER" to ""
  is WorkflowAction.PurgeReminder -> "PURGE_REMINDER" to ""
  is WorkflowAction.ApplyNotificationOverride -> "APPLY_NOTIFICATION_OVERRIDE" to workflowGson.toJson(this)
  is WorkflowAction.ClearNotificationOverride -> "CLEAR_NOTIFICATION_OVERRIDE" to ""
  is WorkflowAction.ActivateReminder -> "ACTIVATE_REMINDER" to workflowGson.toJson(this)
  is WorkflowAction.MoveToGroup -> "MOVE_TO_GROUP" to workflowGson.toJson(this)
  is WorkflowAction.SendBroadcastIntent -> "SEND_BROADCAST_INTENT" to workflowGson.toJson(this)
  is WorkflowAction.RunBackgroundTask -> "RUN_BACKGROUND_TASK" to workflowGson.toJson(this)
}

/** Falls back to [WorkflowAction.ArchiveReminder] (and logs) instead of throwing on a payload it
 * can't parse — one unreadable row must not take down the whole workflow-rule list. */
internal fun toWorkflowAction(type: String, payload: String): WorkflowAction = runCatching {
  when (type) {
    "ARCHIVE_REMINDER" -> WorkflowAction.ArchiveReminder
    "COMPLETE_REMINDER" -> WorkflowAction.CompleteReminder
    "PURGE_REMINDER" -> WorkflowAction.PurgeReminder
    "APPLY_NOTIFICATION_OVERRIDE" -> workflowGson.fromJson(payload, WorkflowAction.ApplyNotificationOverride::class.java)
    "CLEAR_NOTIFICATION_OVERRIDE" -> WorkflowAction.ClearNotificationOverride
    "ACTIVATE_REMINDER" -> workflowGson.fromJson(payload, WorkflowAction.ActivateReminder::class.java)
    "MOVE_TO_GROUP" -> workflowGson.fromJson(payload, WorkflowAction.MoveToGroup::class.java)
    "SEND_BROADCAST_INTENT" -> workflowGson.fromJson(payload, WorkflowAction.SendBroadcastIntent::class.java)
    "RUN_BACKGROUND_TASK" -> workflowGson.fromJson(payload, WorkflowAction.RunBackgroundTask::class.java)
    else -> WorkflowAction.ArchiveReminder
  }
}.getOrElse { e ->
  Logger.e(TAG, "Failed to parse workflow action, type=$type, payload=$payload", e)
  WorkflowAction.ArchiveReminder
}

private data class ConditionColumns(val type: String, val payload: String)

private fun WorkflowCondition.toColumns(): ConditionColumns = when (this) {
  is WorkflowCondition.PriorityAtLeast -> ConditionColumns("PRIORITY_AT_LEAST", workflowGson.toJson(this))
  is WorkflowCondition.WithinTimeWindow -> ConditionColumns("WITHIN_TIME_WINDOW", workflowGson.toJson(this))
  is WorkflowCondition.GroupIs -> ConditionColumns("GROUP_IS", workflowGson.toJson(this))
  is WorkflowCondition.TitleContains -> ConditionColumns("TITLE_CONTAINS", workflowGson.toJson(this))
}

/** Drops (and logs) a condition it can't parse, rather than throwing - one bad condition must not
 * take down the whole rule/conditions list; [toWorkflowConditions] already filters out nulls. */
private fun toWorkflowCondition(columns: ConditionColumns): WorkflowCondition? = runCatching {
  when (columns.type) {
    "PRIORITY_AT_LEAST" -> workflowGson.fromJson(columns.payload, WorkflowCondition.PriorityAtLeast::class.java)
    "WITHIN_TIME_WINDOW" -> workflowGson.fromJson(columns.payload, WorkflowCondition.WithinTimeWindow::class.java)
    "GROUP_IS" -> workflowGson.fromJson(columns.payload, WorkflowCondition.GroupIs::class.java)
    "TITLE_CONTAINS" -> workflowGson.fromJson(columns.payload, WorkflowCondition.TitleContains::class.java)
    else -> null
  }
}.getOrElse { e ->
  Logger.e(TAG, "Failed to parse workflow condition, columns=$columns", e)
  null
}

private val conditionColumnsListType = object : TypeToken<List<ConditionColumns>>() {}.type

internal fun List<WorkflowCondition>.toConditionsPayload(): String =
  workflowGson.toJson(map { it.toColumns() })

internal fun String.toWorkflowConditions(): List<WorkflowCondition> =
  runCatching { workflowGson.fromJson<List<ConditionColumns>>(this, conditionColumnsListType) }
    .getOrNull()
    .orEmpty()
    .mapNotNull { toWorkflowCondition(it) }
