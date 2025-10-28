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

internal class DownloadSingleUseCase(
  private val dataTypeRepositoryCallerFactory: DataTypeRepositoryCallerFactory,
  private val syncDataConverter: SyncDataConverter,
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository,
  private val createRemoteFileMetadataUseCase: CreateRemoteFileMetadataUseCase,
  private val findNewestCloudApiSourceUseCase: FindNewestCloudApiSourceUseCase,
) {
  suspend operator fun invoke(dataType: DataType, id: String) {
    val caller = dataTypeRepositoryCallerFactory.getCaller(dataType)
    val newestResult = findNewestCloudApiSourceUseCase(dataType, id) ?: run {
      Logger.e(TAG, "No cloud file found for dataType: $dataType, id: $id")
      return
    }
    try {
      val cloudFile = newestResult.cloudFile
      val stream = newestResult.cloudFileApi.downloadFile(cloudFile) ?: run {
        Logger.e(TAG, "Failed to download file from cloud for dataType: $dataType, id: $id")
        return
      }
      val data = syncDataConverter.parse(stream, getClass(dataType))
      caller.insertOrUpdate(data)
      val remoteFileMetadata = createRemoteFileMetadataUseCase(
        source = newestResult.cloudFileApi.source.value,
        cloudFile = cloudFile,
        any = data
      )
      remoteFileMetadataRepository.save(remoteFileMetadata)
      caller.updateSyncState(id, SyncState.Synced)
    } catch (e: Exception) {
      throw e
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
