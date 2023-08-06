package com.elementary.tasks.core.work

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.core.cloud.BulkDataFlow
import com.elementary.tasks.core.cloud.SyncManagers
import com.elementary.tasks.core.cloud.storages.CompositeStorage
import com.elementary.tasks.core.utils.launchIo
import com.elementary.tasks.core.utils.withUIContext
import kotlinx.coroutines.Job
import timber.log.Timber

class BackupWorker(
  private val syncManagers: SyncManagers,
  private val context: Context
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
    progress = null
  }

  private fun launchSync() {
    val storage = CompositeStorage(syncManagers.storageManager)

    mJob = launchIo {
      notifyMsg(context.getString(R.string.syncing_groups))
      BulkDataFlow(
        syncManagers.repositoryManager.groupDataFlowRepository,
        syncManagers.converterManager.groupConverter,
        storage,
        completable = null
      ).backup()

      notifyMsg(context.getString(R.string.syncing_reminders))
      BulkDataFlow(
        syncManagers.repositoryManager.reminderDataFlowRepository,
        syncManagers.converterManager.reminderConverter,
        storage,
        syncManagers.completableManager.reminderCompletable
      ).backup()

      notifyMsg(context.getString(R.string.syncing_notes))
      BulkDataFlow(
        syncManagers.repositoryManager.noteDataFlowRepository,
        syncManagers.converterManager.noteConverter,
        storage,
        completable = null
      ).backup()

      notifyMsg(context.getString(R.string.syncing_birthdays))
      BulkDataFlow(
        syncManagers.repositoryManager.birthdayDataFlowRepository,
        syncManagers.converterManager.birthdayConverter,
        storage,
        completable = null
      ).backup()

      notifyMsg(context.getString(R.string.syncing_places))
      BulkDataFlow(
        syncManagers.repositoryManager.placeDataFlowRepository,
        syncManagers.converterManager.placeConverter,
        storage,
        completable = null
      ).backup()

      BulkDataFlow(
        syncManagers.repositoryManager.settingsDataFlowRepository,
        syncManagers.converterManager.settingsConverter,
        storage,
        completable = null
      ).backup()

      withUIContext {
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
