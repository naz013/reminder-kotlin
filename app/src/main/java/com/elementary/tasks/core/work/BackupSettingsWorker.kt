package com.elementary.tasks.core.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.work.operation.SettingsOperationFactory
import com.elementary.tasks.core.work.operation.SyncOperationType
import kotlinx.coroutines.withContext

class BackupSettingsWorker(
  private val syncManagers: SyncManagers,
  context: Context,
  workerParams: WorkerParameters,
  private val dispatcherProvider: DispatcherProvider,
  private val settingsOperationFactory: SettingsOperationFactory
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    withContext(dispatcherProvider.io()) {
      OperationProcessor(
        listOf(
          settingsOperationFactory(
            storage = CompositeStorage(syncManagers.storageManager),
            syncOperationType = SyncOperationType.JUST_BACKUP
          )
        )
      ).process()
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
