package com.elementary.tasks.core.cloud.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.sync.DataType
import com.github.naz013.sync.SyncApi
import kotlinx.coroutines.withContext

class UploadWorker(
  context: Context,
  workerParams: WorkerParameters,
  private val dispatcherProvider: DispatcherProvider,
  private val syncApi: SyncApi
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    val dataType = inputData.getString(WorkerData.DATA_TYPE)
      ?.takeIf { it.isNotEmpty() }
      ?.let { DataType.valueOf(it) }
    val itemId = inputData.getString(WorkerData.ITEM_ID)
      ?.takeIf { it.isNotEmpty() }
    withContext(dispatcherProvider.io()) {
      if (dataType != null) {
        if (itemId != null) {
          syncApi.upload(dataType, itemId)
        } else {
          syncApi.upload(dataType)
        }
      } else {
        syncApi.upload()
      }
    }
    return Result.success()
  }
}
