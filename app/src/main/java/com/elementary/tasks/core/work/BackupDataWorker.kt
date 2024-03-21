package com.elementary.tasks.core.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.work.operation.SyncOperationType
import com.elementary.tasks.core.work.operation.SyncOperationsFactory
import kotlinx.coroutines.withContext

class BackupDataWorker(
  private val syncManagers: SyncManagers,
  context: Context,
  workerParams: WorkerParameters,
  private val dispatcherProvider: DispatcherProvider,
  private val syncOperationsFactory: SyncOperationsFactory
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    withContext(dispatcherProvider.io()) {
      Traces.log("Start full backup")
      val storage = CompositeStorage(syncManagers.storageManager)
      val result = OperationProcessor(
        syncOperationsFactory(storage, SyncOperationType.JUST_BACKUP)
      ).process()
      Traces.log("Full backup completed with result = $result")
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
