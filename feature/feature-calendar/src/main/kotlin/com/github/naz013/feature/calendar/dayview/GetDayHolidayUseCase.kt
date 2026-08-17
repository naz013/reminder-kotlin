package com.github.naz013.feature.calendar.dayview

import com.github.naz013.domain.PublicHoliday
import com.github.naz013.holidaysapi.HolidaySettingsGate
import com.github.naz013.repository.HolidayRepository
import org.threeten.bp.LocalDate

/** Mirrors [com.github.naz013.feature.calendar.monthview.LoadMonthHolidaysUseCase] for a single day. */
class GetDayHolidayUseCase(
  private val holidayRepository: HolidayRepository,
  private val holidaySettingsGate: HolidaySettingsGate,
) {
  suspend operator fun invoke(date: LocalDate): PublicHoliday? {
    if (!holidaySettingsGate.isEnabled()) return null
    return holidayRepository.getByDate(holidaySettingsGate.countryCode(), date)
  }
}
