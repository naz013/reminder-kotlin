package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.Reminder
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.note.OldNote
import com.github.naz013.repository.RemoteFileMetadataRepository
import com.github.naz013.sync.DataType

internal class CreateCloudFileUseCase(
  private val getLocalUuIdUseCase: GetLocalUuIdUseCase,
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository
) {
  suspend operator fun invoke(dataType: DataType, any: Any): CloudFile {
    val localUuId = getLocalUuIdUseCase(any)
    val existingMetadata = remoteFileMetadataRepository.getByLocalUuId(localUuId)
    val name = when (any) {
      is Reminder -> any.getFileNamePrefix()
      is OldNote -> any.getFileNamePrefix()
      is Birthday -> any.getFileNamePrefix()
      is ReminderGroup -> any.getFileNamePrefix()
      is Place -> any.getFileNamePrefix()
      else -> throw IllegalArgumentException("Unsupported data type: ${any::class.java}")
    } + dataType.fileExtension
    return CloudFile(
      id = existingMetadata?.id ?: "",
      name = name,
      size = 0,
      lastModified = 0L,
      fileExtension = dataType.fileExtension,
      fileDescription = null,
    )
  }

  private fun Reminder.getFileNamePrefix(): String {
    return uuId
  }

  private fun OldNote.getFileNamePrefix(): String {
    return key
  }

  private fun Birthday.getFileNamePrefix(): String {
    return uuId
  }

  private fun ReminderGroup.getFileNamePrefix(): String {
    return groupUuId
  }

  private fun Place.getFileNamePrefix(): String {
    return id
  }

  companion object {
    private const val TAG = "CreateCloudFileUseCase"
  }
}
