package com.github.naz013.repository.entity

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.google.gson.Gson

private val gson = Gson()

internal fun WorkflowRule.toEntity(): WorkflowRuleEntity {
  val (scopeType, scopeId) = scope.toColumns()
  val (triggerType, triggerPayload) = trigger.toColumns()
  val (actionType, actionPayload) = action.toColumns()
  return WorkflowRuleEntity(
    uuId = uuId,
    title = title,
    scopeType = scopeType,
    scopeId = scopeId,
    triggerType = triggerType,
    triggerPayload = triggerPayload,
    actionType = actionType,
    actionPayload = actionPayload,
    isEnabled = isEnabled,
    createdAt = createdAt.toEpochMillisUtc(),
    lastRunAt = lastRunAt?.toEpochMillisUtc(),
    version = version,
    syncState = syncState.name
  )
}

internal fun WorkflowRuleEntity.toDomain(): WorkflowRule = WorkflowRule(
  uuId = uuId,
  title = title,
  scope = toWorkflowScope(scopeType, scopeId),
  trigger = toWorkflowTrigger(triggerType, triggerPayload),
  action = toWorkflowAction(actionType, actionPayload),
  isEnabled = isEnabled,
  createdAt = createdAt.toLocalDateTimeUtc(),
  lastRunAt = lastRunAt?.toLocalDateTimeUtc(),
  version = version,
  syncState = SyncState.valueOf(syncState)
)

private fun WorkflowScope.toColumns(): Pair<String, String?> = when (this) {
  is WorkflowScope.Global -> "GLOBAL" to null
  is WorkflowScope.ForGroup -> "GROUP" to groupId
  is WorkflowScope.ForReminder -> "REMINDER" to reminderId
}

private fun toWorkflowScope(scopeType: String, scopeId: String?): WorkflowScope = when (scopeType) {
  "GROUP" -> scopeId?.let { WorkflowScope.ForGroup(it) } ?: WorkflowScope.Global
  "REMINDER" -> scopeId?.let { WorkflowScope.ForReminder(it) } ?: WorkflowScope.Global
  else -> WorkflowScope.Global
}

private fun WorkflowTrigger.toColumns(): Pair<String, String> = when (this) {
  is WorkflowTrigger.ReminderCompleted -> "REMINDER_COMPLETED" to ""
  is WorkflowTrigger.ReminderSnoozedNTimes -> "REMINDER_SNOOZED_N_TIMES" to gson.toJson(this)
  is WorkflowTrigger.GroupAllCompleted -> "GROUP_ALL_COMPLETED" to ""
  is WorkflowTrigger.LocationEntered -> "LOCATION_ENTERED" to ""
  is WorkflowTrigger.LocationExited -> "LOCATION_EXITED" to ""
  is WorkflowTrigger.ReminderAgeExceeded -> "REMINDER_AGE_EXCEEDED" to gson.toJson(this)
  is WorkflowTrigger.ReminderUnacknowledgedFor -> "REMINDER_UNACKNOWLEDGED_FOR" to gson.toJson(this)
}

private fun toWorkflowTrigger(type: String, payload: String): WorkflowTrigger = when (type) {
  "REMINDER_COMPLETED" -> WorkflowTrigger.ReminderCompleted
  "REMINDER_SNOOZED_N_TIMES" -> gson.fromJson(payload, WorkflowTrigger.ReminderSnoozedNTimes::class.java)
  "GROUP_ALL_COMPLETED" -> WorkflowTrigger.GroupAllCompleted
  "LOCATION_ENTERED" -> WorkflowTrigger.LocationEntered
  "LOCATION_EXITED" -> WorkflowTrigger.LocationExited
  "REMINDER_AGE_EXCEEDED" -> gson.fromJson(payload, WorkflowTrigger.ReminderAgeExceeded::class.java)
  "REMINDER_UNACKNOWLEDGED_FOR" -> gson.fromJson(payload, WorkflowTrigger.ReminderUnacknowledgedFor::class.java)
  else -> WorkflowTrigger.ReminderCompleted
}

private fun WorkflowAction.toColumns(): Pair<String, String> = when (this) {
  is WorkflowAction.ArchiveReminder -> "ARCHIVE_REMINDER" to ""
  is WorkflowAction.CompleteReminder -> "COMPLETE_REMINDER" to ""
  is WorkflowAction.ApplyNotificationOverride -> "APPLY_NOTIFICATION_OVERRIDE" to gson.toJson(this)
  is WorkflowAction.ActivateReminder -> "ACTIVATE_REMINDER" to gson.toJson(this)
  is WorkflowAction.RunBackgroundTask -> "RUN_BACKGROUND_TASK" to gson.toJson(this)
}

private fun toWorkflowAction(type: String, payload: String): WorkflowAction = when (type) {
  "ARCHIVE_REMINDER" -> WorkflowAction.ArchiveReminder
  "COMPLETE_REMINDER" -> WorkflowAction.CompleteReminder
  "APPLY_NOTIFICATION_OVERRIDE" -> gson.fromJson(payload, WorkflowAction.ApplyNotificationOverride::class.java)
  "ACTIVATE_REMINDER" -> gson.fromJson(payload, WorkflowAction.ActivateReminder::class.java)
  "RUN_BACKGROUND_TASK" -> gson.fromJson(payload, WorkflowAction.RunBackgroundTask::class.java)
  else -> WorkflowAction.ArchiveReminder
}
