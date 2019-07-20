package com.elementary.tasks.core.cloud.storages

interface Indexable {
    fun removeIndex(id: String)

    fun saveIndex(fileIndex: FileIndex)

    fun hasIndex(id: String): Boolean

    fun needBackup(id: String, updatedAt: String): Boolean

    suspend fun loadIndex()
}