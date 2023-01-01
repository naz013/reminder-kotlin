package com.elementary.tasks.google_tasks.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.GTasks
import com.elementary.tasks.core.data.models.GoogleTask
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.view_models.DispatcherProvider
import com.google.gson.Gson
import kotlinx.coroutines.withContext

class SaveNewTaskWorker(
  context: Context,
  workerParams: WorkerParameters,
  private val gTasks: GTasks,
  private val dispatcherProvider: DispatcherProvider
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    val json = inputData.getString(Constants.INTENT_JSON) ?: "{}"
    if (json.isNotEmpty()) {
      withContext(dispatcherProvider.io()) {
        val googleTask = Gson().fromJson(json, GoogleTask::class.java)
        if (googleTask != null && gTasks.isLogged) {
          runCatching {
            gTasks.insertTask(googleTask)
          }
        }
      }
    }
    return Result.success()
  }
}
