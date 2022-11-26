package com.elementary.tasks.core.cloud.storages

import com.elementary.tasks.core.cloud.converters.Metadata
import kotlinx.coroutines.channels.Channel
import java.io.InputStream

interface Backupable {
  suspend fun backup(fileIndex: FileIndex, metadata: Metadata)
  suspend fun restore(fileName: String): InputStream?
  suspend fun delete(fileName: String)
  fun restoreAll(ext: String, deleteFile: Boolean): Channel<InputStream>
}