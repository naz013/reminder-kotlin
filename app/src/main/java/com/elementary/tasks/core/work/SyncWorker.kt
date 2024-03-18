package com.elementary.tasks.core.work

import com.elementary.tasks.core.analytics.Traces
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.work.operation.SyncOperationsFactory
import kotlinx.coroutines.Job

class SyncWorker(
  private val syncManagers: SyncManagers,
  private val updatesHelper: UpdatesHelper,
  private val syncOperationsFactory: SyncOperationsFactory
) {

  private var mJob: Job? = null
  private var mLastMsg: String? = null
  var onEnd: (() -> Unit)? = null
  var progress: ((String) -> Unit)? = null
    set(value) {
      field = value
      val msg = mLastMsg ?: return
      if (mJob != null) {
        value?.invoke(msg)
      }
    }
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
    progress = null
  }

  private fun launchSync() {
    val storage = CompositeStorage(syncManagers.storageManager)
    mJob = launchIo {
      notifyMsg("Syncing...")

      val result = OperationProcessor(syncOperationsFactory(storage, true)).process()

      Traces.log("Sync finished with result = $result")

      withUIContext {
        updatesHelper.updateWidgets()
        updatesHelper.updateNotesWidget()
        onEnd?.invoke()
      }
      mJob = null
    }
  }

  private suspend fun notifyMsg(msg: String) {
    mLastMsg = msg
    withUIContext { progress?.invoke(msg) }
  }
}
