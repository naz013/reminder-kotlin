package com.elementary.tasks.birthdays.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.DataFlow
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.view_models.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SingleBackupWorker(
  private val syncManagers: SyncManagers,
  context: Context,
  workerParams: WorkerParameters,
  private val dispatcherProvider: DispatcherProvider
) : Worker(context, workerParams) {

  private val job = SupervisorJob()
  private val scope = CoroutineScope(job)

  override fun doWork(): Result {
    val uuId = inputData.getString(Constants.INTENT_ID) ?: ""
    if (uuId.isNotEmpty()) {
      scope.launch {
        withContext(dispatcherProvider.default()) {
          DataFlow(
            syncManagers.repositoryManager.birthdayRepository,
            syncManagers.converterManager.birthdayConverter,
            CompositeStorage(syncManagers.storageManager),
            completable = null
          ).backup(uuId)
        }
      }
    }
    return Result.success()
  }
}