package com.elementary.tasks.workflow

import com.github.naz013.logging.Logger
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult

class RunWorkflowRulesTask(
  private val workflowTriggerRunner: WorkflowTriggerRunner
) : BackgroundTask {
  override suspend fun run(
    input: TaskData,
    progress: TaskProgressReporter
  ): TaskResult {
    workflowTriggerRunner.runDailyPolling()
    Logger.i(TASK_KEY, "Ran age-based and group-completion workflow rules.")
    return TaskResult.Success
  }

  companion object {
    const val TASK_KEY = "run_workflow_rules"
  }
}
