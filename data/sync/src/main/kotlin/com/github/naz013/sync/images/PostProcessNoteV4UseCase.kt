package com.github.naz013.sync.images

import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteDocument
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.note.combineLegacyNoteColor
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.files.model.NoteV4Json
import com.github.naz013.logging.Logger

internal class PostProcessNoteV4UseCase(
  private val downloadNoteFilesUseCase: DownloadNoteFilesUseCase
) {

  suspend operator fun invoke(
    cloudFileApi: CloudFileApi,
    noteV4Json: NoteV4Json
  ): NoteWithImages {
    val images = noteV4Json.images
    if (images.isEmpty()) {
      return NoteWithImages(
        note = createNote(noteV4Json),
        images = emptyList()
      )
    }
    val downloadedImages = downloadNoteFilesUseCase(cloudFileApi, images, noteV4Json.key)
    Logger.i(TAG, "Downloaded ${downloadedImages.size} images for note: ${noteV4Json.key}")
    return NoteWithImages(
      note = createNote(noteV4Json),
      images = downloadedImages
    )
  }

  private fun createNote(noteV4Json: NoteV4Json): Note {
    return Note(
      color = combineLegacyNoteColor(noteV4Json.color, noteV4Json.palette),
      key = noteV4Json.key,
      date = noteV4Json.date,
      style = noteV4Json.style,
      uniqueId = noteV4Json.uniqueId,
      content = NoteDocument(text = noteV4Json.text, spans = noteV4Json.spans.toNoteTextSpans()),
      updatedAt = noteV4Json.updatedAt,
      fontSize = noteV4Json.fontSize,
      archived = noteV4Json.archived,
      isPinned = noteV4Json.isPinned,
      syncState = SyncState.Synced,
    )
  }

  companion object {
    private const val TAG = "PostProcessNoteV4UseCase"
  }
}
