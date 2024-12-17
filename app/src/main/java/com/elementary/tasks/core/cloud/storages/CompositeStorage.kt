package com.elementary.tasks.core.cloud.storages

import com.elementary.tasks.core.cloud.converters.Convertible
import com.elementary.tasks.core.cloud.converters.Metadata
import com.elementary.tasks.core.utils.io.CopyByteArrayStream
import com.github.naz013.logging.Logger
import java.io.InputStream

class CompositeStorage(
  storageManager: StorageManager
) : Storage() {

  private val storageList = storageManager.availableStorageList()

  init {
    Logger.d("init: $storageList")
  }

  override suspend fun backup(stream: CopyByteArrayStream, metadata: Metadata) {
    storageList.forEach { it.backup(stream, metadata) }
  }

  override suspend fun restore(fileName: String): InputStream? {
    storageList.forEach {
      val data = it.restore(fileName)
      if (data != null) return data
    }
    return null
  }

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

  override suspend fun delete(fileName: String) {
    storageList.forEach { it.delete(fileName) }
  }
}
