package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.naz013.repository.entity.GroupV2Entity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface GroupV2Dao {

  @Query("SELECT * FROM GroupV2 WHERE LOWER(title) LIKE '%' || :query || '%'")
  fun search(query: String): List<GroupV2Entity>

  @Query("SELECT * FROM GroupV2 WHERE isDefault=:isDef LIMIT 1")
  fun defaultGroup(isDef: Boolean = true): GroupV2Entity?

  @Query("SELECT * FROM GroupV2 ORDER BY isDefault DESC")
  fun all(): List<GroupV2Entity>

  @Query("SELECT * FROM GroupV2 ORDER BY isDefault DESC")
  fun observeAll(): Flow<List<GroupV2Entity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(group: GroupV2Entity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(groups: List<GroupV2Entity>)

  @Query("SELECT * FROM GroupV2 WHERE uuId=:id")
  fun getById(id: String): GroupV2Entity?

  @Query("DELETE FROM GroupV2 WHERE uuId=:id")
  fun delete(id: String)

  @Query("DELETE FROM GroupV2")
  fun deleteAll()

  @Query("UPDATE GroupV2 SET syncState=:state WHERE uuId=:id")
  fun updateSyncState(id: String, state: String)

  @Query("SELECT uuId FROM GroupV2 WHERE syncState IN (:syncStates)")
  fun getBySyncStates(syncStates: List<String>): List<String>

  @Query("SELECT uuId FROM GroupV2")
  fun getAllIds(): List<String>

  @Query("SELECT COUNT(*) FROM GroupV2")
  fun countAll(): Int

  @Query("UPDATE GroupV2 SET isDefault=:isDef WHERE uuId=:id")
  fun setDefaultGroup(id: String, isDef: Boolean = false)
}
