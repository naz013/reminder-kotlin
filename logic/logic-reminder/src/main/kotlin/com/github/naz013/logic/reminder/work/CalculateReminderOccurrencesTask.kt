package com.github.naz013.logic.reminder.work

import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.occurrence.CalculateReminderOccurrencesUseCase
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult
import com.github.naz013.workapi.WorkRequest

class CalculateReminderOccurrencesTask(
  private val calculateReminderOccurrencesUseCase: CalculateReminderOccurrencesUseCase,
) : BackgroundTask {
  override suspend fun run(
    input: TaskData,
    progress: TaskProgressReporter,
  ): TaskResult {
    val itemId =
      input
        .getString(ARG_ID)
        ?.takeIf { it.isNotEmpty() }
        ?: run {
          Logger.w(TASK_KEY, "No reminder id provided")
          return TaskResult.Success
        }

    calculateReminderOccurrencesUseCase(itemId)

    return TaskResult.Success
  }

  companion object {
    const val TASK_KEY = "CalculateReminderOccurrences"
    private const val ARG_ID = "arg_id"

    fun prepareWorkRequest(id: String): WorkRequest =
      WorkRequest(
        taskKey = TASK_KEY,
        tag = "$TASK_KEY-$id",
        input = TaskData.builder().putString(ARG_ID, id).build(),
      ).also { Logger.i(TASK_KEY, "Prepared work: tag=${it.tag}, id=$id") }
  }
}
