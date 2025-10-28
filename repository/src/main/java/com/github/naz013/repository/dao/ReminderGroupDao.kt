package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.github.naz013.repository.entity.ReminderGroupEntity

@Dao
internal interface ReminderGroupDao {

  @Query("SELECT * FROM ReminderGroup WHERE LOWER(groupTitle) LIKE '%' || :query || '%'")
  fun search(query: String): List<ReminderGroupEntity>

  @Query("SELECT * FROM ReminderGroup WHERE isDefaultGroup=:isDef LIMIT 1")
  fun defaultGroup(isDef: Boolean = true): ReminderGroupEntity?

  @Query("SELECT * FROM ReminderGroup ORDER BY isDefaultGroup DESC")
  fun all(): List<ReminderGroupEntity>

  @Transaction
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(reminderGroup: ReminderGroupEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(reminderGroups: List<ReminderGroupEntity>)

  @Query("DELETE FROM ReminderGroup WHERE groupUuId=:id")
  fun delete(id: String)

  @Query("SELECT * FROM ReminderGroup WHERE groupUuId=:id")
  fun getById(id: String): ReminderGroupEntity?

  @Query("DELETE FROM ReminderGroup")
  fun deleteAll()

  @Query("UPDATE ReminderGroup SET syncState=:state WHERE groupUuId=:id")
  fun updateSyncState(id: String, state: String)

  @Query("SELECT groupUuId FROM ReminderGroup WHERE syncState IN (:syncStates)")
  fun getBySyncStates(syncStates: List<String>): List<String>

  @Query("SELECT groupUuId FROM ReminderGroup")
  fun getAllIds(): List<String>
}
