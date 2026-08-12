package com.github.naz013.holidays.work

import com.github.naz013.domain.PublicHoliday
import com.github.naz013.holidays.remote.HolidayFirestoreDataSource
import com.github.naz013.holidaysapi.HolidaySettingsGate
import com.github.naz013.repository.HolidayRepository
import com.github.naz013.workapi.TaskData
import com.github.naz013.workapi.TaskProgressReporter
import com.github.naz013.workapi.TaskResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate

class HolidaySyncTaskTest {
  private val holidaySettingsGate = mockk<HolidaySettingsGate>()
  private val holidayFirestoreDataSource = mockk<HolidayFirestoreDataSource>()
  private val holidayRepository = mockk<HolidayRepository>(relaxed = true)
  private lateinit var task: HolidaySyncTask

  @Before
  fun setUp() {
    task = HolidaySyncTask(holidaySettingsGate, holidayFirestoreDataSource, holidayRepository)
    every { holidaySettingsGate.countryCode() } returns "US"
  }

  @Test
  fun `run short-circuits without touching Firestore when the feature is disabled`() = runTest {
    every { holidaySettingsGate.isEnabled() } returns false

    val result = task.run(TaskData.EMPTY, TaskProgressReporter.NONE)

    assertEquals(TaskResult.Success, result)
    coVerify(exactly = 0) { holidayFirestoreDataSource.fetch(any(), any()) }
  }

  @Test
  fun `run fetches and replaces the current and next year when enabled`() = runTest {
    every { holidaySettingsGate.isEnabled() } returns true
    val currentYear = LocalDate.now().year
    val holidays = listOf(
      PublicHoliday(
        id = PublicHoliday.id("US", LocalDate.of(currentYear, 1, 1), "New Year's Day"),
        countryCode = "US",
        date = LocalDate.of(currentYear, 1, 1),
        name = "New Year's Day",
        nameLocal = "New Year's Day",
        type = "Public",
        location = null,
      )
    )
    coEvery { holidayFirestoreDataSource.fetch("US", currentYear) } returns Result.success(holidays)
    coEvery { holidayFirestoreDataSource.fetch("US", currentYear + 1) } returns Result.success(emptyList())

    val result = task.run(TaskData.EMPTY, TaskProgressReporter.NONE)

    assertEquals(TaskResult.Success, result)
    coVerify { holidayRepository.replaceForYear("US", currentYear, holidays) }
    coVerify { holidayRepository.replaceForYear("US", currentYear + 1, emptyList()) }
  }

  @Test
  fun `run returns Retry when any year fails to fetch`() = runTest {
    every { holidaySettingsGate.isEnabled() } returns true
    val currentYear = LocalDate.now().year
    coEvery { holidayFirestoreDataSource.fetch("US", currentYear) } returns Result.failure(RuntimeException("boom"))
    coEvery { holidayFirestoreDataSource.fetch("US", currentYear + 1) } returns Result.success(emptyList())

    val result = task.run(TaskData.EMPTY, TaskProgressReporter.NONE)

    assertEquals(TaskResult.Retry, result)
  }
}
