package com.elementary.tasks.core.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elementary.tasks.core.data.models.Birthday

@Dao
interface BirthdaysDao {

  @Query("SELECT * FROM Birthday WHERE LOWER(name) LIKE '%' || :query || '%'")
  fun search(query: String): LiveData<List<Birthday>>

  @Query("SELECT * FROM Birthday")
  fun getAll(): List<Birthday>

  @Query("SELECT * FROM Birthday WHERE LOWER(name) LIKE '%' || :query || '%'")
  fun searchByName(query: String): List<Birthday>

  @Query("SELECT * FROM Birthday WHERE uuId=:id")
  fun loadById(id: String): LiveData<Birthday>

  @Query("SELECT * FROM Birthday WHERE uuId=:id")
  fun getById(id: String): Birthday?

  @Query("SELECT * FROM Birthday")
  fun loadAll(): LiveData<List<Birthday>>

  @Query("SELECT * FROM Birthday WHERE dayMonth=:dayMonth")
  fun getAll(dayMonth: String): List<Birthday>

  @Query("SELECT * FROM Birthday WHERE dayMonth=:dayMonth")
  fun loadAll(dayMonth: String): LiveData<List<Birthday>>

  @Query("SELECT * FROM Birthday WHERE dayMonth IN (:dayMonths)")
  fun findAll(dayMonths: List<String>): LiveData<List<Birthday>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(birthday: Birthday)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(vararg birthdays: Birthday)

  @Delete
  suspend fun delete(birthday: Birthday)

  @Delete
  suspend fun deleteAll(vararg birthdays: Birthday)

  @Query("DELETE FROM Birthday WHERE uuId=:id")
  fun delete(id: String)

  @Query("DELETE FROM Birthday")
  fun deleteAll()
}
