package com.elementary.tasks.core.work

import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.work.operation.SyncOperationType
import com.elementary.tasks.core.work.operation.SyncOperationsFactory
import kotlinx.coroutines.Job
import timber.log.Timber

class BackupWorker(
  private val syncManagers: SyncManagers,
  private val syncOperationsFactory: SyncOperationsFactory
) {

  private var mJob: Job? = null
  var onEnd: (() -> Unit)? = null
  var listener: ((Boolean) -> Unit)? = null
    set(value) {
      field = value
      Timber.d("BackupWorker: $mJob")
      value?.invoke(mJob != null)
    }

  fun backup() {
    mJob?.cancel()
    launchSync()
  }

  fun unsubscribe() {
    onEnd = null
    listener = null
  }

  private fun launchSync() {
    val storage = CompositeStorage(syncManagers.storageManager)
    mJob = launchIo {
      Traces.log("Start full backup")
      val result = OperationProcessor(
        syncOperationsFactory(storage, SyncOperationType.JUST_BACKUP, true)
      ).process()
      Traces.log("Full backup completed with result = $result")
      withUIContext {
        onEnd?.invoke()
      }
      mJob = null
    }
  }
}
