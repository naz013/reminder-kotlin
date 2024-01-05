package com.elementary.tasks.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elementary.tasks.core.data.models.RecurPreset

@Dao
interface RecurPresetDao {

  @Query("SELECT * FROM RecurPreset ORDER BY name ASC, createdAt DESC")
  fun getAll(): List<RecurPreset>

  @Query("SELECT * FROM RecurPreset WHERE type=:presetType ORDER BY name ASC, createdAt DESC")
  fun getAllByType(presetType: Int): List<RecurPreset>

  @Query("SELECT * FROM RecurPreset WHERE id=:id")
  fun getById(id: String): RecurPreset?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(recurPreset: RecurPreset)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(vararg items: RecurPreset)

  @Delete
  fun delete(recurPreset: RecurPreset)

  @Query("DELETE FROM RecurPreset")
  fun deleteAll()

  @Query("DELETE FROM RecurPreset WHERE id=:id")
  fun deleteById(id: String)
}
