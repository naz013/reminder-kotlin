package com.elementary.tasks.core.cloud.storages

import com.elementary.tasks.core.cloud.converters.Metadata
import com.elementary.tasks.core.utils.launchIo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import timber.log.Timber
import java.io.InputStream

class CompositeStorage(private val storageList: List<Storage>) : Storage() {

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

  override fun restoreAll(ext: String, deleteFile: Boolean): Channel<InputStream> {
    val channel = Channel<InputStream>()
    if (storageList.isEmpty()) {
      channel.cancel()
      return channel
    }
    launchIo {
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

  override fun removeIndex(id: String) {
    storageList.forEach { it.removeIndex(id) }
  }

  override fun saveIndex(fileIndex: FileIndex) {
    storageList.forEach { it.saveIndex(fileIndex) }
  }

  override suspend fun saveIndex() {
    storageList.forEach { it.saveIndex() }
  }

  override fun hasIndex(id: String): Boolean {
    return false
  }

  override fun needBackup(id: String, updatedAt: String): Boolean {
    return true
  }

  override suspend fun loadIndex() {
    launchIo {
      storageList.forEach {
        it.loadIndex()
      }
    }
  }

  override fun sendNotification(type: String, details: String) {
    storageList.forEach { it.sendNotification(type, details) }
  }
}