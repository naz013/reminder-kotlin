package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.naz013.repository.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface TagDao {

  @Query("SELECT * FROM Tag ORDER BY name")
  fun observeAll(): Flow<List<TagEntity>>

  @Query("SELECT * FROM Tag ORDER BY name")
  fun getAll(): List<TagEntity>

  @Query("SELECT * FROM Tag WHERE id=:id")
  fun getById(id: String): TagEntity?

  @Query("SELECT * FROM Tag WHERE id=:id")
  fun observeById(id: String): Flow<TagEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(tag: TagEntity)

  @Query("DELETE FROM Tag WHERE id=:id")
  fun delete(id: String)

  @Query("DELETE FROM Tag")
  fun deleteAll()

  @Query("SELECT id FROM Tag WHERE syncState IN (:syncStates)")
  fun getBySyncStates(syncStates: List<String>): List<String>

  @Query("UPDATE Tag SET syncState=:state WHERE id=:id")
  fun updateSyncState(id: String, state: String)

  @Query("SELECT id FROM Tag")
  fun getAllIds(): List<String>
}
