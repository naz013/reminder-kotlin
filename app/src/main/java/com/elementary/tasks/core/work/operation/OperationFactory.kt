package com.elementary.tasks.core.work.operation

import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.work.Operation

class OperationFactory {

  operator fun <T> invoke(
    bulkDataFlow: BulkDataFlow<T>,
    indexTypes: IndexTypes,
    syncOperationType: SyncOperationType
  ): Operation {
    return if (syncOperationType == SyncOperationType.FULL) {
      SyncOperation(bulkDataFlow, indexTypes)
    } else {
      BackupOperation(bulkDataFlow, indexTypes)
    }
  }
}
