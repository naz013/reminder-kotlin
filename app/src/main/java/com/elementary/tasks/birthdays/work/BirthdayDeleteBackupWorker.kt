package com.elementary.tasks.birthdays.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.DataFlow
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.launchDefault

class BirthdayDeleteBackupWorker(
  private val syncManagers: SyncManagers,
  context: Context,
  workerParams: WorkerParameters
) : Worker(context, workerParams) {

  override fun doWork(): Result {
    val uuId = inputData.getString(Constants.INTENT_ID) ?: ""
    if (uuId.isNotEmpty()) {
      launchDefault {
        DataFlow(
          syncManagers.repositoryManager.birthdayDataFlowRepository,
          syncManagers.converterManager.birthdayConverter,
          CompositeStorage(syncManagers.storageManager),
          completable = null
        ).delete(uuId, IndexTypes.TYPE_BIRTHDAY)
      }
    }
    return Result.success()
  }
}
