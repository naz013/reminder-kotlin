package com.elementary.tasks.core.cloud.storages

import com.elementary.tasks.core.cloud.converters.Convertible
import com.elementary.tasks.core.cloud.converters.Metadata
import com.elementary.tasks.core.utils.io.CopyByteArrayStream
import java.io.InputStream

interface Backupable {
  suspend fun backup(stream: CopyByteArrayStream, metadata: Metadata)
  suspend fun restore(fileName: String): InputStream?
  suspend fun delete(fileName: String)
  suspend fun <T> restoreAll(
    ext: String,
    deleteFile: Boolean,
    convertible: Convertible<T>,
    outputChannel: DataChannel<T>
  )
}
