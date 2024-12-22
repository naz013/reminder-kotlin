package com.github.naz013.cloudapi

import com.github.naz013.cloudapi.legacy.Convertible
import com.github.naz013.cloudapi.legacy.DataChannel
import com.github.naz013.cloudapi.legacy.Metadata
import com.github.naz013.cloudapi.stream.CopyByteArrayStream
import java.io.InputStream

interface CloudFileApi {
  @Deprecated("Use saveFile() instead")
  suspend fun backup(stream: CopyByteArrayStream, metadata: Metadata)

  @Deprecated("Use getFile() instead")
  suspend fun restore(fileName: String): InputStream?

  @Deprecated("Use deleteFile() instead")
  suspend fun delete(fileName: String)

  @Deprecated("Use getFiles() and getFile() instead")
  suspend fun <T> restoreAll(
    ext: String,
    deleteFile: Boolean,
    convertible: Convertible<T>,
    outputChannel: DataChannel<T>
  )

  suspend fun saveFile(stream: CopyByteArrayStream, metadata: Metadata)
  suspend fun getFiles(folder: String, predicate: (CloudFile) -> Boolean = { true }): CloudFiles?
  suspend fun getFile(fileName: String): InputStream?
  suspend fun getFile(cloudFile: CloudFile): InputStream?
  suspend fun deleteFile(fileName: String): Boolean
  suspend fun removeAllData(): Boolean
}
