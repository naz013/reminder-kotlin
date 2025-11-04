package com.github.naz013.sync.images

import com.github.naz013.cloudapi.CloudFile
import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.domain.note.ImageFile
import com.github.naz013.domain.sync.NoteV3Image
import com.github.naz013.logging.Logger

internal class DownloadNoteFilesUseCase(
  private val cacheFileUseCase: CacheFileUseCase
) {

  suspend operator fun invoke(
    cloudFileApi: CloudFileApi,
    files: List<NoteV3Image>,
    noteId: String
  ): List<ImageFile> {
    Logger.i(TAG, "Downloading ${files.size} files from ${cloudFileApi.source.name}")
    val files = files.mapNotNull { file ->
      val cloudFile = createCloudFile(file)
      val inputStream = cloudFileApi.downloadFile(cloudFile) ?: return@mapNotNull null
      val filePath = cacheFileUseCase(
        inputStream = inputStream,
        folder = "note_images/$noteId",
        name = cloudFile.name
      )
      ImageFile(
        id = 0,
        noteId = noteId,
        filePath = filePath,
        fileName = cloudFile.name,
      )
    }
    return files
  }

  private fun createCloudFile(noteV3Image: NoteV3Image): CloudFile {
    return CloudFile(
      id = noteV3Image.id,
      name = noteV3Image.fileName,
      size = 0,
      lastModified = 0L,
      fileExtension = NoteImageDataType.FILE_EXTENSION,
      fileDescription = null,
    )
  }

  companion object {
    private const val TAG = "DownloadFilesUseCase"
  }
}
