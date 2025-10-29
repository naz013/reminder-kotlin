package com.github.naz013.sync.usecase

import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.CloudApiProvider
import com.github.naz013.sync.DataType

internal class DeleteSingleUseCase(
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository,
  private val getCloudFileNameUseCase: GetCloudFileNameUseCase,
  private val cloudApiProvider: CloudApiProvider,
) {
  suspend operator fun invoke(dataType: DataType, id: String) {
    if (dataType == DataType.Settings) {
      // Settings are not deleted from cloud.
      return
    }
    try {
      val fileName = getCloudFileNameUseCase(dataType, id)
      cloudApiProvider.getAllowedCloudApis().forEach { cloudFileApi ->
        cloudFileApi.deleteFile(fileName)
      }
      remoteFileMetadataRepository.deleteByLocalUuId(id)
    } catch (e: Exception) {
      throw e
    }
  }
}
