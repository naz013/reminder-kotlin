package com.elementary.tasks.core.work.operation

import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.completables.ReminderCompletable
import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.converters.ReminderConverter
import com.elementary.tasks.core.cloud.repositories.ReminderDataFlowRepository
import com.github.naz013.cloudapi.CloudFileApi
import com.elementary.tasks.core.work.Operation

class ReminderOperationFactory(
  private val repository: ReminderDataFlowRepository,
  private val converter: ReminderConverter,
  private val completable: ReminderCompletable,
  private val operationFactory: OperationFactory
) {

  operator fun invoke(
    storage: CloudFileApi,
    syncOperationType: SyncOperationType
  ): Operation {
    val dataFlow = BulkDataFlow(
      repository = repository,
      convertible = converter,
      storage = storage,
      completable = completable
    )
    return operationFactory(dataFlow, IndexTypes.TYPE_REMINDER, syncOperationType)
  }
}
