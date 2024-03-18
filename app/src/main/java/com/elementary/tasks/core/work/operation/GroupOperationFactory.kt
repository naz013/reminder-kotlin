package com.elementary.tasks.core.work.operation

import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.converters.GroupConverter
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.repositories.GroupDataFlowRepository
import com.elementary.tasks.core.cloud.storages.Storage
import com.elementary.tasks.core.work.Operation

class GroupOperationFactory(
  private val repository: GroupDataFlowRepository,
  private val converter: GroupConverter
) {

  operator fun invoke(
    storage: Storage
  ): Operation {
    val dataFlow = BulkDataFlow(
      repository = repository,
      convertible = converter,
      storage = storage,
      completable = null
    )
    return SyncOperation(dataFlow, IndexTypes.TYPE_GROUP)
  }
}
