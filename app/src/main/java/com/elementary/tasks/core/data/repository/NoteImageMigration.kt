package com.elementary.tasks.core.data.repository

import com.elementary.tasks.core.data.dao.NotesDao
import timber.log.Timber
import java.util.UUID

class NoteImageMigration(
  private val notesDao: NotesDao,
  private val noteImageRepository: NoteImageRepository
) {

  fun migrate() {
    notesDao.getImagesIds().forEach {
      runCatching { notesDao.getImageById(it) }.getOrNull()
        ?.takeIf { it.image != null }
        ?.also { imageFile ->
          Timber.d("migrate image: ${imageFile.noteId}")
          val fileName = imageFile.fileName.takeIf { it.isNotEmpty() }
            ?: UUID.randomUUID().toString()
          imageFile.filePath =
            noteImageRepository.saveBytesToFile(fileName, imageFile.image, imageFile.noteId)
          imageFile.fileName = fileName
          imageFile.image = null
          notesDao.insert(imageFile)
        }
    }
  }
}
