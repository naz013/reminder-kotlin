package com.elementary.tasks.core.work.operation

import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.work.Operation
import com.elementary.tasks.core.work.OperationResult

class SyncOperation<T>(
  private val bulkDataFlow: BulkDataFlow<T>,
  val indexTypes: IndexTypes
) : Operation {

  override suspend fun process(): OperationResult {
    return try {
      Traces.log("Begin backup for $indexTypes")
      bulkDataFlow.backup()

      Traces.log("Begin restoring of data for $indexTypes")
      bulkDataFlow.restore(indexTypes = indexTypes, deleteFile = false)

      Traces.log("Sync complete for $indexTypes")
      OperationResult.Success
    } catch (e: Throwable) {
      OperationResult.Failed
    }
  }
}
