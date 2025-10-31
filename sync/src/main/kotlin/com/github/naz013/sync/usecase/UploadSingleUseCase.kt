package com.github.naz013.sync.usecase

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.DataType
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory

internal class UploadSingleUseCase(
  private val dataTypeRepositoryCallerFactory: DataTypeRepositoryCallerFactory,
  private val syncDataConverter: SyncDataConverter,
  private val createCloudFileUseCase: CreateCloudFileUseCase,
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository,
  private val createRemoteFileMetadataUseCase: CreateRemoteFileMetadataUseCase,
  private val getAllowedCloudApisUseCase: GetAllowedCloudApisUseCase
) {
  /**
   * Uploads a single item to all configured cloud sources.
   *
   * Updates sync state through the lifecycle: Uploading -> Synced (on success) or FailedToUpload (on error).
   * If the item is not found locally, sets state to FailedToUpload and throws an exception.
   *
   * @param dataType The type of data to upload
   * @param id The unique identifier of the item
   * @throws IllegalArgumentException if the id is blank or no data found for the id
   * @throws Exception if upload fails
   */
  suspend operator fun invoke(dataType: DataType, id: String) {
    require(id.isNotBlank()) { "Id cannot be blank" }
    val caller = dataTypeRepositoryCallerFactory.getCaller(dataType)
    val data = caller.getById(id) ?: run {
      caller.updateSyncState(id, SyncState.FailedToUpload)
      throw IllegalArgumentException("No data found for id: $id")
    }
    try {
      caller.updateSyncState(id, SyncState.Uploading)
      Logger.d(TAG, "Uploading item with id: $id of type: $dataType, data: $data")
      val cloudFile = createCloudFileUseCase(dataType, data)
      getAllowedCloudApisUseCase().forEach { cloudFileApi ->
        val stream = syncDataConverter.create(data)
        val resultFile = cloudFileApi.uploadFile(stream, cloudFile)
        val remoteFileMetadata = createRemoteFileMetadataUseCase(
          source = cloudFileApi.source.value,
          cloudFile = resultFile,
          any = data
        )
        remoteFileMetadataRepository.save(remoteFileMetadata)
      }
      caller.updateSyncState(id, SyncState.Synced)
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to upload item with id: $id of type: $dataType", e)
      caller.updateSyncState(id, SyncState.FailedToUpload)
      throw e
    }
  }

  companion object {
    private const val TAG = "UploadSingleUseCase"
  }
}
