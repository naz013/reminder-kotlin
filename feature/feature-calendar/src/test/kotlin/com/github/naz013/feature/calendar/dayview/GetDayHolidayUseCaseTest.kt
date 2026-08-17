package com.github.naz013.feature.calendar.dayview

import com.github.naz013.domain.PublicHoliday
import com.github.naz013.holidaysapi.HolidaySettingsGate
import com.github.naz013.repository.HolidayRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.LocalDate

class GetDayHolidayUseCaseTest {
  private val holidayRepository = mockk<HolidayRepository>()
  private val holidaySettingsGate = mockk<HolidaySettingsGate>()
  private val useCase = GetDayHolidayUseCase(holidayRepository, holidaySettingsGate)

  @Test
  fun `returns null without querying the repository when the feature is disabled`() = runTest {
    every { holidaySettingsGate.isEnabled() } returns false

    val result = useCase(LocalDate.of(2026, 7, 4))

    assertNull(result)
    coVerify(exactly = 0) { holidayRepository.getByDate(any(), any()) }
  }

  @Test
  fun `returns the day's holiday when enabled`() = runTest {
    every { holidaySettingsGate.isEnabled() } returns true
    every { holidaySettingsGate.countryCode() } returns "US"
    val date = LocalDate.of(2026, 7, 4)
    val holiday = PublicHoliday(
      id = "US:2026-07-04:Independence Day",
      countryCode = "US",
      date = date,
      name = "Independence Day",
      nameLocal = "Independence Day",
      type = "National",
      location = null,
    )
    coEvery { holidayRepository.getByDate("US", date) } returns holiday

    val result = useCase(date)

    assertEquals(holiday, result)
  }
}
