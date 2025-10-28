package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.sync.DataType

internal class FindAllFilesToDeleteUseCase(
  private val cloudApiProvider: CloudApiProvider,
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
    return cloudFileApi.findFiles(dataType.fileExtension)
  }

  data class SearchResult(
    val sources: List<CloudFilesWithSource>
  )

  data class CloudFilesWithSource(
    val source: CloudFileApi,
    val cloudFiles: List<CloudFile>
  )
}
