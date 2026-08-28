package com.github.naz013.logic.workflow

import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.logic.reminder.usecase.CompleteReminderUseCase
import com.github.naz013.repository.ReminderV2Repository

/**
 * Finishes the two [WorkflowAction] variants [WorkflowEngine] (a pure-JVM engine) can't apply
 * itself, since both need `logic-reminder`'s [ActivateReminderUseCase]/[CompleteReminderUseCase].
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
      is WorkflowAction.PurgeReminder,
      is WorkflowAction.ApplyNotificationOverride,
      is WorkflowAction.ClearNotificationOverride,
      is WorkflowAction.MoveToGroup,
      is WorkflowAction.RunBackgroundTask -> Unit // the engine never returns these as pending
    }
  }
}
