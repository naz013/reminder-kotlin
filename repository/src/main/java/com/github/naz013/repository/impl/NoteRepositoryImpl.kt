package com.github.naz013.repository.impl

import com.github.naz013.domain.note.ImageFile
import com.github.naz013.domain.note.Note
import com.github.naz013.domain.note.NoteWithImages
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.logging.Logger
import com.github.naz013.repository.NoteRepository
import com.github.naz013.repository.dao.NotesDao
import com.github.naz013.repository.entity.ImageFileEntity
import com.github.naz013.repository.entity.NoteEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table

internal class NoteRepositoryImpl(
  private val dao: NotesDao,
  private val tableChangeNotifier: TableChangeNotifier
) : NoteRepository {

  override suspend fun save(note: Note) {
    Logger.d(TAG, "Save note: ${note.key}")
    dao.insert(NoteEntity(note))
    tableChangeNotifier.notify(Table.Note)
  }

  override suspend fun save(imageFile: ImageFile) {
    Logger.d(TAG, "Save image: ${imageFile.id}")
    dao.insert(ImageFileEntity(imageFile))
    tableChangeNotifier.notify(Table.ImageFile)
    tableChangeNotifier.notify(Table.Note)
  }

  override suspend fun saveAll(imageFiles: List<ImageFile>) {
    Logger.d(TAG, "Save images: ${imageFiles.size}")
    dao.insertAll(imageFiles.map { ImageFileEntity(it) })
    tableChangeNotifier.notify(Table.ImageFile)
    tableChangeNotifier.notify(Table.Note)
  }

  override suspend fun getById(id: String): NoteWithImages? {
    Logger.d(TAG, "Get note by id: $id")
    return dao.getById(id)?.toDomain()
  }

  override suspend fun getAll(isArchived: Boolean): List<NoteWithImages> {
    Logger.d(TAG, "Get all notes, archived: $isArchived")
    return dao.getAllNotes(isArchived = isArchived).map { addImagesToNote(it) }
  }

  private fun addImagesToNote(note: NoteEntity): NoteWithImages {
    return NoteWithImages(note.toDomain(), dao.getImagesByNoteId(note.key).map { it.toDomain() })
  }

  override suspend fun searchByText(query: String, isArchived: Boolean): List<NoteWithImages> {
    Logger.d(TAG, "Search notes by text: $query, archived: $isArchived")
    return dao.searchByText(query, isArchived = isArchived).map { it.toDomain() }
  }

  override suspend fun getImagesIds(): List<Int> {
    Logger.d(TAG, "Get all images ids")
    return dao.getImagesIds()
  }

  override suspend fun getImageById(id: Int): ImageFile? {
    Logger.d(TAG, "Get image by id: $id")
    return dao.getImageById(id)?.toDomain()
  }

  override suspend fun search(query: String): List<Note> {
    Logger.d(TAG, "Search notes by query: $query")
    return dao.search(query).map { it.toDomain() }
  }

  override suspend fun getByIds(ids: List<String>): List<NoteWithImages> {
    Logger.d(TAG, "Get notes by ids: $ids")
    return dao.loadByIds(ids).map { it.toDomain() }
  }

  override suspend fun getImagesByNoteId(id: String): List<ImageFile> {
    Logger.d(TAG, "Get images by note id: $id")
    return dao.getImagesByNoteId(id).map { it.toDomain() }
  }

  override suspend fun delete(id: String) {
    Logger.d(TAG, "Delete note by id: $id")
    dao.delete(id)
    tableChangeNotifier.notify(Table.Note)
  }

  override suspend fun deleteImage(id: Int) {
    Logger.d(TAG, "Delete image by id: $id")
    dao.deleteImage(id)
    tableChangeNotifier.notify(Table.ImageFile)
  }

  override suspend fun deleteImageForNote(id: String) {
    Logger.d(TAG, "Delete image for note by id: $id")
    dao.deleteImageForNote(id)
    tableChangeNotifier.notify(Table.ImageFile)
  }

  override suspend fun deleteAllNotes() {
    Logger.d(TAG, "Delete all notes")
    dao.deleteAllNotes()
    tableChangeNotifier.notify(Table.Note)
  }

  override suspend fun deleteAllImages() {
    Logger.d(TAG, "Delete all images")
    dao.deleteAllImages()
    tableChangeNotifier.notify(Table.ImageFile)
  }

  override suspend fun getIdsByState(syncStates: List<SyncState>): List<String> {
    Logger.d(TAG, "Get note ids by sync states: $syncStates")
    return dao.getBySyncStates(syncStates.map { it.name })
  }

  override suspend fun updateSyncState(id: String, state: SyncState) {
    Logger.d(TAG, "Update sync state for note id: $id to state: $state")
    dao.updateSyncState(id, state.name)
    tableChangeNotifier.notify(Table.Note)
  }

  companion object {
    private const val TAG = "NoteRepository"
  }
}
