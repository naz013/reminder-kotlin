package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.naz013.repository.entity.RecentQueryEntity

@Dao
internal interface RecentQueryDao {

  @Query("SELECT * FROM RecentQuery ORDER BY lastUsedAt DESC LIMIT 5")
  fun getAll(): List<RecentQueryEntity>

  @Query(
    """
        SELECT *
        FROM RecentQuery
        WHERE LOWER(queryText) LIKE '%' || :query || '%'
        ORDER BY lastUsedAt DESC LIMIT 5"""
  )
  fun search(query: String): List<RecentQueryEntity>

  @Query("SELECT * FROM RecentQuery WHERE id=:id")
  fun getById(id: Long): RecentQueryEntity?

  @Query("SELECT * FROM RecentQuery WHERE queryText=:query")
  fun getByQuery(query: String): RecentQueryEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(recentQuery: RecentQueryEntity)

  @Query("DELETE FROM RecentQuery WHERE id=:id")
  fun delete(id: Long)

  @Query("DELETE FROM RecentQuery")
  fun deleteAll()
}
