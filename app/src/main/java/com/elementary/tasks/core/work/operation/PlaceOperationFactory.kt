package com.elementary.tasks.core.work.operation

import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.converters.PlaceConverter
import com.elementary.tasks.core.cloud.repositories.PlaceDataFlowRepository
import com.elementary.tasks.core.cloud.storages.Storage
import com.elementary.tasks.core.work.Operation

class PlaceOperationFactory(
  private val repository: PlaceDataFlowRepository,
  private val converter: PlaceConverter
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
    return SyncOperation(dataFlow, IndexTypes.TYPE_PLACE)
  }
}
