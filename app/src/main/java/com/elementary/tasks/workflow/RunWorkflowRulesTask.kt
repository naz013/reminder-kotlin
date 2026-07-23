package com.elementary.tasks.workflow

import com.github.naz013.logging.Logger
import com.github.naz013.usecase.reminders.WorkflowEngine
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult

class RunWorkflowRulesTask(
  private val workflowEngine: WorkflowEngine
) : BackgroundTask {
  override suspend fun run(
    input: TaskData,
    progress: TaskProgressReporter
  ): TaskResult {
    workflowEngine.runAgeBasedRules()
    Logger.i(TASK_KEY, "Ran age-based workflow rules.")
    return TaskResult.Success
  }

  companion object {
    const val TASK_KEY = "run_workflow_rules"
  }
}
