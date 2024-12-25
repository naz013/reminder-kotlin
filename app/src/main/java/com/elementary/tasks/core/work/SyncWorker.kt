package com.elementary.tasks.core.work

import com.github.naz013.appwidgets.AppWidgetUpdater
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.work.operation.SyncOperationType
import com.elementary.tasks.core.work.operation.SyncOperationsFactory
import kotlinx.coroutines.Job

class SyncWorker(
  private val syncManagers: SyncManagers,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val syncOperationsFactory: SyncOperationsFactory
) {

  private var mJob: Job? = null
  var onEnd: (() -> Unit)? = null
  var listener: ((Boolean) -> Unit)? = null
    set(value) {
      field = value
      value?.invoke(mJob != null)
    }

  fun sync() {
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
      OperationProcessor(
        syncOperationsFactory(storage, SyncOperationType.FULL, true)
      ).process()
      withUIContext {
        appWidgetUpdater.updateAllWidgets()
        appWidgetUpdater.updateNotesWidget()
        onEnd?.invoke()
      }
      mJob = null
    }
  }
}
