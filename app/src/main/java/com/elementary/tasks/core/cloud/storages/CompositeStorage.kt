package com.elementary.tasks.core.cloud.storages

import com.elementary.tasks.core.cloud.DataFlow
import com.elementary.tasks.core.cloud.converters.Metadata
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream

class CompositeStorage(
  storageManager: StorageManager
) : Storage() {

  private val storageList = DataFlow.availableStorageList(storageManager)
  private val dispatcherProvider = storageManager.dispatcherProvider

  init {
    Timber.d("init: $storageList")
  }

  override suspend fun backup(fileIndex: FileIndex, metadata: Metadata) {
    storageList.forEach { it.backup(fileIndex, metadata) }
  }

  override suspend fun restore(fileName: String): InputStream? {
    storageList.forEach {
      val data = it.restore(fileName)
      if (data != null) return data
    }
    return null
  }

  override suspend fun restoreAll(ext: String, deleteFile: Boolean): Channel<InputStream> {
    val channel = Channel<InputStream>()
    if (storageList.isEmpty()) {
      channel.cancel()
      return channel
    }
    withContext(dispatcherProvider.io()) {
      loadIndex()
      storageList.forEach {
        it.restoreAll(ext, deleteFile).consumeEach { json ->
          channel.send(json)
        }
      }
      channel.close()
    }
    return channel
  }

  override suspend fun delete(fileName: String) {
    storageList.forEach { it.delete(fileName) }
  }

  override suspend fun removeIndex(id: String) {
    storageList.forEach { it.removeIndex(id) }
  }

  override suspend fun saveIndex(fileIndex: FileIndex) {
    storageList.forEach { it.saveIndex(fileIndex) }
  }

  override suspend fun saveIndex() {
    storageList.forEach { it.saveIndex() }
  }

  override suspend fun hasIndex(id: String): Boolean {
    return false
  }

  override fun needBackup(id: String, updatedAt: String): Boolean {
    return true
  }

  override suspend fun loadIndex() {
    withContext(dispatcherProvider.io()) {
      storageList.forEach {
        it.loadIndex()
      }
    }
  }
}