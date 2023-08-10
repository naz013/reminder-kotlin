package com.elementary.tasks.core.cloud.storages

import com.elementary.tasks.core.cloud.converters.Convertible
import com.elementary.tasks.core.cloud.converters.Metadata
import java.io.InputStream

interface Backupable {
  suspend fun backup(fileIndex: FileIndex, metadata: Metadata)
  suspend fun restore(fileName: String): InputStream?
  suspend fun delete(fileName: String)
  suspend fun <T> restoreAll(
    ext: String,
    deleteFile: Boolean,
    convertible: Convertible<T>,
    outputChannel: DataChannel<T>
  )
}
