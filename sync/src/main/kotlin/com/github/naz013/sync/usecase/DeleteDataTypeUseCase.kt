package com.github.naz013.sync.usecase

import com.github.naz013.logging.Logger
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.DataType

internal class DeleteDataTypeUseCase(
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository,
  private val findAllFilesToDeleteUseCase: FindAllFilesToDeleteUseCase,
) {
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
    private const val TAG = "DownloadSingleUseCase"
  }
}
