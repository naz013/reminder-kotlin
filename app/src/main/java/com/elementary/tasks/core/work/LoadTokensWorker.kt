package com.elementary.tasks.core.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.cloud.storages.StorageManager
import com.elementary.tasks.core.utils.launchIo

class LoadTokensWorker(
  private val storageManager: StorageManager,
  context: Context,
  workerParams: WorkerParameters
) : Worker(context, workerParams) {

  override fun doWork(): Result {
    launchIo { CompositeStorage(storageManager).loadIndex() }
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