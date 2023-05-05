package com.elementary.tasks.settings.additional.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.elementary.tasks.core.cloud.DataFlow
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import kotlinx.coroutines.withContext

@Deprecated("After S")
class TemplateDeleteBackupWorker(
  private val syncManagers: SyncManagers,
  context: Context,
  workerParams: WorkerParameters,
  private val dispatcherProvider: DispatcherProvider
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    val uuId = inputData.getString(Constants.INTENT_ID) ?: ""
    if (uuId.isNotEmpty()) {
      withContext(dispatcherProvider.default()) {
        DataFlow(
          syncManagers.repositoryManager.templateDataFlowRepository,
          syncManagers.converterManager.templateConverter,
          CompositeStorage(syncManagers.storageManager),
          completable = null
        ).delete(uuId, IndexTypes.TYPE_TEMPLATE)
      }
    }
    return Result.success()
  }
}