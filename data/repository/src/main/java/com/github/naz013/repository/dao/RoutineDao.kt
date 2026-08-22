package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.naz013.repository.entity.RoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface RoutineDao {

  @Query("SELECT * FROM Routine ORDER BY isPinned DESC, createdAt DESC")
  fun observeAll(): Flow<List<RoutineEntity>>

  @Query("SELECT * FROM Routine ORDER BY isPinned DESC, createdAt DESC")
  fun getAll(): List<RoutineEntity>

  @Query("SELECT * FROM Routine WHERE id=:id")
  fun getById(id: String): RoutineEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(routine: RoutineEntity)

  @Query("DELETE FROM Routine WHERE id=:id")
  fun delete(id: String)

  @Query("DELETE FROM Routine")
  fun deleteAll()

  @Query("UPDATE Routine SET isPinned=:isPinned WHERE id=:id")
  fun setPinned(id: String, isPinned: Boolean)

  @Query("SELECT id FROM Routine WHERE syncState IN (:syncStates)")
  fun getBySyncStates(syncStates: List<String>): List<String>

  @Query("UPDATE Routine SET syncState=:state WHERE id=:id")
  fun updateSyncState(id: String, state: String)

  @Query("SELECT id FROM Routine")
  fun getAllIds(): List<String>
}
