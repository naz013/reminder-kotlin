package com.elementary.tasks.googletasks.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.domain.GoogleTask
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.GoogleTaskRepository
import com.google.gson.Gson
import kotlinx.coroutines.withContext

class SaveNewTaskWorker(
  context: Context,
  workerParams: WorkerParameters,
  private val googleTasksApi: GoogleTasksApi,
  private val dispatcherProvider: DispatcherProvider,
  private val googleTaskRepository: GoogleTaskRepository
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    val json = inputData.getString(IntentKeys.INTENT_JSON) ?: "{}"
    if (json.isNotEmpty()) {
      withContext(dispatcherProvider.io()) {
        val googleTask = Gson().fromJson(json, GoogleTask::class.java)
        if (googleTask != null) {
          googleTasksApi.saveTask(googleTask)?.let {
            googleTaskRepository.save(it)
          }
        }
      }
    }
    return Result.success()
  }
}
