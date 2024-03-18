package com.elementary.tasks.core.work.operation

import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.completables.ReminderCompletable
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.converters.ReminderConverter
import com.elementary.tasks.core.cloud.repositories.ReminderDataFlowRepository
import com.elementary.tasks.core.cloud.storages.Storage
import com.elementary.tasks.core.work.Operation

class ReminderOperationFactory(
  private val repository: ReminderDataFlowRepository,
  private val converter: ReminderConverter,
  private val completable: ReminderCompletable
) {

  operator fun invoke(
    storage: Storage
  ): Operation {
    val dataFlow = BulkDataFlow(
      repository = repository,
      convertible = converter,
      storage = storage,
      completable = completable
    )
    return SyncOperation(dataFlow, IndexTypes.TYPE_REMINDER)
  }
}
