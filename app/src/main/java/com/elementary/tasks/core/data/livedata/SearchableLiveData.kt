package com.elementary.tasks.core.data.livedata

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class SearchableLiveData<T>(
  private val scope: CoroutineScope
) : LiveData<T>() {

  private var job: Job? = null
  private var query: String = ""

  protected abstract fun runQuery(query: String): T

  fun refresh() {
    load()
  }

  fun onNewQuery(s: String) {
    if (query != s) {
      query = s
      load()
    }
  }

  override fun onActive() {
    super.onActive()
    load()
  }

  override fun onInactive() {
    super.onInactive()
    job?.cancel()
  }

  private fun load() {
    job?.cancel()
    job = scope.launch {
      runCatching { postValue(runQuery(query)) }
    }
  }
}
