package com.elementary.tasks.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.NoteWithImages

@Dao
interface NotesDao {

    @Transaction
    @Query("SELECT * FROM Note WHERE archived=:isArchived")
    fun getAllNotes(isArchived: Boolean = false): List<Note>

    @Transaction
    @Query("SELECT * FROM Note WHERE LOWER(summary) LIKE '%' || :query || '%' AND archived=:isArchived")
    fun searchByText(query: String, isArchived: Boolean = false): List<NoteWithImages>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: Note)

    @Delete
    fun delete(note: Note)

    @Transaction
    @Query("SELECT * FROM Note WHERE `key`=:id")
    fun getById(id: String): NoteWithImages?

    @Transaction
    @Query("SELECT * FROM ImageFile WHERE noteId=:id")
    fun getImagesByNoteId(id: String): List<ImageFile>

    @Transaction
    @Query("SELECT id FROM ImageFile")
    fun getImagesIds(): List<Int>

    @Transaction
    @Query("SELECT * FROM ImageFile WHERE id=:id")
    fun getImageById(id: Int): ImageFile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(imageFile: ImageFile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(notes: List<ImageFile>)

    @Delete
    fun delete(imageFile: ImageFile)

    @Query("DELETE FROM Note")
    fun deleteAllNotes()

    @Query("DELETE FROM ImageFile")
    fun deleteAllImages()
}
