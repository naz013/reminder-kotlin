package com.elementary.tasks.settings.additional.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.DataFlow
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault

class TemplateSingleBackupWorker(
  private val syncManagers: SyncManagers,
  context: Context,
  workerParams: WorkerParameters
) : Worker(context, workerParams) {

  override fun doWork(): Result {
    val uuId = inputData.getString(Constants.INTENT_ID) ?: ""
    if (uuId.isNotEmpty()) {
      launchDefault {
        DataFlow(
          syncManagers.repositoryManager.templateDataFlowRepository,
          syncManagers.converterManager.templateConverter,
          CompositeStorage(syncManagers.storageManager),
          completable = null
        ).backup(uuId)
      }
    }
    return Result.success()
  }
}