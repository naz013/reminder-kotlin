package com.github.naz013.sync.images

import com.github.naz013.domain.note.ImageFile
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.note.OldNote
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.sync.FileCacheProvider
import java.io.File
import java.util.UUID

internal class PostProcessOldNoteUseCase(
  private val fileCacheProvider: FileCacheProvider
) {

  suspend operator fun invoke(
    oldNote: OldNote
  ): NoteWithImages {
    val images = oldNote.images
    if (images.isEmpty()) {
      return NoteWithImages(
        note = createNote(oldNote),
        images = emptyList()
      )
    }
    val downloadedImages = images.mapNotNull { oldImageFile ->
      val byteArray = oldImageFile.image ?: return@mapNotNull null
      val fileName = UUID.randomUUID().toString()
      val cachedPath = cacheByteArray(
        data = byteArray,
        folder = "note_images/${oldNote.key}",
        name = fileName
      )
      ImageFile(
        id = 0,
        noteId = oldNote.key,
        filePath = cachedPath,
        fileName = fileName,
      )
    }
    Logger.i(TAG, "Processed ${downloadedImages.size} images for old note: ${oldNote.key}")
    return NoteWithImages(
      note = createNote(oldNote),
      images = downloadedImages
    )
  }

  private fun cacheByteArray(data: ByteArray, folder: String, name: String): String {
    val rootFolder = File(fileCacheProvider.getRootCacheDir(), folder)
    if (!rootFolder.exists()) {
      rootFolder.mkdirs()
    }
    val file = File(rootFolder, name)
    file.outputStream().use { output ->
      output.write(data)
    }
    Logger.i(TAG, "Cached OLD file at: ${file.absolutePath}")
    return file.absolutePath
  }

  private fun createNote(oldNote: OldNote): Note {
    return Note(
      color = oldNote.color,
      palette = oldNote.palette,
      key = oldNote.key,
      date = oldNote.date,
      style = oldNote.style,
      uniqueId = oldNote.uniqueId,
      summary = oldNote.summary,
      updatedAt = oldNote.updatedAt,
      fontSize = oldNote.fontSize,
      archived = oldNote.archived,
      syncState = SyncState.Synced,
    )
  }

  companion object {
    private const val TAG = "PostProcessOldNoteUseCase"
  }
}
