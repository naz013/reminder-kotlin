package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.cloudapi.Source
import com.github.naz013.domain.sync.RemoteFileMetadata
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.sync.DataType

internal class FindAllFilesToDownloadUseCase(
  private val cloudApiProvider: CloudApiProvider,
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository,
) {

  suspend operator fun invoke(dataType: DataType): SearchResult? {
    val apiList = cloudApiProvider.getAllowedCloudApis()
    val sources = mutableListOf<CloudFilesWithSource>()
    for (api in apiList) {
      val cloudFiles = findFilesInApiSource(api, dataType)
      if (cloudFiles.isNotEmpty()) {
        sources.add(CloudFilesWithSource(api, cloudFiles))
      }
    }
    return if (sources.isNotEmpty()) {
      SearchResult(sources)
    } else {
      null
    }
  }

  private suspend fun findFilesInApiSource(
    cloudFileApi: CloudFileApi,
    dataType: DataType,
  ): List<CloudFile> {
    val cloudFiles = cloudFileApi.findFiles(dataType.fileExtension)
    val remoteMetadataMap = remoteFileMetadataRepository.getBySource(cloudFileApi.source.name)
      .associateBy { it.name }
    return cloudFiles.filter { cloudFile ->
      val remoteMetadata = remoteMetadataMap[cloudFile.name]
      decideIfCanUseFile(cloudFileApi.source, cloudFile, remoteMetadata)
    }
  }

  private fun decideIfCanUseFile(
    source: Source,
    cloudFile: CloudFile,
    remoteMetadata: RemoteFileMetadata?
  ): Boolean {
    if (remoteMetadata == null) {
      return true
    }
    return when (source) {
      Source.GoogleDrive -> {
        cloudFile.lastModified > remoteMetadata.lastModified || cloudFile.version != remoteMetadata.version
      }
      Source.Dropbox -> {
        cloudFile.rev != remoteMetadata.rev
      }
    }
  }

  data class SearchResult(
    val sources: List<CloudFilesWithSource>
  )

  data class CloudFilesWithSource(
    val source: CloudFileApi,
    val cloudFiles: List<CloudFile>
  )

  companion object {
    private const val TAG = "FindNewestCloudApiSource"
  }
}
