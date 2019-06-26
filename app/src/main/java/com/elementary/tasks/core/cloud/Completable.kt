package com.elementary.tasks.core.cloud

interface Completable<T> {
    fun action(t: T)
}