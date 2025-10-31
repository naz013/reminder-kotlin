package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.cloudapi.Source
import com.github.naz013.domain.sync.RemoteFileMetadata
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.DataType

internal class FindAllFilesToDownloadUseCase(
  private val getAllowedCloudApisUseCase: GetAllowedCloudApisUseCase,
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository,
) {

  /**
   * Finds all files of a specific data type that need to be downloaded from cloud sources.
   *
   * Compares cloud file metadata with local metadata to determine which files are newer
   * or haven't been downloaded yet. Uses different comparison strategies for different sources
   * (version for Google Drive, rev for Dropbox).
   *
   * @param dataType The type of data to search for
   * @return SearchResult containing cloud files grouped by source, or null if no files to download
   */
  suspend operator fun invoke(dataType: DataType): SearchResult? {
    val apiList = getAllowedCloudApisUseCase()
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
