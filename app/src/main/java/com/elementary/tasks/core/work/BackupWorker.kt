package com.elementary.tasks.core.work

import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.sync.SyncApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BackupWorker(
  private val syncApi: SyncApi,
  private val dispatcherProvider: DispatcherProvider
) {

  private var mJob: Job? = null
  var onEnd: (() -> Unit)? = null
  var listener: ((Boolean) -> Unit)? = null
    set(value) {
      field = value
      Logger.d("BackupWorker: $mJob")
      value?.invoke(mJob != null)
    }

  fun backup(coroutineScope: CoroutineScope) {
    mJob?.cancel()
    launchSync(coroutineScope)
  }

  fun unsubscribe() {
    onEnd = null
    listener = null
  }

  private fun launchSync(coroutineScope: CoroutineScope) {
    mJob = coroutineScope.launch(dispatcherProvider.io()) {
      Logger.i(TAG, "Start full backup")
      syncApi.upload()
      Logger.i(TAG, "Full backup completed")
      withContext(dispatcherProvider.main()) {
        onEnd?.invoke()
      }
      mJob = null
    }
  }

  companion object {
    private const val TAG = "BackupWorker"
  }
}
