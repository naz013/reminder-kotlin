package com.github.naz013.sync.usecase

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.note.OldNote
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.DataType
import com.github.naz013.sync.SyncDataConverter
import com.github.naz013.sync.local.DataTypeRepositoryCallerFactory

internal class DownloadUseCase(
  private val dataTypeRepositoryCallerFactory: DataTypeRepositoryCallerFactory,
  private val syncDataConverter: SyncDataConverter,
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository,
  private val createRemoteFileMetadataUseCase: CreateRemoteFileMetadataUseCase,
  private val findAllFilesToDownloadUseCase: FindAllFilesToDownloadUseCase,
  private val getLocalUuIdUseCase: GetLocalUuIdUseCase
) {
  suspend operator fun invoke(dataType: DataType) {
    val caller = dataTypeRepositoryCallerFactory.getCaller(dataType)
    val newestResult = findAllFilesToDownloadUseCase(dataType) ?: run {
      Logger.i(TAG, "No files to download for dataType: $dataType")
      return
    }
    for (cloudFilesWithSource in newestResult.sources) {
      val cloudFileApi = cloudFilesWithSource.source
      for (cloudFile in cloudFilesWithSource.cloudFiles) {
        Logger.i(TAG, "Downloading file: ${cloudFile.name} from source: ${cloudFileApi.source}")
        val stream = cloudFileApi.downloadFile(cloudFile) ?: run {
          Logger.e(TAG, "Failed to download file from cloud for dataType: $dataType, file: ${cloudFile.name}")
          continue
        }
        val data = syncDataConverter.parse(stream, getClass(dataType))
        caller.insertOrUpdate(data)
        val remoteFileMetadata = createRemoteFileMetadataUseCase(
          source = cloudFileApi.source.value,
          cloudFile = cloudFile,
          any = data
        )
        remoteFileMetadataRepository.save(remoteFileMetadata)
        val id = getLocalUuIdUseCase(data)
        caller.updateSyncState(id, SyncState.Synced)
        Logger.i(TAG, "Downloaded and saved file: ${cloudFile.name} from source: ${cloudFileApi.source}")
      }
    }
  }

  private fun getClass(dataType: DataType): Class<*> {
    return when (dataType) {
      DataType.Reminders -> Reminder::class.java
      DataType.Notes -> OldNote::class.java
      DataType.Birthdays -> Birthday::class.java
      DataType.Groups -> ReminderGroup::class.java
      DataType.Places -> Place::class.java
    }
  }

  companion object {
    private const val TAG = "DownloadSingleUseCase"
  }
}
