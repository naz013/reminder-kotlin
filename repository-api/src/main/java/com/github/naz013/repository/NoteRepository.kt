package com.github.naz013.repository

import com.github.naz013.domain.note.ImageFile
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages

interface NoteRepository {
  suspend fun save(note: Note)
  suspend fun save(imageFile: ImageFile)
  suspend fun saveAll(imageFiles: List<ImageFile>)

  suspend fun getById(id: String): NoteWithImages?
  suspend fun getAll(isArchived: Boolean = false): List<NoteWithImages>
  suspend fun searchByText(query: String, isArchived: Boolean = false): List<NoteWithImages>
  suspend fun getImagesIds(): List<Int>
  suspend fun getImageById(id: Int): ImageFile?
  suspend fun search(query: String): List<Note>
  suspend fun getByIds(ids: List<String>): List<NoteWithImages>
  suspend fun getImagesByNoteId(id: String): List<ImageFile>

  suspend fun delete(id: String)
  suspend fun deleteImage(id: Int)
  suspend fun deleteImageForNote(id: String)
  suspend fun deleteAllNotes()
  suspend fun deleteAllImages()
}
