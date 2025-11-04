package com.github.naz013.sync.images

import com.github.naz013.cloudapi.CloudFileApi
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.NoteV3Json
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger

internal class PostProcessNoteV3UseCase(
  private val downloadNoteFilesUseCase: DownloadNoteFilesUseCase
) {

  suspend operator fun invoke(
    cloudFileApi: CloudFileApi,
    noteV3Json: NoteV3Json
  ): NoteWithImages {
    val images = noteV3Json.images
    if (images.isEmpty()) {
      return NoteWithImages(
        note = createNote(noteV3Json),
        images = emptyList()
      )
    }
    val downloadedImages = downloadNoteFilesUseCase(cloudFileApi, images, noteV3Json.key)
    Logger.i(TAG, "Downloaded ${downloadedImages.size} images for note: ${noteV3Json.key}")
    return NoteWithImages(
      note = createNote(noteV3Json),
      images = downloadedImages
    )
  }

  private fun createNote(noteV3Json: NoteV3Json): Note {
    return Note(
      color = noteV3Json.color,
      palette = noteV3Json.palette,
      key = noteV3Json.key,
      date = noteV3Json.date,
      style = noteV3Json.style,
      uniqueId = noteV3Json.uniqueId,
      summary = noteV3Json.summary,
      updatedAt = noteV3Json.updatedAt,
      fontSize = noteV3Json.fontSize,
      archived = noteV3Json.archived,
      syncState = SyncState.Synced,
    )
  }

  companion object {
    private const val TAG = "PostProcessNoteV3UseCase"
  }
}
