package com.github.naz013.feature.workflow

import com.github.naz013.usecase.reminders.WorkflowEngine

/**
 * Single entry point shared by the periodic polling tasks and every reactive trigger call site:
 * runs the relevant [WorkflowEngine] method(s), then dispatches any [PendingWorkflowAction][com.github.naz013.usecase.reminders.PendingWorkflowAction]
 * it returns via [WorkflowActionDispatcher].
 */
class WorkflowTriggerRunner(
  private val workflowEngine: WorkflowEngine,
  private val workflowActionDispatcher: WorkflowActionDispatcher
) {
  suspend fun runDailyPolling() {
    (workflowEngine.runAgeBasedRules() + workflowEngine.runGroupCompletionRules())
      .forEach { workflowActionDispatcher.dispatch(it) }
  }

  suspend fun runUnacknowledgedPolling() {
    workflowEngine.runUnacknowledgedRules().forEach { workflowActionDispatcher.dispatch(it) }
  }

  suspend fun onReminderCompleted(reminderId: String) {
    workflowEngine.runReminderCompletedRules(reminderId).forEach { workflowActionDispatcher.dispatch(it) }
  }

  suspend fun onReminderSnoozed(reminderId: String) {
    workflowEngine.runSnoozeCountRules(reminderId).forEach { workflowActionDispatcher.dispatch(it) }
  }

  suspend fun onLocationEntered(reminderId: String) {
    workflowEngine.runLocationEnteredRules(reminderId).forEach { workflowActionDispatcher.dispatch(it) }
  }

  suspend fun onLocationExited(reminderId: String) {
    workflowEngine.runLocationExitedRules(reminderId).forEach { workflowActionDispatcher.dispatch(it) }
  }
}
