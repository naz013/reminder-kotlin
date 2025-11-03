package com.elementary.tasks.notes.usecase

import android.content.Context
import android.util.Base64
import android.util.Base64OutputStream
import com.elementary.tasks.notes.SharedNote
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.logging.Logger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonWriter
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class CreateSharedNoteFileUseCase(
  private val context: Context
) {

  suspend operator fun invoke(noteWithImages: NoteWithImages): File? {
    val sharedNote = SharedNote(
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

  private suspend fun writeToFile(sharedNote: SharedNote, fileName: String): File? {
    val cacheDir = context.externalCacheDir ?: context.cacheDir
    val file = File(cacheDir, fileName)
    if (!file.createNewFile()) {
      try {
        file.delete()
        file.createNewFile()
      } catch (e: Exception) {
        Logger.w(TAG, "Failed to create shared note file: ${e.message}")
      }
    }
    return try {
      val outputStream = FileOutputStream(file)
      writeToStream(sharedNote, outputStream)
      outputStream.flush()
      outputStream.close()
      file
    } catch (e: Exception) {
      Logger.w(TAG, "Failed to create shared note file: ${e.message}")
      null
    }
  }

  private suspend fun writeToStream(sharedNote: SharedNote, outputStream: OutputStream) {
    try {
      val output64 = Base64OutputStream(outputStream, Base64.DEFAULT)
      val bufferedWriter = BufferedWriter(OutputStreamWriter(output64, StandardCharsets.UTF_8))
      val writer = JsonWriter(bufferedWriter)
      val type =object : TypeToken<SharedNote>() {}.type
      try {
        Gson().toJson(sharedNote, type, writer)
      } catch (e: Exception) {
        Logger.w(TAG, "Failed to write shared note file: ${e.message}")
      } catch (e: OutOfMemoryError) {
        Logger.w(TAG, "Failed to write shared note file: OutOfMemoryError")
      }
      writer.close()
    } catch (e: Exception) {
      Logger.w(TAG, "Failed to write shared note file: ${e.message}")
    }
  }

  companion object {
    private const val TAG = "CreateSharedNoteFileUseCase"
  }
}
