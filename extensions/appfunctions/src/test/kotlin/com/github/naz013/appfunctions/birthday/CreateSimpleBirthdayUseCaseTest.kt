package com.github.naz013.appfunctions.birthday

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.repository.BirthdayRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate as JavaLocalDate
import org.threeten.bp.LocalDate as ThreeTenLocalDate

class CreateSimpleBirthdayUseCaseTest {

  private val birthdayRepository = mockk<BirthdayRepository>(relaxUnitFun = true)
  private val dateTimeManager = mockk<DateTimeManager>()
  private val useCase = CreateSimpleBirthdayUseCase(birthdayRepository, dateTimeManager)

  @Test
  fun `invoke saves a birthday with a zero-indexed month and computed dayMonth`() = runTest {
    val date = JavaLocalDate.of(1999, 10, 3)
    every { dateTimeManager.formatBirthdayDate(ThreeTenLocalDate.of(1999, 10, 3)) } returns "1999-10-03"
    every { dateTimeManager.getNowGmtDateTime() } returns "2026-08-01 00:00:00.000+0000"

    val result = useCase(name = "Ada", date = date, ignoreYear = false)

    assertEquals("Ada", result.name)
    assertEquals("1999-10-03", result.date)
    assertEquals(3, result.day)
    assertEquals(9, result.month)
    assertEquals("3|9", result.dayMonth)
    assertEquals(false, result.ignoreYear)
    coVerify { birthdayRepository.save(result) }
  }

  @Test
  fun `invoke forwards ignoreYear to the saved birthday`() = runTest {
    val date = JavaLocalDate.of(1999, 10, 3)
    every { dateTimeManager.formatBirthdayDate(any()) } returns "1999-10-03"
    every { dateTimeManager.getNowGmtDateTime() } returns "2026-08-01 00:00:00.000+0000"

    val result = useCase(name = "Ada", date = date, ignoreYear = true)

    assertEquals(true, result.ignoreYear)
  }
}
