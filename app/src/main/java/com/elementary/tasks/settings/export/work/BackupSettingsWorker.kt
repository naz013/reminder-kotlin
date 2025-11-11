package com.elementary.tasks.settings.export.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.sync.DataType
import com.github.naz013.sync.SyncApi
import kotlinx.coroutines.withContext

class BackupSettingsWorker(
  context: Context,
  workerParams: WorkerParameters,
  private val dispatcherProvider: DispatcherProvider,
  private val syncApi: SyncApi
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    withContext(dispatcherProvider.io()) {
      syncApi.upload(DataType.Settings)
    }
    return Result.success()
  }

  companion object {
    private const val TAG = "BackupSettingsWorker"

    fun schedule(context: Context) {
      val work = OneTimeWorkRequest.Builder(BackupSettingsWorker::class.java)
        .addTag(TAG)
        .setConstraints(
          Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .build()
        )
        .build()
      WorkManager.getInstance(context).enqueue(work)
    }
  }
}
