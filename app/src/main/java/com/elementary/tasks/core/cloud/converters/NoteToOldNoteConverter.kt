package com.elementary.tasks.core.cloud.converters

import com.elementary.tasks.notes.SharedNote
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState

class NoteToOldNoteConverter {
  fun toNote(sharedNote: SharedNote): NoteWithImages? {
    val noteWithImages =
      NoteWithImages(
        note =
          Note(
            style = sharedNote.style,
            color = sharedNote.color,
            palette = sharedNote.palette,
            date = sharedNote.date,
            key = sharedNote.id,
            summary = sharedNote.text,
            title = sharedNote.title,
            titleFontSize = sharedNote.titleFontSize,
            titleFontStyle = sharedNote.titleFontStyle,
            updatedAt = sharedNote.updatedAt,
            fontSize = sharedNote.fontSize,
            archived = false,
            version = 0,
            syncState = SyncState.WaitingForUpload,
          ),
        images = emptyList(),
      )
    return if (noteWithImages.isValid()) {
      noteWithImages
    } else {
      null
    }
  }

  fun toSharedNote(noteWithImages: NoteWithImages): SharedNote? =
    SharedNote(
      text = noteWithImages.note?.summary ?: "",
      title = noteWithImages.note?.title ?: "",
      titleFontSize = noteWithImages.note?.titleFontSize ?: -1,
      titleFontStyle = noteWithImages.note?.titleFontStyle ?: -1,
      id = noteWithImages.note?.key ?: "",
      date = noteWithImages.note?.date ?: "",
      color = noteWithImages.note?.color ?: 0,
      style = noteWithImages.note?.style ?: 0,
      palette = noteWithImages.note?.palette ?: 0,
      updatedAt = noteWithImages.note?.updatedAt,
      opacity = noteWithImages.note?.opacity ?: 100,
      fontSize = noteWithImages.note?.fontSize ?: -1,
    )

  private fun NoteWithImages.isValid(): Boolean {
    val nt = note
    return nt != null && nt.key.isNotEmpty()
  }
}
