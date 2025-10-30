package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.naz013.repository.entity.PlaceEntity

@Dao
internal interface PlacesDao {

  @Query("SELECT * FROM Place")
  fun getAll(): List<PlaceEntity>

  @Query("SELECT * FROM Place WHERE LOWER(name) LIKE '%' || :query || '%'")
  fun searchByName(query: String): List<PlaceEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(place: PlaceEntity)

  @Query("DELETE FROM Place WHERE id=:id")
  fun delete(id: String)

  @Query("SELECT * FROM Place WHERE id=:id")
  fun getById(id: String): PlaceEntity?

  @Query("DELETE FROM Place")
  fun deleteAll()

  @Query("UPDATE Place SET syncState=:state WHERE id=:id")
  fun updateSyncState(id: String, state: String)

  @Query("SELECT id FROM Place WHERE syncState IN (:syncStates)")
  fun getBySyncStates(syncStates: List<String>): List<String>

  @Query("SELECT id FROM Place")
  fun getAllIds(): List<String>
}
