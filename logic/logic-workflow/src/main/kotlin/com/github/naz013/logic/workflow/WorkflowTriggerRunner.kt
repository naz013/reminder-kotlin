package com.github.naz013.logic.workflow

import com.github.naz013.logic.reminder.ReminderWorkflowTrigger

/**
 * Single entry point shared by the periodic polling tasks and every reactive trigger call site:
 * runs the relevant [WorkflowEngine] method(s), then dispatches any [PendingWorkflowAction]
 * it returns via [WorkflowActionDispatcher]. Also implements [ReminderWorkflowTrigger], the seam
 * `logic-reminder` uses to trigger workflow evaluation without depending on `logic-workflow`.
 *
 * Every method is gated on [WorkflowConfig.isEnabled] - this is the *only* place that flag is
 * checked at the execution layer (UI call sites only use it to hide entry points from the user,
 * see `WorkflowRulesUtil`'s doc comment and `BottomNavInitViewModel.checkDb()`). Existing
 * [com.github.naz013.domain.workflow.WorkflowRule]s aren't deleted when the flag is turned off, and
 * the periodic polling `BackgroundTask`s stay scheduled once `BottomNavInitViewModel` has scheduled
 * them once - without this gate, disabling the flag (e.g. a remote kill switch) would silently keep
 * archiving/purging/notifying against those rules instead of actually stopping the feature.
 */
class WorkflowTriggerRunner(
  private val workflowEngine: WorkflowEngine,
  private val workflowActionDispatcher: WorkflowActionDispatcher,
  private val workflowConfig: WorkflowConfig
) : ReminderWorkflowTrigger {
  suspend fun runDailyPolling() {
    if (!workflowConfig.isEnabled) return
    (workflowEngine.runAgeBasedRules() + workflowEngine.runGroupCompletionRules() + workflowEngine.runScheduleRules())
      .forEach { workflowActionDispatcher.dispatch(it) }
  }

  suspend fun runUnacknowledgedPolling() {
    if (!workflowConfig.isEnabled) return
    workflowEngine.runUnacknowledgedRules().forEach { workflowActionDispatcher.dispatch(it) }
  }

  suspend fun onReminderCompleted(reminderId: String) {
    if (!workflowConfig.isEnabled) return
    workflowEngine.runReminderCompletedRules(reminderId).forEach { workflowActionDispatcher.dispatch(it) }
  }

  override suspend fun onReminderSnoozed(reminderId: String) {
    if (!workflowConfig.isEnabled) return
    workflowEngine.runSnoozeCountRules(reminderId).forEach { workflowActionDispatcher.dispatch(it) }
  }

  override suspend fun onReminderCreated(reminderId: String) {
    if (!workflowConfig.isEnabled) return
    workflowEngine.runReminderCreatedRules(reminderId).forEach { workflowActionDispatcher.dispatch(it) }
  }

  suspend fun onLocationEntered(reminderId: String) {
    if (!workflowConfig.isEnabled) return
    workflowEngine.runLocationEnteredRules(reminderId).forEach { workflowActionDispatcher.dispatch(it) }
  }

  suspend fun onLocationExited(reminderId: String) {
    if (!workflowConfig.isEnabled) return
    workflowEngine.runLocationExitedRules(reminderId).forEach { workflowActionDispatcher.dispatch(it) }
  }
}
