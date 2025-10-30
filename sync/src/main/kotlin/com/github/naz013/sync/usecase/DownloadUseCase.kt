package com.github.naz013.sync.usecase

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.note.OldNote
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.DataPostProcessor
import com.github.naz013.sync.DataType
import com.github.naz013.sync.Downloaded
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.sync.SyncResult
import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory
import com.github.naz013.sync.settings.SettingsModel

internal class DownloadUseCase(
  private val dataTypeRepositoryCallerFactory: DataTypeRepositoryCallerFactory,
  private val syncDataConverter: SyncDataConverter,
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository,
  private val createRemoteFileMetadataUseCase: CreateRemoteFileMetadataUseCase,
  private val findAllFilesToDownloadUseCase: FindAllFilesToDownloadUseCase,
  private val getLocalUuIdUseCase: GetLocalUuIdUseCase,
  private val dataPostProcessor: DataPostProcessor
) {
  /**
   * Downloads all files of a specific data type from all configured cloud sources.
   *
   * Iterates through all cloud sources and downloads files that are newer than
   * the local version or don't exist locally. Updates sync state to Synced on success.
   *
   * @param dataType The type of data to download
   * @return SyncResult with list of downloaded items or Skipped if nothing to download
   * @throws Exception if download or processing fails for any file
   */
  suspend operator fun invoke(dataType: DataType): SyncResult {
    val caller = dataTypeRepositoryCallerFactory.getCaller(dataType)
    val newestResult = findAllFilesToDownloadUseCase(dataType) ?: run {
      Logger.i(TAG, "No files to download for dataType: $dataType")
      return SyncResult.Skipped
    }
    val downloadedFiles = mutableListOf<Downloaded>()
    for (cloudFilesWithSource in newestResult.sources) {
      val cloudFileApi = cloudFilesWithSource.source
      for (cloudFile in cloudFilesWithSource.cloudFiles) {
        Logger.i(TAG, "Downloading file: ${cloudFile.name} from source: ${cloudFileApi.source}")
        val stream = cloudFileApi.downloadFile(cloudFile) ?: run {
          Logger.e(TAG, "Failed to download file from cloud for dataType: $dataType, file: ${cloudFile.name}")
          continue
        }
        val data = try {
          syncDataConverter.parse(stream, getClass(dataType))
        } catch (e: Exception) {
          Logger.e(TAG, "Failed to parse downloaded file for dataType: $dataType, file: ${cloudFile.name}, error: $e")
          continue
        }
        val id = getLocalUuIdUseCase(data)

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
          source = cloudFileApi.source.value,
          cloudFile = cloudFile,
          any = data
        )
        remoteFileMetadataRepository.save(remoteFileMetadata)
        caller.updateSyncState(id, SyncState.Synced)
        Logger.i(TAG, "Downloaded and saved file: ${cloudFile.name} from source: ${cloudFileApi.source}")
        downloadedFiles.add(Downloaded(dataType, id))
      }
    }
    return if (downloadedFiles.isNotEmpty()) {
      SyncResult.Success(
        downloaded = downloadedFiles,
        success = true
      )
    } else {
      SyncResult.Skipped
    }
  }

  private fun getClass(dataType: DataType): Class<*> {
    return when (dataType) {
      DataType.Reminders -> Reminder::class.java
      DataType.Notes -> OldNote::class.java
      DataType.Birthdays -> Birthday::class.java
      DataType.Groups -> ReminderGroup::class.java
      DataType.Places -> Place::class.java
      DataType.Settings -> SettingsModel::class.java
    }
  }

  companion object {
    private const val TAG = "DownloadUseCase"
  }
}
