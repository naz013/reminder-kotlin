package com.elementary.tasks.core.cloud.storages

interface DataChannel<T> {
  suspend fun onNewData(data: T)
}