package com.elementary.tasks.core.work.operation

import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.work.Operation
import com.elementary.tasks.core.work.OperationResult
import com.github.naz013.logging.Logger

class SyncOperation<T>(
  private val bulkDataFlow: BulkDataFlow<T>,
  private val indexTypes: IndexTypes
) : Operation {

  override suspend fun process(): OperationResult {
    return try {
      Logger.i("Begin backup for $indexTypes")
      bulkDataFlow.backup()

      Logger.i("Begin restoring of data for $indexTypes")
      bulkDataFlow.restore(indexTypes = indexTypes, deleteFile = false)

      Logger.i("Sync complete for $indexTypes")
      OperationResult.Success
    } catch (e: Throwable) {
      OperationResult.Failed
    }
  }
}
