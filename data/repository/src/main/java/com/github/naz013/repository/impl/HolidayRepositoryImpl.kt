package com.github.naz013.repository.impl

import com.github.naz013.domain.PublicHoliday
import com.github.naz013.repository.HolidayRepository
import com.github.naz013.repository.dao.HolidayDao
import com.github.naz013.repository.entity.HolidayEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table
import org.threeten.bp.LocalDate

internal class HolidayRepositoryImpl(
  private val holidayDao: HolidayDao,
  private val tableChangeNotifier: TableChangeNotifier
) : HolidayRepository {

  override suspend fun getByDateRange(
    countryCode: String,
    startDate: LocalDate,
    endDate: LocalDate
  ): List<PublicHoliday> {
    return holidayDao.getByDateRange(countryCode, startDate.toEpochDay(), endDate.toEpochDay())
      .map { it.toDomain() }
  }

  override suspend fun getByDate(countryCode: String, date: LocalDate): PublicHoliday? {
    return holidayDao.getByDate(countryCode, date.toEpochDay())?.toDomain()
  }

  override suspend fun replaceForYear(countryCode: String, year: Int, holidays: List<PublicHoliday>) {
    holidayDao.deleteForCountryAndYear(countryCode, year)
    holidayDao.insertAll(holidays.map { HolidayEntity(it) })
    tableChangeNotifier.notify(Table.Holiday)
  }

  override suspend fun deleteForCountry(countryCode: String) {
    holidayDao.deleteForCountry(countryCode)
    tableChangeNotifier.notify(Table.Holiday)
  }

  override suspend fun deleteAll() {
    holidayDao.deleteAll()
    tableChangeNotifier.notify(Table.Holiday)
  }
}
