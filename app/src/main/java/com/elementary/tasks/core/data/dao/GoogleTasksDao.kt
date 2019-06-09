package com.elementary.tasks.core.data.dao

import com.elementary.tasks.core.data.models.GoogleTask
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface GoogleTasksDao {

    @Query("SELECT * FROM GoogleTask ORDER BY status DESC, title ASC")
    fun all(): List<GoogleTask>

    @Query("SELECT * FROM GoogleTask ORDER BY status DESC, title ASC")
    fun loadAll(): LiveData<List<GoogleTask>>

    @Query("SELECT * FROM GoogleTask WHERE listId=:listId ORDER BY status DESC, title ASC")
    fun loadAllByList(listId: String): LiveData<List<GoogleTask>>

    @Query("SELECT * FROM GoogleTask WHERE listId=:listId AND status=:status ORDER BY title ASC")
    fun getAllByList(listId: String, status: String): List<GoogleTask>

    @Insert(onConflict = REPLACE)
    fun insert(googleTask: GoogleTask)

    @Insert(onConflict = REPLACE)
    fun insertAll(googleTasks: List<GoogleTask>)

    @Delete
    fun delete(googleTask: GoogleTask)

    @Query("SELECT * FROM GoogleTask WHERE taskId=:id")
    fun loadById(id: String): LiveData<GoogleTask>

    @Query("SELECT * FROM GoogleTask WHERE taskId=:id")
    fun getById(id: String): GoogleTask?

    @Query("SELECT * FROM GoogleTask WHERE uuId=:id")
    fun getByReminderId(id: String): GoogleTask?

    @Delete
    fun deleteAll(googleTasks: List<GoogleTask>)

    @Query("DELETE FROM GoogleTask")
    fun deleteAll()

    @Query("DELETE FROM GoogleTask WHERE listId=:listId")
    fun deleteAll(listId: String)
}
