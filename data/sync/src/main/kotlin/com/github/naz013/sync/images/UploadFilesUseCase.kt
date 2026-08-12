package com.github.naz013.sync.images

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.usecase.CreateRemoteFileMetadataUseCase
import java.io.File
import java.io.InputStream
import java.util.UUID

internal class UploadFilesUseCase(
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository,
  private val createRemoteFileMetadataUseCase: CreateRemoteFileMetadataUseCase,
) {

  suspend operator fun invoke(
    cloudFileApi: CloudFileApi,
    files: List<String>,
    extension: String
  ): List<CachedFile> {
    val files = files.mapNotNull { filePath ->
      val cloudFile = createCloudFile(extension)
      val inputStream = getInputStream(filePath) ?: return@mapNotNull null
      val resultFile = cloudFileApi.uploadFile(inputStream, cloudFile)
      val cachedFile = CachedFile(
        name = cloudFile.name,
        extension = extension,
        size = resultFile.size,
        id = cloudFile.id
      )
      val metadata = createRemoteFileMetadataUseCase(
        source = cloudFileApi.source.name,
        cloudFile = resultFile,
        any = cachedFile
      )
      remoteFileMetadataRepository.save(metadata)
      cachedFile
    }
    return files
  }

  private fun getInputStream(filePath: String): InputStream? {
    val file = File(filePath)
    return if (file.exists()) {
      file.inputStream()
    } else {
      null
    }
  }

  private fun createCloudFile(extension: String): CloudFile {
    val id = UUID.randomUUID().toString()
    return CloudFile(
      id = id,
      name = id + extension,
      size = 0,
      lastModified = 0L,
      fileExtension = extension,
      fileDescription = null,
    )
  }
}
