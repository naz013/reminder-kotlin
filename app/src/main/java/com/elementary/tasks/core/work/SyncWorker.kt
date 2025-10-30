package com.elementary.tasks.core.work

import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.sync.SyncApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SyncWorker(
  private val syncApi: SyncApi,
  private val dispatcherProvider: DispatcherProvider,
  private val appWidgetUpdater: AppWidgetUpdater,
) {

  private var mJob: Job? = null
  var onEnd: (() -> Unit)? = null
  var listener: ((Boolean) -> Unit)? = null
    set(value) {
      field = value
      value?.invoke(mJob != null)
    }

  fun sync(coroutineScope: CoroutineScope) {
    mJob?.cancel()
    launchSync(coroutineScope)
  }

  fun unsubscribe() {
    onEnd = null
    listener = null
  }

  private fun launchSync(coroutineScope: CoroutineScope) {
    mJob = coroutineScope.launch(dispatcherProvider.io()) {
      syncApi.sync()
      withContext(dispatcherProvider.main()) {
        appWidgetUpdater.updateAllWidgets()
        appWidgetUpdater.updateNotesWidget()
        onEnd?.invoke()
      }
      mJob = null
    }
  }
}
