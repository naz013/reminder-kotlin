package com.elementary.tasks.core.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.DataFlow
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.DispatcherProvider
import kotlinx.coroutines.withContext

class BackupSettingsWorker(
  private val syncManagers: SyncManagers,
  context: Context,
  workerParams: WorkerParameters,
  private val dispatcherProvider: DispatcherProvider
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    withContext(dispatcherProvider.io()) {
      DataFlow(
        repository = syncManagers.repositoryManager.settingsDataFlowRepository,
        convertible = syncManagers.converterManager.settingsConverter,
        storage = CompositeStorage(syncManagers.storageManager),
        completable = null
      ).backup("")
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
