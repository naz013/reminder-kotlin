package com.elementary.tasks.core.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.elementary.tasks.core.data.models.ReminderGroup

@Dao
interface ReminderGroupDao {

  @Query("SELECT * FROM ReminderGroup WHERE LOWER(groupTitle) LIKE '%' || :query || '%'")
  fun search(query: String): LiveData<List<ReminderGroup>>

  @Query("SELECT * FROM ReminderGroup WHERE isDefaultGroup=:isDef LIMIT 1")
  fun defaultGroup(isDef: Boolean = true): ReminderGroup?

  @Query("SELECT * FROM ReminderGroup ORDER BY isDefaultGroup DESC")
  fun all(): List<ReminderGroup>

  @Transaction
  @Query("SELECT * FROM ReminderGroup ORDER BY isDefaultGroup DESC")
  fun loadAll(): LiveData<List<ReminderGroup>>

  @Transaction
  @Query("SELECT * FROM ReminderGroup WHERE isDefaultGroup='true' ORDER BY isDefaultGroup LIMIT 1")
  fun loadDefault(): LiveData<ReminderGroup>

  @Transaction
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(reminderGroup: ReminderGroup)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(reminderGroups: List<ReminderGroup>)

  @Delete
  fun delete(reminderGroup: ReminderGroup)

  @Query("SELECT * FROM ReminderGroup WHERE groupUuId=:id")
  fun loadById(id: String): LiveData<ReminderGroup>

  @Query("SELECT * FROM ReminderGroup WHERE groupUuId=:id")
  fun getById(id: String): ReminderGroup?

  @Query("DELETE FROM ReminderGroup")
  fun deleteAll()
}
