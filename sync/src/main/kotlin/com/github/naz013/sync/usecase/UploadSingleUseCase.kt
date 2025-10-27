package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.DataType
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory

internal class UploadSingleUseCase(
  private val cloudFileApi: CloudFileApi,
  private val dataTypeRepositoryCallerFactory: DataTypeRepositoryCallerFactory,
  private val syncDataConverter: SyncDataConverter,
  private val createCloudFileUseCase: CreateCloudFileUseCase,
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository,
  private val createRemoteFileMetadataUseCase: CreateRemoteFileMetadataUseCase,
) {
  suspend operator fun invoke(dataType: DataType, id: String) {
    val caller = dataTypeRepositoryCallerFactory.getCaller(dataType)
    val data = caller.getById(id) ?: run {
      caller.updateSyncState(id, SyncState.FailedToUpload)
      throw IllegalArgumentException("No data found for id: $id")
    }
    try {
      caller.updateSyncState(id, SyncState.Uploading)
      val cloudFile = createCloudFileUseCase(dataType, id)
      val stream = syncDataConverter.create(data)
      val resultFile = cloudFileApi.uploadFile(stream, cloudFile)
      val remoteFileMetadata = createRemoteFileMetadataUseCase(
        source = cloudFileApi.source.value,
        cloudFile = resultFile,
        any = data
      )
      remoteFileMetadataRepository.save(remoteFileMetadata)
      caller.updateSyncState(id, SyncState.Synced)
    } catch (e: Exception) {
      caller.updateSyncState(id, SyncState.FailedToUpload)
      throw e
    }
  }

  companion object {
    private const val TAG = "UploadSingleUseCase"
  }
}
