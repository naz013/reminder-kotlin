package com.elementary.tasks.core.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elementary.tasks.core.data.models.GoogleTaskList

@Dao
interface GoogleTaskListsDao {

  @Query("SELECT * FROM GoogleTaskList ORDER BY title")
  fun all(): List<GoogleTaskList>

  @Query("SELECT * FROM GoogleTaskList WHERE def=1")
  fun defaultGoogleTaskList(): GoogleTaskList?

  @Query("SELECT * FROM GoogleTaskList ORDER BY title")
  fun loadAll(): LiveData<List<GoogleTaskList>>

  @Query("SELECT * FROM GoogleTaskList WHERE def=1")
  fun loadDefault(): LiveData<GoogleTaskList>

  @Query("SELECT * FROM GoogleTaskList WHERE def=1")
  fun getDefault(): List<GoogleTaskList>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(googleTaskList: GoogleTaskList)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(vararg googleTaskLists: GoogleTaskList)

  @Delete
  fun delete(googleTaskList: GoogleTaskList)

  @Query("SELECT * FROM GoogleTaskList WHERE listId=:id")
  fun loadById(id: String): LiveData<GoogleTaskList>

  @Query("SELECT * FROM GoogleTaskList WHERE listId=:id")
  fun getById(id: String): GoogleTaskList?

  @Query("DELETE FROM GoogleTaskList")
  fun deleteAll()
}
