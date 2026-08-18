package com.github.naz013.feature.calendar.timeline

import com.github.naz013.domain.PublicHoliday
import com.github.naz013.holidaysapi.HolidaySettingsGate
import com.github.naz013.repository.HolidayRepository
import org.threeten.bp.LocalDate

/**
 * Loads the public holiday (if any) for each day in the inclusive [[startDate], [endDate]] window,
 * keyed by date - the multi-day sibling of
 * [com.github.naz013.feature.calendar.monthview.LoadMonthHolidaysUseCase].
 */
class GetRangeHolidaysUseCase(
  private val holidayRepository: HolidayRepository,
  private val holidaySettingsGate: HolidaySettingsGate,
) {
  suspend operator fun invoke(
    startDate: LocalDate,
    endDate: LocalDate,
  ): Map<LocalDate, PublicHoliday> {
    if (!holidaySettingsGate.isEnabled()) return emptyMap()

    return holidayRepository
      .getByDateRange(holidaySettingsGate.countryCode(), startDate, endDate)
      .associateBy { it.date }
  }
}
