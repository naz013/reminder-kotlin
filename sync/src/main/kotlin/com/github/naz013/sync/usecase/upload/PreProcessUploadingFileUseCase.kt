package com.github.naz013.sync.usecase.upload

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.NoteV3Image
import com.github.naz013.domain.sync.NoteV3Json
import com.github.naz013.logging.Logger
import com.github.naz013.sync.DataType
import com.github.naz013.sync.images.CachedFile
import com.github.naz013.sync.images.NoteImageDataType
import com.github.naz013.sync.images.UploadFilesUseCase

internal class PreProcessUploadingFileUseCase(
  private val uploadFilesUseCase: UploadFilesUseCase
) {

  suspend operator fun invoke(
    cloudFileApi: CloudFileApi,
    cloudFile: CloudFile,
    dataType: DataType,
    data: Any
  ): Any {
    return when (data) {
      is NoteWithImages -> {
        Logger.i(TAG, "Pre-processing NoteWithImages before upload: ${cloudFile.name}")
        preProcessNote(cloudFileApi, data)
      }
      else -> data
    }
  }

  private suspend fun preProcessNote(
    cloudFileApi: CloudFileApi,
    noteWithImages: NoteWithImages
  ): Any {
    val note = noteWithImages.note ?: throw IllegalArgumentException("Note is null in NoteWithImages")
    val images = noteWithImages.images
    if (images.isEmpty()) {
      Logger.d(TAG, "No images to upload for note: ${note.key}")
      return createNoteV3Json(note, emptyList())
    }
    val uploadedImages = uploadFilesUseCase(
      cloudFileApi = cloudFileApi,
      files = images.map { it.filePath },
      extension = NoteImageDataType.FILE_EXTENSION
    )
    Logger.d(TAG, "Uploaded ${uploadedImages.size} images for note: ${note.key}")
    return createNoteV3Json(note, uploadedImages)
  }

  private fun createNoteV3Json(note: Note, images: List<CachedFile>): NoteV3Json {
    return NoteV3Json(
      key = note.key,
      summary = note.summary,
      color = note.color,
      archived = note.archived,
      date = note.date,
      fontSize = note.fontSize,
      palette = note.palette,
      style = note.style,
      uniqueId = note.uniqueId,
      updatedAt = note.updatedAt,
      version = note.version,
      images = images.map {
        NoteV3Image(
          id = it.id,
          fileName = it.name,
          size = it.size
        )
      }
    )
  }

  companion object {
    private const val TAG = "PreProcessUploadingFileUseCase"
  }
}
