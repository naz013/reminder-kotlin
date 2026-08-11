package com.github.naz013.feature.workflow

import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.logic.reminder.usecase.CompleteReminderUseCase
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.usecase.reminders.PendingWorkflowAction

/**
 * Finishes the two [WorkflowAction] variants [WorkflowEngine][com.github.naz013.usecase.reminders.WorkflowEngine]
 * (a pure-JVM module) can't apply itself, since both need `logic-reminder`'s
 * [ActivateReminderUseCase]/[CompleteReminderUseCase], which `usecase:reminders` doesn't depend on.
 */
class WorkflowActionDispatcher(
  private val reminderV2Repository: ReminderV2Repository,
  private val activateReminderUseCase: ActivateReminderUseCase,
  private val completeReminderUseCase: CompleteReminderUseCase
) {
  suspend fun dispatch(pending: PendingWorkflowAction) {
    when (val action = pending.action) {
      is WorkflowAction.CompleteReminder ->
        reminderV2Repository.getById(pending.contextReminderId)?.let { completeReminderUseCase(it) }

      is WorkflowAction.ActivateReminder ->
        reminderV2Repository.getById(action.reminderId)?.let { activateReminderUseCase(it) }

      is WorkflowAction.ArchiveReminder,
      is WorkflowAction.ApplyNotificationOverride,
      is WorkflowAction.RunBackgroundTask -> Unit // the engine never returns these as pending
    }
  }
}
