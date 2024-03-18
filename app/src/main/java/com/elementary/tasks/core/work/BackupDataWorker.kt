package com.elementary.tasks.core.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.DispatcherProvider
import kotlinx.coroutines.withContext

class BackupDataWorker(
  private val syncManagers: SyncManagers,
  context: Context,
  workerParams: WorkerParameters,
  private val dispatcherProvider: DispatcherProvider
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    withContext(dispatcherProvider.io()) {
      val storage = CompositeStorage(syncManagers.storageManager)
      BulkDataFlow(
        syncManagers.repositoryManager.groupDataFlowRepository,
        syncManagers.converterManager.groupConverter,
        storage,
        completable = null
      ).backup()
      BulkDataFlow(
        syncManagers.repositoryManager.reminderDataFlowRepository,
        syncManagers.converterManager.reminderConverter,
        storage,
        syncManagers.completableManager.reminderCompletable
      ).backup()
      BulkDataFlow(
        syncManagers.repositoryManager.noteDataFlowRepository,
        syncManagers.converterManager.noteConverter,
        storage,
        completable = null
      ).backup()
      BulkDataFlow(
        syncManagers.repositoryManager.birthdayDataFlowRepository,
        syncManagers.converterManager.birthdayConverter,
        storage,
        completable = null
      ).backup()
      BulkDataFlow(
        syncManagers.repositoryManager.placeDataFlowRepository,
        syncManagers.converterManager.placeConverter,
        storage,
        completable = null
      ).backup()
      BulkDataFlow(
        syncManagers.repositoryManager.settingsDataFlowRepository,
        syncManagers.converterManager.settingsConverter,
        storage,
        completable = null
      ).backup()
    }
    return Result.success()
  }

  companion object {
    private const val TAG = "BackupDataWorker"

    fun schedule(context: Context) {
      val work = OneTimeWorkRequest.Builder(BackupDataWorker::class.java)
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
