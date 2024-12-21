package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.github.naz013.repository.entity.GoogleTaskEntity

@Dao
internal interface GoogleTasksDao {

  @Transaction
  @Query(
    """
        SELECT *
        FROM GoogleTask
        WHERE uuId IS NOT NULL
        AND uuId != ""
        """
  )
  fun getAttachedToReminder(): List<GoogleTaskEntity>

  @Transaction
  @Query(
    """
        SELECT *
        FROM GoogleTask
        WHERE LOWER(title) LIKE '%' || :query || '%'
        OR LOWER(notes) LIKE '%' || :query || '%'
        """
  )
  fun search(query: String): List<GoogleTaskEntity>

  @Query("SELECT * FROM GoogleTask ORDER BY status DESC, title ASC")
  fun all(): List<GoogleTaskEntity>

  @Query("SELECT * FROM GoogleTask WHERE listId=:listId AND status=:status ORDER BY title ASC")
  fun getAllByList(listId: String, status: String): List<GoogleTaskEntity>

  @Query("SELECT * FROM GoogleTask WHERE listId=:listId ORDER BY status DESC, title ASC")
  fun getAllByList(listId: String): List<GoogleTaskEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(googleTask: GoogleTaskEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(googleTasks: List<GoogleTaskEntity>)

  @Query("DELETE FROM GoogleTask WHERE taskId=:id")
  fun delete(id: String)

  @Query("DELETE FROM GoogleTask WHERE taskId IN (:ids)")
  fun deleteAll(ids: List<String>)

  @Query("SELECT * FROM GoogleTask WHERE taskId=:id")
  fun getById(id: String): GoogleTaskEntity?

  @Query("SELECT * FROM GoogleTask WHERE uuId=:id")
  fun getByReminderId(id: String): GoogleTaskEntity?

  @Query("DELETE FROM GoogleTask")
  fun deleteAll()

  @Query("DELETE FROM GoogleTask WHERE listId=:listId")
  fun deleteAll(listId: String)
}
