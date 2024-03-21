package com.elementary.tasks.core.work.operation

import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.converters.NoteConverter
import com.elementary.tasks.core.cloud.repositories.NoteDataFlowRepository
import com.elementary.tasks.core.cloud.storages.Storage
import com.elementary.tasks.core.work.Operation

class NoteOperationFactory(
  private val repository: NoteDataFlowRepository,
  private val converter: NoteConverter,
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
    return operationFactory(dataFlow, IndexTypes.TYPE_NOTE, syncOperationType)
  }
}
