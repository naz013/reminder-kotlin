package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.sync.DataType

internal class FindAllFilesToDeleteUseCase(
  private val getAllowedCloudApisUseCase: GetAllowedCloudApisUseCase
) {

  /**
   * Finds all files of a specific data type in all configured cloud sources.
   *
   * Returns all cloud files matching the data type's file extension,
   * without filtering by metadata (all files will be deleted).
   *
   * @param dataType The type of data to search for deletion
   * @return SearchResult containing cloud files grouped by source, or null if no files found
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
