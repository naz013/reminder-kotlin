package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.github.naz013.repository.entity.NoteVersionEntity

@Dao
internal interface NoteVersionDao {

  @Insert
  fun insert(version: NoteVersionEntity): Long

  @Query("SELECT * FROM NoteVersion WHERE noteKey=:noteKey ORDER BY savedAt DESC, id DESC")
  fun getByNoteKey(noteKey: String): List<NoteVersionEntity>

  @Query("SELECT * FROM NoteVersion WHERE id=:id")
  fun getById(id: Long): NoteVersionEntity?

  @Query("DELETE FROM NoteVersion WHERE noteKey=:noteKey")
  fun deleteByNoteKey(noteKey: String)

  @Query("DELETE FROM NoteVersion")
  fun deleteAll()

  @Query(
    "DELETE FROM NoteVersion WHERE noteKey=:noteKey AND id NOT IN " +
      "(SELECT id FROM NoteVersion WHERE noteKey=:noteKey ORDER BY savedAt DESC, id DESC LIMIT :keep)"
  )
  fun prune(noteKey: String, keep: Int)

  @Transaction
  fun insertAndPrune(version: NoteVersionEntity, keep: Int): Long {
    val id = insert(version)
    prune(version.noteKey, keep)
    return id
  }
}
