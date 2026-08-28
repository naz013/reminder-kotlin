package com.github.naz013.logic.workflow

import com.github.naz013.logic.reminder.ReminderWorkflowTrigger

/**
 * Single entry point shared by the periodic polling tasks and every reactive trigger call site:
 * runs the relevant [WorkflowEngine] method(s), then dispatches any [PendingWorkflowAction]
 * it returns via [WorkflowActionDispatcher]. Also implements [ReminderWorkflowTrigger], the seam
 * `logic-reminder` uses to trigger workflow evaluation without depending on `logic-workflow`.
 */
class WorkflowTriggerRunner(
  private val workflowEngine: WorkflowEngine,
  private val workflowActionDispatcher: WorkflowActionDispatcher
) : ReminderWorkflowTrigger {
  suspend fun runDailyPolling() {
    (workflowEngine.runAgeBasedRules() + workflowEngine.runGroupCompletionRules() + workflowEngine.runScheduleRules())
      .forEach { workflowActionDispatcher.dispatch(it) }
  }

  suspend fun runUnacknowledgedPolling() {
    workflowEngine.runUnacknowledgedRules().forEach { workflowActionDispatcher.dispatch(it) }
  }

  suspend fun onReminderCompleted(reminderId: String) {
    workflowEngine.runReminderCompletedRules(reminderId).forEach { workflowActionDispatcher.dispatch(it) }
  }

  override suspend fun onReminderSnoozed(reminderId: String) {
    workflowEngine.runSnoozeCountRules(reminderId).forEach { workflowActionDispatcher.dispatch(it) }
  }

  suspend fun onLocationEntered(reminderId: String) {
    workflowEngine.runLocationEnteredRules(reminderId).forEach { workflowActionDispatcher.dispatch(it) }
  }

  suspend fun onLocationExited(reminderId: String) {
    workflowEngine.runLocationExitedRules(reminderId).forEach { workflowActionDispatcher.dispatch(it) }
  }
}
