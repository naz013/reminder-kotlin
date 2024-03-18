package com.elementary.tasks.core.work.operation

import com.elementary.tasks.core.cloud.converters.IndexTypes
import com.elementary.tasks.core.cloud.storages.Storage
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.work.Operation
import com.elementary.tasks.core.work.SyncDataWorker

class SyncOperationsFactory(
  private val syncOperationFactory: SyncOperationFactory,
  private val prefs: Prefs
) {

  operator fun invoke(
    storage: Storage,
    ignoreFlags: Boolean = false
  ): List<Operation> {
    val syncFlags = prefs.autoSyncFlags
    val predicate: (String) -> Boolean = {
      ignoreFlags || syncFlags.contains(it)
    }
    return listOfNotNull(
      syncOperationFactory.create(IndexTypes.TYPE_GROUP, storage).takeIf {
        predicate(SyncDataWorker.FLAG_REMINDER)
      },
      syncOperationFactory.create(IndexTypes.TYPE_REMINDER, storage).takeIf {
        predicate(SyncDataWorker.FLAG_REMINDER)
      },
      syncOperationFactory.create(IndexTypes.TYPE_NOTE, storage).takeIf {
        predicate(SyncDataWorker.FLAG_NOTE)
      },
      syncOperationFactory.create(IndexTypes.TYPE_PLACE, storage).takeIf {
        predicate(SyncDataWorker.FLAG_PLACE)
      },
      syncOperationFactory.create(IndexTypes.TYPE_BIRTHDAY, storage).takeIf {
        predicate(SyncDataWorker.FLAG_BIRTHDAY)
      },
      syncOperationFactory.create(IndexTypes.TYPE_SETTINGS, storage).takeIf {
        predicate(SyncDataWorker.FLAG_SETTINGS)
      }
    )
  }
}
