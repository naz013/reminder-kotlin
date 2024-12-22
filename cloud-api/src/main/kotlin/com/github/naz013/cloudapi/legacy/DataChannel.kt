package com.github.naz013.cloudapi.legacy

interface DataChannel<T> {
  suspend fun onNewData(data: T)
}
