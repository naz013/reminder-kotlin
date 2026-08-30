package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.naz013.repository.entity.GoogleTaskListEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface GoogleTaskListsDao {

  @Query("SELECT * FROM GoogleTaskList ORDER BY title")
  fun all(): List<GoogleTaskListEntity>

  @Query("SELECT * FROM GoogleTaskList ORDER BY title")
  fun observeAll(): Flow<List<GoogleTaskListEntity>>

  @Query("SELECT * FROM GoogleTaskList WHERE def=1")
  fun defaultGoogleTaskList(): GoogleTaskListEntity?

  @Query("SELECT * FROM GoogleTaskList WHERE def=1")
  fun getDefault(): List<GoogleTaskListEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(googleTaskList: GoogleTaskListEntity)

  @Query("DELETE FROM GoogleTaskList WHERE listId=:id")
  fun delete(id: String)

  @Query("SELECT * FROM GoogleTaskList WHERE listId=:id")
  fun getById(id: String): GoogleTaskListEntity?

  @Query("SELECT * FROM GoogleTaskList WHERE listId=:id")
  fun observeById(id: String): Flow<GoogleTaskListEntity?>

  @Query("DELETE FROM GoogleTaskList")
  fun deleteAll()
}
