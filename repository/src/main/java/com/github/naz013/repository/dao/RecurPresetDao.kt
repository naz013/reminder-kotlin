package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.naz013.repository.entity.RecurPresetEntity

@Dao
internal interface RecurPresetDao {

  @Query("SELECT * FROM RecurPreset ORDER BY name ASC, createdAt DESC")
  fun getAll(): List<RecurPresetEntity>

  @Query("SELECT * FROM RecurPreset WHERE type=:presetType ORDER BY name ASC, createdAt DESC")
  fun getAllByType(presetType: Int): List<RecurPresetEntity>

  @Query("SELECT * FROM RecurPreset WHERE id=:id")
  fun getById(id: String): RecurPresetEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(recurPreset: RecurPresetEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertAll(vararg items: RecurPresetEntity)

  @Query("DELETE FROM RecurPreset")
  fun deleteAll()

  @Query("DELETE FROM RecurPreset WHERE id=:id")
  fun deleteById(id: String)

//  @Query("UPDATE RecurPreset SET syncState=:state WHERE id=:id")
//  fun updateSyncState(id: String, state: String)
//
//  @Query("SELECT id FROM RecurPreset WHERE syncState IN (:syncStates)")
//  fun getBySyncStates(syncStates: List<String>): List<String>
//
//  @Query("SELECT id FROM RecurPreset")
//  fun getAllIds(): List<String>
}
