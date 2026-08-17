package com.github.naz013.feature.calendar.monthview

import com.github.naz013.domain.PublicHoliday
import com.github.naz013.holidaysapi.HolidaySettingsGate
import com.github.naz013.repository.HolidayRepository
import org.threeten.bp.LocalDate

/**
 * Loads the public holiday (if any) for each day of [monthDate]'s month, keyed by date - mirrors
 * [LoadMonthEventsUseCase]'s shape so `CalendarScreen` can join it in at render time the same way
 * it already does for reminder/birthday dots.
 */
class LoadMonthHolidaysUseCase(
  private val holidayRepository: HolidayRepository,
  private val holidaySettingsGate: HolidaySettingsGate,
) {
  suspend operator fun invoke(monthDate: LocalDate): Map<LocalDate, PublicHoliday> {
    if (!holidaySettingsGate.isEnabled()) return emptyMap()

    val startOfMonth = monthDate.withDayOfMonth(1)
    val endOfMonth = monthDate.withDayOfMonth(monthDate.lengthOfMonth())
    val countryCode = holidaySettingsGate.countryCode()

    return holidayRepository
      .getByDateRange(countryCode, startOfMonth, endOfMonth)
      .associateBy { it.date }
  }
}
