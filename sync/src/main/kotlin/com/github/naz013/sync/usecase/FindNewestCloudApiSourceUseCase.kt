package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.cloudapi.CloudFileSearchParams
import com.github.naz013.cloudapi.Source
import com.github.naz013.domain.sync.RemoteFileMetadata
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.sync.DataType

internal class FindNewestCloudApiSourceUseCase(
  private val cloudApiProvider: CloudApiProvider,
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository,
) {

  /**
   * Finds the newest version of a specific file across all configured cloud sources.
   *
   * Searches all cloud APIs for the file and compares lastModified timestamps
   * to determine which source has the most recent version. Only includes files
   * that are newer than the local metadata or don't exist in local metadata.
   *
   * @param dataType The type of data to search for
   * @param id The unique identifier of the item
   * @return SearchResult with the cloud API and file that has the newest version, or null if not found
   */
  suspend operator fun invoke(dataType: DataType, id: String): SearchResult? {
    val apiList = cloudApiProvider.getAllowedCloudApis()
    var newestResult: SearchResult? = null
    for (api in apiList) {
      val cloudFile = findFileInApiSource(api, dataType, id)
      if (cloudFile != null) {
        if (newestResult == null || cloudFile.lastModified > newestResult.cloudFile.lastModified) {
          newestResult = SearchResult(api, cloudFile)
        }
      }
    }
    return newestResult
  }

  private suspend fun findFileInApiSource(
    cloudFileApi: CloudFileApi,
    dataType: DataType,
    id: String
  ): CloudFile? {
    val cloudFile = cloudFileApi.findFile(
      CloudFileSearchParams(
        name = id,
        fileExtension = dataType.fileExtension
      )
    )
    val remoteMetadata = remoteFileMetadataRepository.getByLocalUuIdAndSource(id, cloudFileApi.source.name)
    return cloudFile?.takeIf { decideIfCanUseFile(cloudFileApi.source, it, remoteMetadata) }
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
    val cloudFileApi: CloudFileApi,
    val cloudFile: CloudFile
  )

  companion object {
    private const val TAG = "FindNewestCloudApiSource"
  }
}
