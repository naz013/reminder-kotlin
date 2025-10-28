package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.DataType
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory

internal class DeleteSingleUseCase(
  private val cloudFileApi: CloudFileApi,
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository,
  private val getCloudFileNameUseCase: GetCloudFileNameUseCase
) {
  suspend operator fun invoke(dataType: DataType, id: String) {
    try {
      val fileName = getCloudFileNameUseCase(dataType, id)
      cloudFileApi.deleteFile(fileName)
      remoteFileMetadataRepository.deleteByLocalUuId(id)
    } catch (e: Exception) {
      throw e
    }
  }
}
