package com.github.naz013.logic.reminder

/**
 * Seam over `logic-workflow`'s `WorkflowTriggerRunner`, which `logic-reminder` can't depend on
 * directly (`logic-workflow` already depends on `logic-reminder` for
 * `ActivateReminderUseCase`/`CompleteReminderUseCase`). Implemented by `WorkflowTriggerRunner`
 * itself and bound via Koin in `logic-workflow`.
 *
 * Beyond the Gradle module cycle, the Koin object graph is circular too:
 * `WorkflowTriggerRunner` -> `WorkflowActionDispatcher` -> `ActivateReminderUseCase` ->
 * `SaveReminderUseCase` -> [ReminderWorkflowTrigger]. Every call site in `logic-reminder` must
 * therefore inject `Lazy<ReminderWorkflowTrigger>` (resolved via `lazy { get() }` in the Koin
 * module, not `factoryOf`) rather than `ReminderWorkflowTrigger` directly - eager injection
 * makes Koin recurse into that cycle while building the graph and stack-overflow.
 */
interface ReminderWorkflowTrigger {
  suspend fun onReminderSnoozed(reminderId: String)

  suspend fun onReminderCreated(reminderId: String)
}
