package com.elementary.tasks.core.cloud

interface Backupable<T> {
    fun backup(t: T)
}