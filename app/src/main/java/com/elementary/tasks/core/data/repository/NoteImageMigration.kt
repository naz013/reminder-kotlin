package com.elementary.tasks.core.data.repository

import com.github.naz013.logging.Logger
import com.github.naz013.repository.NoteRepository
import java.util.UUID

class NoteImageMigration(
  private val noteRepository: NoteRepository,
  private val noteImageRepository: NoteImageRepository
) {

  suspend fun migrate() {
    noteRepository.getImagesIds().forEach {
      runCatching { noteRepository.getImageById(it) }.getOrNull()
        ?.takeIf { it.image != null }
        ?.also { imageFile ->
          Logger.d("migrate image: ${imageFile.noteId}")
          val fileName = imageFile.fileName.takeIf { it.isNotEmpty() }
            ?: UUID.randomUUID().toString()
          imageFile.filePath =
            noteImageRepository.saveBytesToFile(fileName, imageFile.image, imageFile.noteId)
          imageFile.fileName = fileName
          imageFile.image = null
          noteRepository.save(imageFile)
        }
    }
  }
}
