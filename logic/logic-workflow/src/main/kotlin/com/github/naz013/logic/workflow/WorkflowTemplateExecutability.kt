package com.github.naz013.logic.workflow

import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTrigger

/**
 * Whether [WorkflowEngine] actually evaluates [WorkflowTemplate.trigger] and applies
 * [WorkflowTemplate.action] for this template. A template seeded ahead of its trigger/action being
 * wired into the engine would otherwise create rules that silently never fire - this happened once
 * already (the "Escalate after repeated snoozes" built-in template, seeded before
 * [WorkflowTrigger.ReminderSnoozedNTimes]/[WorkflowAction.ApplyNotificationOverride] were wired).
 * Every variant is exhaustively listed here on purpose: adding a new [WorkflowTrigger] or
 * [WorkflowAction] subtype forces a compile error in this file until its executability is decided,
 * rather than letting a template silently reference an unimplemented one.
 */
fun WorkflowTemplate.isExecutable(): Boolean = isTriggerImplemented(trigger) && isActionImplemented(action)

private fun isTriggerImplemented(trigger: WorkflowTrigger): Boolean = when (trigger) {
  is WorkflowTrigger.ReminderCreated,
  is WorkflowTrigger.ReminderCompleted,
  is WorkflowTrigger.ReminderSnoozedNTimes,
  is WorkflowTrigger.GroupAllCompleted,
  is WorkflowTrigger.LocationEntered,
  is WorkflowTrigger.LocationExited,
  is WorkflowTrigger.ReminderAgeExceeded,
  is WorkflowTrigger.ReminderUnacknowledgedFor,
  is WorkflowTrigger.ScheduleReached -> true
}

private fun isActionImplemented(action: WorkflowAction): Boolean = when (action) {
  is WorkflowAction.ArchiveReminder,
  is WorkflowAction.CompleteReminder,
  is WorkflowAction.PurgeReminder,
  is WorkflowAction.ApplyNotificationOverride,
  is WorkflowAction.ClearNotificationOverride,
  is WorkflowAction.ActivateReminder,
  is WorkflowAction.MoveToGroup,
  is WorkflowAction.SendBroadcastIntent,
  is WorkflowAction.RunBackgroundTask,
  is WorkflowAction.ApplyTag,
  is WorkflowAction.RemoveTag -> true
}
