package com.elementary.tasks.core.work

import android.content.Context
import androidx.work.*
import com.elementary.tasks.core.cloud.DataFlow
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.launchIo

class LoadTokensWorker(
  context: Context,
  workerParams: WorkerParameters
) : Worker(context, workerParams) {

  override fun doWork(): Result {
    launchIo {
      CompositeStorage(DataFlow.availableStorageList(applicationContext)).loadIndex()
    }
    return Result.success()
  }

  companion object {
    private const val TAG = "LoadTokensWorker"

    fun schedule(context: Context) {
      val work = OneTimeWorkRequest.Builder(LoadTokensWorker::class.java)
        .addTag(TAG)
        .setConstraints(Constraints.Builder()
          .setRequiredNetworkType(NetworkType.UNMETERED)
          .setRequiresBatteryNotLow(true)
          .build())
        .build()
      WorkManager.getInstance(context).enqueue(work)
    }
  }
}