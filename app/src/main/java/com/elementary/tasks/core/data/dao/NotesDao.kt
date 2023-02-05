package com.elementary.tasks.core.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.elementary.tasks.core.data.models.ImageFile
import com.elementary.tasks.core.data.models.Note
import com.elementary.tasks.core.data.models.NoteWithImages

@Dao
interface NotesDao {

    @Transaction
    @Query("SELECT * FROM Note")
    fun getAll(): List<NoteWithImages>

    @Transaction
    @Query("SELECT * FROM Note WHERE LOWER(summary) LIKE '%' || :query || '%'")
    fun searchByText(query: String): List<NoteWithImages>

    @Transaction
    @Query("SELECT * FROM Note")
    fun loadAll(): LiveData<List<NoteWithImages>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: Note)

    @Delete
    fun delete(note: Note)

    @Transaction
    @Query("SELECT * FROM Note WHERE `key`=:id")
    fun loadById(id: String): LiveData<NoteWithImages>

    @Transaction
    @Query("SELECT * FROM Note WHERE `key`=:id")
    fun getById(id: String): NoteWithImages?

    @Transaction
    @Query("SELECT * FROM ImageFile WHERE noteId=:id")
    fun getImages(id: String): List<ImageFile>

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
