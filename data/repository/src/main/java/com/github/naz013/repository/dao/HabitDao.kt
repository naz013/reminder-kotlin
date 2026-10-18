package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.github.naz013.repository.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal abstract class HabitDao {

  @Query("SELECT * FROM Habit ORDER BY isPinned DESC, createdAt DESC")
  abstract fun observeAll(): Flow<List<HabitEntity>>

  @Query("SELECT * FROM Habit ORDER BY isPinned DESC, createdAt DESC")
  abstract fun getAll(): List<HabitEntity>

  @Query("SELECT * FROM Habit WHERE id=:id")
  abstract fun getById(id: String): HabitEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract fun insert(habit: HabitEntity)

  @Query("DELETE FROM Habit WHERE id=:id")
  abstract fun delete(id: String)

  @Query("DELETE FROM Habit")
  abstract fun deleteAll()

  @Query("UPDATE Habit SET isPinned=:isPinned WHERE id=:id")
  abstract fun setPinned(id: String, isPinned: Boolean)

  @Query("SELECT id FROM Habit WHERE syncState IN (:syncStates)")
  abstract fun getBySyncStates(syncStates: List<String>): List<String>

  @Query("UPDATE Habit SET syncState=:state WHERE id=:id")
  abstract fun updateSyncState(id: String, state: String)

  @Query("SELECT id FROM Habit")
  abstract fun getAllIds(): List<String>

  @Query("DELETE FROM HabitCheckIn WHERE habitId=:habitId")
  abstract fun deleteCheckInsOf(habitId: String)

  @Query("DELETE FROM HabitCheckIn")
  abstract fun deleteAllCheckIns()

  @Transaction
  open fun deleteWithCheckIns(id: String) {
    deleteCheckInsOf(id)
    delete(id)
  }

  @Transaction
  open fun deleteAllWithCheckIns() {
    deleteAllCheckIns()
    deleteAll()
  }
}
