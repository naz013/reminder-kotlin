package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.naz013.repository.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface HabitDao {

  @Query("SELECT * FROM Habit ORDER BY isPinned DESC, createdAt DESC")
  fun observeAll(): Flow<List<HabitEntity>>

  @Query("SELECT * FROM Habit ORDER BY isPinned DESC, createdAt DESC")
  fun getAll(): List<HabitEntity>

  @Query("SELECT * FROM Habit WHERE id=:id")
  fun getById(id: String): HabitEntity?

  @Query("SELECT * FROM Habit WHERE id=:id")
  fun observeById(id: String): Flow<HabitEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(habit: HabitEntity)

  @Query("DELETE FROM Habit WHERE id=:id")
  fun delete(id: String)

  @Query("DELETE FROM Habit")
  fun deleteAll()

  @Query("UPDATE Habit SET isPinned=:isPinned WHERE id=:id")
  fun setPinned(id: String, isPinned: Boolean)

  @Query("SELECT id FROM Habit WHERE syncState IN (:syncStates)")
  fun getBySyncStates(syncStates: List<String>): List<String>

  @Query("UPDATE Habit SET syncState=:state WHERE id=:id")
  fun updateSyncState(id: String, state: String)

  @Query("SELECT id FROM Habit")
  fun getAllIds(): List<String>
}
