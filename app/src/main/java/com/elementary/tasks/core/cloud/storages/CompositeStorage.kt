package com.elementary.tasks.core.cloud.storages

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.cloudapi.CloudFiles
import com.github.naz013.cloudapi.legacy.Convertible
import com.github.naz013.cloudapi.legacy.DataChannel
import com.github.naz013.cloudapi.legacy.Metadata
import com.github.naz013.cloudapi.stream.CopyByteArrayStream
import com.github.naz013.logging.Logger
import java.io.InputStream

class CompositeStorage(
  storageManager: StorageManager
) : CloudFileApi {

  private val storageList = storageManager.availableStorageList()

  init {
    Logger.d("init: $storageList")
  }

  override suspend fun saveFile(stream: CopyByteArrayStream, metadata: Metadata) {
    storageList.forEach { it.saveFile(stream, metadata) }
  }

  override suspend fun getFiles(folder: String, predicate: (CloudFile) -> Boolean): CloudFiles? {
    return null
  }

  override suspend fun getFile(cloudFile: CloudFile): InputStream? {
    return null
  }

  override suspend fun getFile(fileName: String): InputStream? {
    return null
  }

  override suspend fun deleteFile(fileName: String): Boolean {
    storageList.forEach { it.deleteFile(fileName) }
    return true
  }

  override suspend fun removeAllData(): Boolean {
    storageList.forEach { it.removeAllData() }
    return true
  }

  @Deprecated("Use saveFile() instead")
  override suspend fun backup(stream: CopyByteArrayStream, metadata: Metadata) {
    storageList.forEach { it.backup(stream, metadata) }
  }

  @Deprecated("Use getFile() instead")
  override suspend fun restore(fileName: String): InputStream? {
    storageList.forEach {
      val data = it.restore(fileName)
      if (data != null) return data
    }
    return null
  }

  @Deprecated("Use getFiles() and getFile() instead")
  override suspend fun <T> restoreAll(
    ext: String,
    deleteFile: Boolean,
    convertible: Convertible<T>,
    outputChannel: DataChannel<T>
  ) {
    if (storageList.isEmpty()) {
      return
    }
    Logger.d("restoreAll: start")
    storageList.forEach {
      it.restoreAll(ext, deleteFile, convertible, outputChannel)
    }
  }

  @Deprecated("Use deleteFile() instead")
  override suspend fun delete(fileName: String) {
    storageList.forEach { it.delete(fileName) }
  }
}
