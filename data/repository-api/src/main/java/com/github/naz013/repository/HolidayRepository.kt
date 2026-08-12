package com.github.naz013.repository

import com.github.naz013.domain.PublicHoliday
import org.threeten.bp.LocalDate

interface HolidayRepository {
  suspend fun getByDateRange(countryCode: String, startDate: LocalDate, endDate: LocalDate): List<PublicHoliday>
  suspend fun getByDate(countryCode: String, date: LocalDate): PublicHoliday?

  suspend fun replaceForYear(countryCode: String, year: Int, holidays: List<PublicHoliday>)
  suspend fun deleteForCountry(countryCode: String)
  suspend fun deleteAll()
}
