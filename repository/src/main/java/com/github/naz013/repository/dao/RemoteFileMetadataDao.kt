package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.naz013.repository.entity.RemoteFileMetadataEntity

@Dao
internal interface RemoteFileMetadataDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(entity: RemoteFileMetadataEntity)

  @Query("SELECT * FROM RemoteFileMetadata WHERE id=:id")
  fun getById(id: String): RemoteFileMetadataEntity?

  @Query("SELECT * FROM RemoteFileMetadata WHERE localUuId=:localUuId")
  fun getByLocalUuId(localUuId: String): RemoteFileMetadataEntity?

  @Query("SELECT * FROM RemoteFileMetadata WHERE localUuId=:localUuId AND source=:source")
  fun get(localUuId: String, source: String): RemoteFileMetadataEntity?

  @Query("SELECT * FROM RemoteFileMetadata")
  fun getAll(): List<RemoteFileMetadataEntity>

  @Query("DELETE FROM RemoteFileMetadata WHERE id=:id")
  fun delete(id: String)

  @Query("DELETE FROM RemoteFileMetadata")
  fun deleteAll()
}
