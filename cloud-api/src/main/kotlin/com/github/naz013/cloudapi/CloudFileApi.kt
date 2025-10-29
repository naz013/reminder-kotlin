package com.github.naz013.cloudapi

import java.io.InputStream

interface CloudFileApi {
  val source: Source

  suspend fun deleteFile(fileName: String): Boolean
  suspend fun removeAllData(): Boolean

  suspend fun uploadFile(stream: InputStream, cloudFile: CloudFile): CloudFile
  suspend fun findFile(searchParams: CloudFileSearchParams): CloudFile?
  suspend fun findFiles(fileExtension: String): List<CloudFile>
  suspend fun downloadFile(cloudFile: CloudFile): InputStream?
}
