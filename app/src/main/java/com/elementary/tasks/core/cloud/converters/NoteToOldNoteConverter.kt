package com.elementary.tasks.core.cloud.converters

import com.elementary.tasks.core.arch.isValid
import com.github.naz013.domain.note.ImageFile
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.note.OldImageFile
import com.github.naz013.domain.note.OldNote
import com.elementary.tasks.core.data.repository.NoteImageRepository
import java.util.UUID

class NoteToOldNoteConverter(
  private val noteImageRepository: NoteImageRepository
) {

  fun toNote(oldNote: OldNote): NoteWithImages? {
    val noteWithImages = NoteWithImages(
      note = Note(oldNote),
      images = oldNote.images.map {
        val fileName = UUID.randomUUID().toString()
        ImageFile(
          fileName = fileName,
          filePath = noteImageRepository.saveBytesToFile(fileName, it.image, oldNote.key),
          noteId = oldNote.key
        )
      }
    )
    return if (noteWithImages.isValid()) {
      noteWithImages
    } else {
      null
    }
  }

  fun toOldNote(noteWithImages: NoteWithImages): OldNote? {
    val note = noteWithImages.note ?: return null
    val images = noteWithImages.images.map {
      OldImageFile(
        image = noteImageRepository.readBytes(it.filePath),
        noteId = it.noteId
      )
    }
    return OldNote(
      images = images,
      uniqueId = note.uniqueId,
      style = note.style,
      color = note.color,
      palette = note.palette,
      date = note.date,
      key = note.key,
      summary = note.summary,
      updatedAt = note.updatedAt,
      fontSize = note.fontSize,
      archived = note.archived
    )
  }
}
