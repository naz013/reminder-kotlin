package com.github.naz013.feature.note.usecase

import android.content.Context
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.files.DataConverter
import com.github.naz013.files.model.SharedNote
import com.github.naz013.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

internal class CreateSharedNoteFileUseCase(
  private val context: Context,
  private val dataConverter: DataConverter
) {
  suspend operator fun invoke(noteWithImages: NoteWithImages): File? {
    val sharedNote =
      SharedNote(
        text = noteWithImages.note?.summary ?: "",
        id = noteWithImages.note?.key ?: "",
        date = noteWithImages.note?.date ?: "",
        color = noteWithImages.note?.color ?: 0,
        style = noteWithImages.note?.style ?: 0,
        palette = noteWithImages.note?.palette ?: 0,
        updatedAt = noteWithImages.note?.updatedAt,
        opacity = noteWithImages.note?.opacity ?: 100,
        fontSize = noteWithImages.note?.fontSize ?: -1,
      )
    val fileName = "note_${sharedNote.id}${SharedNote.FILE_EXTENSION}"
    return writeToFile(sharedNote, fileName)
  }

  private suspend fun writeToFile(
    sharedNote: SharedNote,
    fileName: String,
  ): File? {
    val cacheDir = context.externalCacheDir ?: context.cacheDir
    val file = File(cacheDir, fileName)
    if (!withContext(Dispatchers.IO) {
        file.createNewFile()
      }
    ) {
      try {
        file.delete()
        withContext(Dispatchers.IO) {
          file.createNewFile()
        }
      } catch (e: Exception) {
        Logger.w(TAG, "Failed to create shared note file: ${e.message}")
      }
    }
    return try {
      val outputStream = withContext(Dispatchers.IO) {
        FileOutputStream(file)
      }
      dataConverter.toOutputStream(sharedNote, outputStream)
      file
    } catch (e: Exception) {
      Logger.w(TAG, "Failed to create shared note file: ${e.message}")
      null
    }
  }

  companion object {
    private const val TAG = "CreateSharedNoteFileUseCase"
  }
}
