package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.naz013.repository.entity.BirthdayEntity

@Dao
internal interface BirthdaysDao {

  @Query("SELECT * FROM Birthday")
  fun getAll(): List<BirthdayEntity>

  @Query("SELECT * FROM Birthday WHERE LOWER(name) LIKE '%' || :query || '%'")
  fun searchByName(query: String): List<BirthdayEntity>

  @Query("SELECT * FROM Birthday WHERE uuId=:id")
  fun getById(id: String): BirthdayEntity?

  @Query("SELECT * FROM Birthday WHERE dayMonth=:dayMonth")
  fun getAll(dayMonth: String): List<BirthdayEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(birthday: BirthdayEntity)

  @Query("DELETE FROM Birthday WHERE uuId=:id")
  fun delete(id: String)

  @Query("DELETE FROM Birthday")
  fun deleteAll()
}
