package com.github.naz013.sync.usecase.download

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.DataPostProcessor
import com.github.naz013.sync.DataType
import com.github.naz013.sync.Downloaded
import com.github.naz013.sync.SyncResult
import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory
import com.github.naz013.sync.usecase.CreateRemoteFileMetadataUseCase
import com.github.naz013.sync.usecase.FindNewestCloudApiSourceUseCase

internal class DownloadSingleUseCase(
  private val dataTypeRepositoryCallerFactory: DataTypeRepositoryCallerFactory,
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository,
  private val createRemoteFileMetadataUseCase: CreateRemoteFileMetadataUseCase,
  private val findNewestCloudApiSourceUseCase: FindNewestCloudApiSourceUseCase,
  private val dataPostProcessor: DataPostProcessor,
  private val downloadCloudFileUseCase: DownloadCloudFileUseCase
) {
  /**
   * Downloads and syncs a single item from the cloud.
   *
   * Finds the newest version of the file across all configured cloud sources,
   * downloads it, and updates the local database. Updates sync state to Synced on success.
   *
   * @param dataType The type of data to download
   * @param id The unique identifier of the item
   * @return SyncResult indicating success or skip status
   * @throws IllegalArgumentException if the id is blank
   * @throws Exception if download or processing fails
   */
  suspend operator fun invoke(dataType: DataType, id: String): SyncResult {
    require(id.isNotBlank()) { "Id cannot be blank" }

    val caller = dataTypeRepositoryCallerFactory.getCaller(dataType)
    val newestResult = findNewestCloudApiSourceUseCase(dataType, id) ?: run {
      Logger.e(TAG, "No cloud file found for dataType: $dataType, id: $id")
      return SyncResult.Skipped
    }
    val cloudFile = newestResult.cloudFile
    val existingMetadata = remoteFileMetadataRepository.getBySource(
      source = newestResult.cloudFileApi.source.value
    ).firstOrNull { it.name == cloudFile.name }
    if (existingMetadata != null) {
      if (cloudFile.lastModified <= existingMetadata.lastModified) {
        Logger.d(TAG, "Local file is up to date for dataType: $dataType, id: $id, skipping download.")
        return SyncResult.Skipped
      }
    }

    val data = downloadCloudFileUseCase(
      cloudFileApi = newestResult.cloudFileApi,
      cloudFile = cloudFile,
      dataType = dataType
    )

    // Check for conflicts before updating
    val existingData = caller.getById(id)
    if (existingData != null) {
      Logger.d(TAG, "Existing data found for id: $id, overwriting with cloud version")
      // Cloud version takes precedence for now
      // Future: Implement proper conflict resolution strategy
    }

    caller.insertOrUpdate(data)
    dataPostProcessor.process(dataType, data)
    val remoteFileMetadata = createRemoteFileMetadataUseCase(
      source = newestResult.cloudFileApi.source.value,
      cloudFile = cloudFile,
      any = data
    )
    remoteFileMetadataRepository.save(remoteFileMetadata)
    caller.updateSyncState(id, SyncState.Synced)

    return SyncResult.Success(
      downloaded = listOf(
        Downloaded(
          dataType = dataType,
          id = id
        )
      ),
      success = true
    )
  }

  companion object {
    private const val TAG = "DownloadSingleUseCase"
  }
}
