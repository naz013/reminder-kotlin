package com.github.naz013.tags.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.naz013.tags.db.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface TagDao {

  @Query("SELECT * FROM Tag ORDER BY name")
  fun observeAll(): Flow<List<TagEntity>>

  @Query("SELECT * FROM Tag ORDER BY name")
  suspend fun getAll(): List<TagEntity>

  @Query("SELECT * FROM Tag WHERE id=:id")
  suspend fun getById(id: String): TagEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(tag: TagEntity)

  @Query("DELETE FROM Tag WHERE id=:id")
  suspend fun delete(id: String)
}
