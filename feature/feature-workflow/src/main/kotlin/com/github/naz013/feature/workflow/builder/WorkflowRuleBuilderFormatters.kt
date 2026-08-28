package com.github.naz013.feature.workflow.builder

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowCondition
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.feature.workflow.R

/** Display name for a trigger/condition/action's *type*, used both for its picker-list option
 * and as the row title once configured - independent of any params it carries. */
@Composable
internal fun workflowTriggerLabel(trigger: WorkflowTrigger): String = when (trigger) {
  is WorkflowTrigger.ReminderCompleted -> stringResource(R.string.workflow_trigger_reminder_completed)
  is WorkflowTrigger.ReminderSnoozedNTimes -> stringResource(R.string.workflow_trigger_reminder_snoozed_n_times)
  is WorkflowTrigger.GroupAllCompleted -> stringResource(R.string.workflow_trigger_group_all_completed)
  is WorkflowTrigger.LocationEntered -> stringResource(R.string.workflow_trigger_location_entered)
  is WorkflowTrigger.LocationExited -> stringResource(R.string.workflow_trigger_location_exited)
  is WorkflowTrigger.ReminderAgeExceeded -> stringResource(R.string.workflow_trigger_reminder_age_exceeded)
  is WorkflowTrigger.ReminderUnacknowledgedFor -> stringResource(R.string.workflow_trigger_reminder_unacknowledged_for)
  is WorkflowTrigger.ScheduleReached -> stringResource(R.string.workflow_trigger_schedule_reached)
}

/** The configured value line for a trigger, or null for parameterless ones. */
@Composable
internal fun workflowTriggerValue(trigger: WorkflowTrigger): String? = when (trigger) {
  is WorkflowTrigger.ReminderSnoozedNTimes -> "${trigger.count}"
  is WorkflowTrigger.ReminderAgeExceeded -> "${trigger.days} ${stringResource(R.string.days)}"
  is WorkflowTrigger.ReminderUnacknowledgedFor -> "${trigger.minutes} ${stringResource(
    R.string.workflow_builder_minutes_unit
  )}"
  else -> null
}

@Composable
internal fun workflowConditionLabel(condition: WorkflowCondition): String = when (condition) {
  is WorkflowCondition.PriorityAtLeast -> stringResource(R.string.workflow_condition_priority_at_least)
  is WorkflowCondition.WithinTimeWindow -> stringResource(R.string.workflow_condition_within_time_window)
  is WorkflowCondition.GroupIs -> stringResource(R.string.workflow_condition_group_is)
}

@Composable
internal fun workflowPriorityLabel(priority: ReminderPriority): String = when (priority) {
  ReminderPriority.LOWEST -> stringResource(R.string.priority_lowest)
  ReminderPriority.LOW -> stringResource(R.string.priority_low)
  ReminderPriority.NORMAL -> stringResource(R.string.priority_normal)
  ReminderPriority.HIGH -> stringResource(R.string.priority_high)
  ReminderPriority.HIGHEST -> stringResource(R.string.priority_highest)
}

@Composable
internal fun workflowConditionValue(
  condition: WorkflowCondition,
  groups: List<UiWorkflowGroupOption>
): String = when (condition) {
  is WorkflowCondition.PriorityAtLeast -> workflowPriorityLabel(condition.priority)
  is WorkflowCondition.WithinTimeWindow ->
    "%02d:%02d–%02d:%02d".format(
      condition.fromMinuteOfDay / 60,
      condition.fromMinuteOfDay % 60,
      condition.toMinuteOfDay / 60,
      condition.toMinuteOfDay % 60,
    )
  is WorkflowCondition.GroupIs -> groups.firstOrNull { it.id == condition.groupId }?.title ?: condition.groupId
}

@Composable
internal fun workflowActionLabel(action: WorkflowAction): String = when (action) {
  is WorkflowAction.ArchiveReminder -> stringResource(R.string.workflow_action_archive_reminder)
  is WorkflowAction.CompleteReminder -> stringResource(R.string.workflow_action_complete_reminder)
  is WorkflowAction.PurgeReminder -> stringResource(R.string.workflow_action_purge_reminder)
  is WorkflowAction.ApplyNotificationOverride -> stringResource(R.string.workflow_action_apply_notification_override)
  is WorkflowAction.ActivateReminder -> stringResource(R.string.workflow_action_activate_reminder)
  is WorkflowAction.RunBackgroundTask -> action.taskKey
}

internal fun workflowActionValue(
  action: WorkflowAction,
  reminders: List<UiWorkflowReminderOption>
): String? = when (action) {
  is WorkflowAction.ActivateReminder -> reminders.firstOrNull { it.id == action.reminderId }?.title ?: action.reminderId
  else -> null
}
