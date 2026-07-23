package com.github.naz013.repository.entity

import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.google.gson.Gson

/**
 * Shared trigger/action (type, payload) encoding, reused by both [WorkflowRuleMapper] and
 * [WorkflowTemplateMapper] — a rule and a template store the exact same trigger/action shapes.
 */
internal val workflowGson = Gson()

internal fun WorkflowTrigger.toColumns(): Pair<String, String> = when (this) {
  is WorkflowTrigger.ReminderCompleted -> "REMINDER_COMPLETED" to ""
  is WorkflowTrigger.ReminderSnoozedNTimes -> "REMINDER_SNOOZED_N_TIMES" to workflowGson.toJson(this)
  is WorkflowTrigger.GroupAllCompleted -> "GROUP_ALL_COMPLETED" to ""
  is WorkflowTrigger.LocationEntered -> "LOCATION_ENTERED" to ""
  is WorkflowTrigger.LocationExited -> "LOCATION_EXITED" to ""
  is WorkflowTrigger.ReminderAgeExceeded -> "REMINDER_AGE_EXCEEDED" to workflowGson.toJson(this)
  is WorkflowTrigger.ReminderUnacknowledgedFor -> "REMINDER_UNACKNOWLEDGED_FOR" to workflowGson.toJson(this)
}

internal fun toWorkflowTrigger(type: String, payload: String): WorkflowTrigger = when (type) {
  "REMINDER_COMPLETED" -> WorkflowTrigger.ReminderCompleted
  "REMINDER_SNOOZED_N_TIMES" -> workflowGson.fromJson(payload, WorkflowTrigger.ReminderSnoozedNTimes::class.java)
  "GROUP_ALL_COMPLETED" -> WorkflowTrigger.GroupAllCompleted
  "LOCATION_ENTERED" -> WorkflowTrigger.LocationEntered
  "LOCATION_EXITED" -> WorkflowTrigger.LocationExited
  "REMINDER_AGE_EXCEEDED" -> workflowGson.fromJson(payload, WorkflowTrigger.ReminderAgeExceeded::class.java)
  "REMINDER_UNACKNOWLEDGED_FOR" -> workflowGson.fromJson(payload, WorkflowTrigger.ReminderUnacknowledgedFor::class.java)
  else -> WorkflowTrigger.ReminderCompleted
}

internal fun WorkflowAction.toColumns(): Pair<String, String> = when (this) {
  is WorkflowAction.ArchiveReminder -> "ARCHIVE_REMINDER" to ""
  is WorkflowAction.CompleteReminder -> "COMPLETE_REMINDER" to ""
  is WorkflowAction.ApplyNotificationOverride -> "APPLY_NOTIFICATION_OVERRIDE" to workflowGson.toJson(this)
  is WorkflowAction.ActivateReminder -> "ACTIVATE_REMINDER" to workflowGson.toJson(this)
  is WorkflowAction.RunBackgroundTask -> "RUN_BACKGROUND_TASK" to workflowGson.toJson(this)
}

internal fun toWorkflowAction(type: String, payload: String): WorkflowAction = when (type) {
  "ARCHIVE_REMINDER" -> WorkflowAction.ArchiveReminder
  "COMPLETE_REMINDER" -> WorkflowAction.CompleteReminder
  "APPLY_NOTIFICATION_OVERRIDE" -> workflowGson.fromJson(payload, WorkflowAction.ApplyNotificationOverride::class.java)
  "ACTIVATE_REMINDER" -> workflowGson.fromJson(payload, WorkflowAction.ActivateReminder::class.java)
  "RUN_BACKGROUND_TASK" -> workflowGson.fromJson(payload, WorkflowAction.RunBackgroundTask::class.java)
  else -> WorkflowAction.ArchiveReminder
}
