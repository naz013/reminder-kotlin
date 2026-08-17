package com.github.naz013.feature.calendar.monthview

import com.github.naz013.domain.PublicHoliday
import com.github.naz013.holidaysapi.HolidaySettingsGate
import com.github.naz013.repository.HolidayRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate

class LoadMonthHolidaysUseCaseTest {
  private val holidayRepository = mockk<HolidayRepository>()
  private val holidaySettingsGate = mockk<HolidaySettingsGate>()
  private val useCase = LoadMonthHolidaysUseCase(holidayRepository, holidaySettingsGate)

  @Test
  fun `returns an empty map without querying the repository when the feature is disabled`() = runTest {
    every { holidaySettingsGate.isEnabled() } returns false

    val result = useCase(LocalDate.of(2026, 7, 1))

    assertEquals(emptyMap<LocalDate, PublicHoliday>(), result)
    coVerify(exactly = 0) { holidayRepository.getByDateRange(any(), any(), any()) }
  }

  @Test
  fun `keys the month's holidays by date when enabled`() = runTest {
    every { holidaySettingsGate.isEnabled() } returns true
    every { holidaySettingsGate.countryCode() } returns "US"
    val holiday = PublicHoliday(
      id = "US:2026-07-04:Independence Day",
      countryCode = "US",
      date = LocalDate.of(2026, 7, 4),
      name = "Independence Day",
      nameLocal = "Independence Day",
      type = "National",
      location = null,
    )
    coEvery {
      holidayRepository.getByDateRange("US", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31))
    } returns listOf(holiday)

    val result = useCase(LocalDate.of(2026, 7, 1))

    assertEquals(mapOf(holiday.date to holiday), result)
  }
}
