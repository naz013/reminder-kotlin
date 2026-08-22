package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.github.naz013.repository.entity.ImageFileEntity
import com.github.naz013.repository.entity.NoteEntity
import com.github.naz013.repository.entity.NoteWithImagesEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface NotesDao {

  @Transaction
  @Query("SELECT * FROM Note WHERE `key` IN (:ids)")
  fun loadByIds(ids: List<String>): List<NoteWithImagesEntity>

  @Transaction
  @Query(
    """
        SELECT *
        FROM Note
        WHERE archived = :isArchived
        AND (:query = '' OR LOWER(summary) LIKE '%' || :query || '%')
        ORDER BY
          isPinned DESC,
          CASE WHEN :sortOrder = 'date_az' THEN date END ASC,
          CASE WHEN :sortOrder = 'text_az' THEN summary END ASC,
          CASE WHEN :sortOrder = 'text_za' THEN summary END DESC,
          CASE WHEN :sortOrder NOT IN ('date_az', 'text_az', 'text_za') THEN date END DESC
    """
  )
  fun getNotes(isArchived: Boolean, query: String, sortOrder: String): List<NoteWithImagesEntity>

  @Transaction
  @Query(
    """
        SELECT *
        FROM Note
        WHERE archived = :isArchived
        AND (:query = '' OR LOWER(summary) LIKE '%' || :query || '%')
        ORDER BY
          isPinned DESC,
          CASE WHEN :sortOrder = 'date_az' THEN date END ASC,
          CASE WHEN :sortOrder = 'text_az' THEN summary END ASC,
          CASE WHEN :sortOrder = 'text_za' THEN summary END DESC,
          CASE WHEN :sortOrder NOT IN ('date_az', 'text_az', 'text_za') THEN date END DESC
    """
  )
  fun observeNotes(isArchived: Boolean, query: String, sortOrder: String): Flow<List<NoteWithImagesEntity>>

  @Transaction
  @Query(
    """
        SELECT *
        FROM Note
        WHERE LOWER(summary) LIKE '%' || :query || '%'"""
  )
  fun search(query: String): List<NoteEntity>

  @Transaction
  @Query("SELECT * FROM Note WHERE archived=:isArchived")
  fun getAllNotes(isArchived: Boolean = false): List<NoteEntity>

  @Transaction
  @Query(
    """
        SELECT *
        FROM Note
        WHERE LOWER(summary) LIKE '%' || :query || '%'
        AND archived=:isArchived"""
  )
  fun searchByText(query: String, isArchived: Boolean = false): List<NoteWithImagesEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(note: NoteEntity)

  @Query("DELETE FROM Note WHERE `key`=:id")
  fun delete(id: String)

  @Query("DELETE FROM ImageFile WHERE id=:id")
  fun deleteImage(id: Int)

  @Query("DELETE FROM ImageFile WHERE noteId=:id")
  fun deleteImageForNote(id: String)

  @Transaction
  @Query("SELECT * FROM Note WHERE `key`=:id")
  fun getById(id: String): NoteWithImagesEntity?

  @Transaction
  @Query("SELECT * FROM Note WHERE `key`=:id")
  fun observeById(id: String): Flow<NoteWithImagesEntity?>

  @Transaction
  @Query("SELECT * FROM ImageFile WHERE noteId=:id")
  fun getImagesByNoteId(id: String): List<ImageFileEntity>

  @Transaction
  @Query("SELECT id FROM ImageFile")
  fun getImagesIds(): List<Int>

  @Transaction
  @Query("SELECT * FROM ImageFile WHERE id=:id")
  fun getImageById(id: Int): ImageFileEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(imageFile: ImageFileEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(notes: List<ImageFileEntity>)

  @Query("DELETE FROM Note")
  fun deleteAllNotes()

  @Query("DELETE FROM ImageFile")
  fun deleteAllImages()

  @Query("UPDATE Note SET syncState=:state WHERE `key`=:id")
  fun updateSyncState(id: String, state: String)

  @Query("SELECT `key` FROM Note WHERE syncState IN (:syncStates)")
  fun getBySyncStates(syncStates: List<String>): List<String>

  @Query("SELECT `key` FROM Note")
  fun getAllIds(): List<String>

  @Query("SELECT COUNT(*) FROM Note WHERE archived=:isArchived")
  fun countAll(isArchived: Boolean = false): Int
}
