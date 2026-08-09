package com.github.naz013.feature.googletask.work

import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.GoogleTask
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.workapi.BackgroundTask
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult
import com.google.gson.Gson

class UpdateTaskTask(
  private val googleTasksApi: GoogleTasksApi,
  private val googleTaskRepository: GoogleTaskRepository,
) : BackgroundTask {
  override suspend fun run(
    input: TaskData,
    progress: TaskProgressReporter,
  ): TaskResult {
    val json = input.getString(IntentKeys.INTENT_JSON) ?: "{}"
    val status = input.getString(IntentKeys.INTENT_STATUS) ?: GoogleTask.TASKS_NEED_ACTION
    if (json.isNotEmpty()) {
      val googleTask = Gson().fromJson(json, GoogleTask::class.java)
      if (googleTask != null) {
        googleTasksApi.updateTaskStatus(status, googleTask)?.let {
          googleTaskRepository.save(it)
        }
      }
    }
    return TaskResult.Success
  }

  companion object {
    const val TASK_KEY = "update_google_task"
  }
}
