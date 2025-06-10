package com.elementary.tasks.reminder.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.DataFlow
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import kotlinx.coroutines.withContext

class ReminderSingleBackupWorker(
  context: Context,
  workerParams: WorkerParameters,
  private val syncManagers: SyncManagers,
  private val dispatcherProvider: DispatcherProvider
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    val uuId = inputData.getString(IntentKeys.INTENT_ID) ?: ""
    Logger.i(TAG, "Start reminder backup with id = $uuId")
    if (uuId.isNotEmpty()) {
      withContext(dispatcherProvider.io()) {
        DataFlow(
          syncManagers.repositoryManager.reminderDataFlowRepository,
          syncManagers.converterManager.reminderConverter,
          CompositeStorage(syncManagers.storageManager),
          completable = null
        ).backup(uuId)
      }
    }
    return Result.success()
  }

  companion object {
    private const val TAG = "ReminderSingleBackupWorker"
  }
}
