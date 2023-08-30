package com.elementary.tasks.core.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elementary.tasks.core.data.models.RecentQuery

@Dao
interface RecentQueryDao {

  @Query("SELECT * FROM RecentQuery ORDER BY lastUsedAt DESC LIMIT 5")
  fun search(): LiveData<List<RecentQuery>>

  @Query(
    """
        SELECT *
        FROM RecentQuery
        WHERE LOWER(queryText) LIKE '%' || :query || '%'
        ORDER BY lastUsedAt DESC LIMIT 5"""
  )
  fun search(query: String): LiveData<List<RecentQuery>>

  @Query("SELECT * FROM RecentQuery WHERE id=:id")
  fun getById(id: Long): RecentQuery?

  @Query("SELECT * FROM RecentQuery WHERE queryText=:query")
  fun getByQuery(query: String): RecentQuery?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(recentQuery: RecentQuery)

  @Delete
  fun delete(recentQuery: RecentQuery)

  @Query("DELETE FROM RecentQuery")
  fun deleteAll()
}
