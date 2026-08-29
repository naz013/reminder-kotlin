package com.github.naz013.files

import android.content.Context
import android.net.Uri
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.note.combineLegacyNoteColor
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.files.model.SharedNote
import com.github.naz013.logging.Logger

class AndroidDataConverterImpl(
  private val context: Context,
  private val dataConverter: DataConverter,
) : AndroidDataConverter {

  override suspend fun toData(uri: Uri): Any? {
    val inputStream = try {
      context.contentResolver.openInputStream(uri) ?: return null
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to read Intent URI: ${e.message}")
      return null
    }

    return try {
      dataConverter.toData(inputStream).toDomain()
    } catch (e: Exception) {
      Logger.e(TAG, "Failed to parse data from URI: ${e.message}")
      null
    }
  }

  private fun Any.toDomain(): Any {
    return when (this) {
      is SharedNote -> this.toDomain()
      else -> this
    }
  }

  private fun SharedNote.toDomain(): NoteWithImages {
    return NoteWithImages(
      note =
        Note(
          style = this.style,
          color = combineLegacyNoteColor(this.color, this.palette),
          date = this.date,
          key = this.id,
          content = NoteDocument.fromLegacy(title = this.title, summary = this.text),
          updatedAt = this.updatedAt,
          fontSize = this.fontSize,
          archived = false,
          version = 0,
          syncState = SyncState.WaitingForUpload,
        ),
      images = emptyList(),
    )
  }

  companion object {
    private const val TAG = "AndroidDataConverter"
  }
}
