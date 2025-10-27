package com.github.naz013.cloudapi

import com.github.naz013.cloudapi.legacy.Convertible
import com.github.naz013.cloudapi.legacy.DataChannel
import com.github.naz013.cloudapi.legacy.Metadata
import com.github.naz013.cloudapi.stream.CopyByteArrayStream
import java.io.InputStream
import java.io.OutputStream

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
  suspend fun getFile(fileName: String): InputStream?
  suspend fun getFile(cloudFile: CloudFile): InputStream?
  suspend fun deleteFile(fileName: String): Boolean
  suspend fun removeAllData(): Boolean

  val source: Source

  suspend fun uploadFile(stream: InputStream, cloudFile: CloudFile): CloudFile
  suspend fun findFile(searchParams: CloudFileSearchParams): CloudFile?
  suspend fun downloadFile(cloudFile: CloudFile): InputStream?
}
