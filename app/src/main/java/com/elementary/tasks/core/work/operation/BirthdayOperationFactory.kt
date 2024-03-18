package com.elementary.tasks.core.work.operation

import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.converters.BirthdayConverter
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.repositories.BirthdayDataFlowRepository
import com.elementary.tasks.core.cloud.storages.Storage
import com.elementary.tasks.core.work.Operation

class BirthdayOperationFactory(
  private val repository: BirthdayDataFlowRepository,
  private val converter: BirthdayConverter
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
    return SyncOperation(dataFlow, IndexTypes.TYPE_BIRTHDAY)
  }
}
