package com.elementary.tasks.core.work.operation

import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.converters.SettingsConverter
import com.elementary.tasks.core.cloud.repositories.SettingsDataFlowRepository
import com.elementary.tasks.core.cloud.storages.Storage
import com.elementary.tasks.core.work.Operation

class SettingsOperationFactory(
  private val repository: SettingsDataFlowRepository,
  private val converter: SettingsConverter,
  private val operationFactory: OperationFactory
) {

  operator fun invoke(
    storage: Storage,
    syncOperationType: SyncOperationType
  ): Operation {
    val dataFlow = BulkDataFlow(
      repository = repository,
      convertible = converter,
      storage = storage,
      completable = null
    )
    return operationFactory(dataFlow, IndexTypes.TYPE_SETTINGS, syncOperationType)
  }
}
