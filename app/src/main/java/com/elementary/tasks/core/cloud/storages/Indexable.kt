package com.elementary.tasks.core.cloud.storages

interface Indexable {
  suspend fun removeIndex(id: String)
  suspend fun saveIndex(fileIndex: FileIndex)
  suspend fun hasIndex(id: String): Boolean
  fun needBackup(id: String, updatedAt: String): Boolean
  suspend fun loadIndex()
  suspend fun saveIndex()
}
