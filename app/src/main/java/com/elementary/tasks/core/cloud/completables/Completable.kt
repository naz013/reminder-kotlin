package com.elementary.tasks.core.cloud.completables

interface Completable<T> {
  suspend fun action(t: T)
}