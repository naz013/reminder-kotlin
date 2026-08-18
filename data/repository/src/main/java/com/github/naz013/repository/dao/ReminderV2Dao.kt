package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.naz013.repository.entity.ReminderV2Entity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ReminderV2Dao {

  @Query("SELECT * FROM ReminderV2 WHERE uuId=:id")
  fun getById(id: String): ReminderV2Entity?

  @Query("SELECT * FROM ReminderV2")
  fun getAll(): List<ReminderV2Entity>

  @Query("SELECT * FROM ReminderV2 WHERE isRemoved=:removed AND isActive=:active")
  fun getAll(active: Boolean, removed: Boolean): List<ReminderV2Entity>

  @Query("SELECT * FROM ReminderV2 WHERE isRemoved=:removed")
  fun getByRemovedStatus(removed: Boolean): List<ReminderV2Entity>

  @Query(
    """SELECT * FROM ReminderV2
        WHERE isRemoved=:removed
        AND isActive=:active
        AND sched_eventDateTime>=:fromMillis
        AND sched_eventDateTime<:toMillis
        ORDER BY isActive DESC, sched_eventDateTime ASC"""
  )
  fun getActiveInRange(
    removed: Boolean,
    active: Boolean,
    fromMillis: Long,
    toMillis: Long
  ): List<ReminderV2Entity>

  @Query(
    """SELECT * FROM ReminderV2
        WHERE isRemoved=:removed
        AND isActive=:active
        AND sched_eventDateTime>=:fromMillis
        AND sched_eventDateTime<:toMillis
        ORDER BY isActive DESC, sched_eventDateTime ASC"""
  )
  fun observeActiveInRange(
    removed: Boolean,
    active: Boolean,
    fromMillis: Long,
    toMillis: Long
  ): Flow<List<ReminderV2Entity>>

  @Query("SELECT * FROM ReminderV2 WHERE groupId=:groupId")
  fun getByGroupId(groupId: String): List<ReminderV2Entity>

  @Query("SELECT COUNT(*) FROM ReminderV2 WHERE groupId=:groupId AND isActive=1 AND isRemoved=0")
  fun countActiveByGroupId(groupId: String): Int

  @Query("UPDATE ReminderV2 SET groupId = NULL WHERE groupId=:groupId")
  fun clearGroupId(groupId: String)

  @Query("SELECT * FROM ReminderV2 WHERE noteId=:noteId")
  fun getByNoteId(noteId: String): List<ReminderV2Entity>

  @Query(
    """SELECT * FROM ReminderV2
        WHERE LOWER(summary) LIKE '%' || :query || '%'
        ORDER BY isActive DESC, sched_eventDateTime ASC"""
  )
  fun search(query: String): List<ReminderV2Entity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(reminder: ReminderV2Entity)

  @Query("DELETE FROM ReminderV2 WHERE uuId=:id")
  fun delete(id: String)

  @Query("DELETE FROM ReminderV2 WHERE uuId IN (:ids)")
  fun deleteAll(ids: List<String>)

  @Query("DELETE FROM ReminderV2")
  fun deleteAll()

  @Query("UPDATE ReminderV2 SET syncState=:state WHERE uuId=:id")
  fun updateSyncState(id: String, state: String)

  @Query("SELECT uuId FROM ReminderV2 WHERE syncState IN (:syncStates)")
  fun getBySyncStates(syncStates: List<String>): List<String>

  @Query("SELECT uuId FROM ReminderV2")
  fun getAllIds(): List<String>
}
