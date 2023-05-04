package com.elementary.tasks.core.data.repository

import com.elementary.tasks.core.data.dao.NotesDao
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.NoteWithImages

class NoteRepository(private val notesDao: NotesDao) {

  fun getAll(): List<NoteWithImages> {
    return notesDao.getAllNotes().map { addImagesToNote(it) }
  }

  private fun addImagesToNote(note: Note): NoteWithImages {
    return NoteWithImages(note, notesDao.getImagesByNoteId(note.key))
  }
}