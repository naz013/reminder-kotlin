package com.elementary.tasks.googletasks.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.utils.Constants
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.domain.GoogleTask
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.GoogleTaskRepository
import com.google.gson.Gson
import kotlinx.coroutines.withContext

class UpdateTaskWorker(
  context: Context,
  workerParams: WorkerParameters,
  private val googleTasksApi: GoogleTasksApi,
  private val dispatcherProvider: DispatcherProvider,
  private val googleTaskRepository: GoogleTaskRepository
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    val json = inputData.getString(Constants.INTENT_JSON) ?: "{}"
    val status = inputData.getString(Constants.INTENT_STATUS) ?: GoogleTask.TASKS_NEED_ACTION
    if (json.isNotEmpty()) {
      withContext(dispatcherProvider.io()) {
        val googleTask = Gson().fromJson(json, GoogleTask::class.java)
        if (googleTask != null) {
          googleTasksApi.updateTaskStatus(status, googleTask)?.let {
            googleTaskRepository.save(it)
          }
        }
      }
    }
    return Result.success()
  }
}
