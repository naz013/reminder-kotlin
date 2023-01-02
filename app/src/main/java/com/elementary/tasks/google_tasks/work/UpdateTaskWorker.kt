package com.elementary.tasks.google_tasks.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.google.gson.Gson
import kotlinx.coroutines.withContext

class UpdateTaskWorker(
  context: Context,
  workerParams: WorkerParameters,
  private val gTasks: GTasks,
  private val dispatcherProvider: DispatcherProvider
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    val json = inputData.getString(Constants.INTENT_JSON) ?: "{}"
    val status = inputData.getString(Constants.INTENT_STATUS) ?: GTasks.TASKS_NEED_ACTION
    if (json.isNotEmpty()) {
      withContext(dispatcherProvider.io()) {
        val googleTask = Gson().fromJson(json, GoogleTask::class.java)
        if (googleTask != null && gTasks.isLogged) {
          runCatching {
            gTasks.updateTaskStatus(status, googleTask)
          }
        }
      }
    }
    return Result.success()
  }
}
