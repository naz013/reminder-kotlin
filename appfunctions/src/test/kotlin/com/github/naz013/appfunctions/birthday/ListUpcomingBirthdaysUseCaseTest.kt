package com.github.naz013.appfunctions.birthday

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.datecalc.BirthdayDateCalculator
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.usecase.birthdays.GetAllBirthdaysUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

class ListUpcomingBirthdaysUseCaseTest {

  private val getAllBirthdaysUseCase = mockk<GetAllBirthdaysUseCase>()
  private val birthdayDateCalculator = mockk<BirthdayDateCalculator>()
  private val dateTimeManager = mockk<DateTimeManager>()
  private val useCase = ListUpcomingBirthdaysUseCase(getAllBirthdaysUseCase, birthdayDateCalculator, dateTimeManager)

  private val now = LocalDateTime.of(2026, 8, 1, 0, 0)
  private val birthdayTime = LocalTime.of(9, 0)

  @Test
  fun `invoke returns only birthdays occurring within withinDays, soonest first`() = runTest {
    val soon = birthday(uuId = "soon", date = "1990-08-05")
    val far = birthday(uuId = "far", date = "1990-09-15")
    coEvery { getAllBirthdaysUseCase() } returns listOf(far, soon)
    every { dateTimeManager.getCurrentDateTime() } returns now
    every { dateTimeManager.getBirthdayLocalTime() } returns birthdayTime
    every { dateTimeManager.parseBirthdayDate("1990-08-05") } returns LocalDate.of(1990, 8, 5)
    every { dateTimeManager.parseBirthdayDate("1990-09-15") } returns LocalDate.of(1990, 9, 15)
    every {
      birthdayDateCalculator.getNextOccurrence(LocalDate.of(1990, 8, 5), birthdayTime, false, 0, now)
    } returns LocalDateTime.of(2026, 8, 5, 9, 0)
    every {
      birthdayDateCalculator.getNextOccurrence(LocalDate.of(1990, 9, 15), birthdayTime, false, 0, now)
    } returns LocalDateTime.of(2026, 9, 15, 9, 0)

    val result = useCase(withinDays = 10)

    assertEquals(listOf(soon), result)
  }

  @Test
  fun `invoke skips birthdays whose stored date cannot be parsed`() = runTest {
    val unparsable = birthday(uuId = "bad", date = "not-a-date")
    coEvery { getAllBirthdaysUseCase() } returns listOf(unparsable)
    every { dateTimeManager.getCurrentDateTime() } returns now
    every { dateTimeManager.getBirthdayLocalTime() } returns birthdayTime
    every { dateTimeManager.parseBirthdayDate("not-a-date") } returns null

    val result = useCase(withinDays = 30)

    assertEquals(emptyList<Birthday>(), result)
  }

  private fun birthday(uuId: String, date: String) = Birthday(
    uuId = uuId,
    name = uuId,
    date = date,
    syncState = SyncState.Synced,
  )
}
