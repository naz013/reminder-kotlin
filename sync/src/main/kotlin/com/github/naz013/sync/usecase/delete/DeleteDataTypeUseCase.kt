package com.github.naz013.sync.usecase.delete

import com.github.naz013.logging.Logger
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.DataType
import com.github.naz013.sync.usecase.FindAllFilesToDeleteUseCase

internal class DeleteDataTypeUseCase(
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository,
  private val findAllFilesToDeleteUseCase: FindAllFilesToDeleteUseCase,
) {
  /**
   * Deletes all files of a specific data type from all configured cloud sources.
   *
   * @param dataType The type of data to delete from cloud
   * @throws Exception if deletion fails
   */
  suspend operator fun invoke(dataType: DataType) {
    val newestResult = findAllFilesToDeleteUseCase(dataType) ?: run {
      Logger.i(TAG, "No files to delete for data type: $dataType")
      return
    }
    for (cloudFilesWithSource in newestResult.sources) {
      val cloudFileApi = cloudFilesWithSource.source
      for (cloudFile in cloudFilesWithSource.cloudFiles) {
        cloudFileApi.deleteFile(cloudFile.name)
        remoteFileMetadataRepository.delete(cloudFile.id)
        Logger.d(TAG, "Deleted remote file: ${cloudFile.name} from ${cloudFileApi.source}")
      }
    }
  }

  companion object {
    private const val TAG = "DeleteDataTypeUseCase"
  }
}
