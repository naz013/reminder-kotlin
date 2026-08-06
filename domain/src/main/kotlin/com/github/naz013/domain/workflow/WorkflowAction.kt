package com.github.naz013.domain.workflow

import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride

sealed class WorkflowAction {
  /** Powers the auto-archive workflow. */
  data object ArchiveReminder : WorkflowAction()

  data object CompleteReminder : WorkflowAction()

  data class ApplyNotificationOverride(
    val override: NotificationSettingsOverride
  ) : WorkflowAction()

  /** Chained/dependent reminders: activates another reminder by id. */
  data class ActivateReminder(
    val reminderId: String
  ) : WorkflowAction()

  /** Escape hatch mirroring ResolvedEventAction's open-endedness: runs an existing BackgroundTask. */
  data class RunBackgroundTask(
    val taskKey: String
  ) : WorkflowAction()
}
