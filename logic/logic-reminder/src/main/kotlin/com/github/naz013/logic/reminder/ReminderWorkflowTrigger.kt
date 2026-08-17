package com.github.naz013.logic.reminder

/**
 * Seam over `logic-workflow`'s `WorkflowTriggerRunner`, which `logic-reminder` can't depend on
 * directly (`logic-workflow` already depends on `logic-reminder` for
 * `ActivateReminderUseCase`/`CompleteReminderUseCase`). Implemented by `WorkflowTriggerRunner`
 * itself and bound via Koin in `logic-workflow`.
 */
interface ReminderWorkflowTrigger {
  suspend fun onReminderSnoozed(reminderId: String)
}
