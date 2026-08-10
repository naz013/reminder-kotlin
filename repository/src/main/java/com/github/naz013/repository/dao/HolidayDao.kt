package com.github.naz013.repository.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.github.naz013.repository.entity.HolidayEntity

@Dao
internal interface HolidayDao {

  @Query(
    "SELECT * FROM Holiday WHERE countryCode = :countryCode " +
      "AND date BETWEEN :startDate AND :endDate ORDER BY date"
  )
  suspend fun getByDateRange(countryCode: String, startDate: Long, endDate: Long): List<HolidayEntity>

  @Query("SELECT * FROM Holiday WHERE countryCode = :countryCode AND date = :date LIMIT 1")
  suspend fun getByDate(countryCode: String, date: Long): HolidayEntity?

  @Insert(onConflict = REPLACE)
  suspend fun insertAll(holidays: List<HolidayEntity>)

  @Query("DELETE FROM Holiday WHERE countryCode = :countryCode AND year = :year")
  suspend fun deleteForCountryAndYear(countryCode: String, year: Int)

  @Query("DELETE FROM Holiday WHERE countryCode = :countryCode")
  suspend fun deleteForCountry(countryCode: String)

  @Query("DELETE FROM Holiday")
  suspend fun deleteAll()
}
