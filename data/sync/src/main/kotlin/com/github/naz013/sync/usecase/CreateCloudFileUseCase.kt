package com.github.naz013.sync.usecase

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.Place
import com.github.naz013.domain.RecurPreset
import com.github.naz013.domain.Tag
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.files.DataType
import com.github.naz013.files.model.NoteV4Json
import com.github.naz013.files.model.SettingsModel
import com.github.naz013.files.model.TagAssignmentsSnapshotJson
import com.github.naz013.logging.Logger
import com.github.naz013.repository.RemoteFileMetadataRepository

internal class CreateCloudFileUseCase(
  private val getLocalUuIdUseCase: GetLocalUuIdUseCase,
  private val remoteFileMetadataRepository: RemoteFileMetadataRepository
) {
  suspend operator fun invoke(dataType: DataType, any: Any): CloudFile {
    val localUuId = getLocalUuIdUseCase(any)
    val existingMetadata = remoteFileMetadataRepository.getByLocalUuId(localUuId)
    val name = when (any) {
      is ReminderV2 -> any.getFileNamePrefix()
      is NoteWithImages -> any.getFileNamePrefix()
      is Birthday -> any.getFileNamePrefix()
      is GroupV2 -> any.getFileNamePrefix()
      is Place -> any.getFileNamePrefix()
      is SettingsModel -> "app"
      is TagAssignmentsSnapshotJson -> "app"
      is RecurPreset -> any.getFileNamePrefix()
      is Tag -> any.getFileNamePrefix()
      is NoteV4Json -> any.key
      else -> throw IllegalArgumentException("Unsupported data type: ${any::class.java}")
    } + dataType.fileExtension
    Logger.d(TAG, "Created cloud file name: $name for dataType: $dataType")
    return CloudFile(
      id = existingMetadata?.id ?: "",
      name = name,
      size = 0,
      lastModified = 0L,
      fileExtension = dataType.fileExtension,
      fileDescription = null,
    )
  }

  private fun ReminderV2.getFileNamePrefix(): String {
    return uuId
  }

  private fun NoteWithImages.getFileNamePrefix(): String {
    return note?.key ?: throw IllegalArgumentException("Note key is null")
  }

  private fun Birthday.getFileNamePrefix(): String {
    return uuId
  }

  private fun GroupV2.getFileNamePrefix(): String {
    return uuId
  }

  private fun Place.getFileNamePrefix(): String {
    return id
  }

  private fun RecurPreset.getFileNamePrefix(): String {
    return id
  }

  private fun Tag.getFileNamePrefix(): String {
    return id
  }

  companion object {
    private const val TAG = "CreateCloudFileUseCase"
  }
}
