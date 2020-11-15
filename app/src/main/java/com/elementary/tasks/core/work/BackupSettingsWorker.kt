package com.elementary.tasks.core.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.DataFlow
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.launchIo

class BackupSettingsWorker(
  private val syncManagers: SyncManagers,
  context: Context,
  workerParams: WorkerParameters
) : Worker(context, workerParams) {

  override fun doWork(): Result {
    launchIo {
      DataFlow(
        syncManagers.repositoryManager.settingsRepository,
        syncManagers.converterManager.settingsConverter,
        CompositeStorage(syncManagers.storageManager),
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
        .setConstraints(Constraints.Builder()
          .setRequiredNetworkType(NetworkType.UNMETERED)
          .setRequiresBatteryNotLow(true)
          .build())
        .build()
      WorkManager.getInstance(context).enqueue(work)
    }
  }
}