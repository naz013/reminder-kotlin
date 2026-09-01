package com.github.naz013.appfunctions.birthday

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.BirthdayRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate as JavaLocalDate
import org.threeten.bp.LocalDate as ThreeTenLocalDate

class UpdateBirthdayUseCaseTest {

  private val birthdayRepository = mockk<BirthdayRepository>(relaxUnitFun = true)
  private val dateTimeManager = mockk<DateTimeManager>()
  private val useCase = UpdateBirthdayUseCase(birthdayRepository, dateTimeManager)

  @Test
  fun `invoke recomputes derived date fields, bumps version and marks for upload`() = runTest {
    val existing =
      Birthday(
        uuId = "birthday-1",
        name = "Ada",
        date = "1999-10-03",
        day = 3,
        month = 9,
        dayMonth = "3|9",
        syncState = SyncState.Synced,
        version = 2L,
      )
    coEvery { birthdayRepository.getById("birthday-1") } returns existing
    every {
      dateTimeManager.formatBirthdayDate(ThreeTenLocalDate.of(1999, 11, 4))
    } returns "1999-11-04"
    every { dateTimeManager.getNowGmtDateTime() } returns "2026-08-01 00:00:00.000+0000"

    val result =
      useCase(id = "birthday-1", name = "Ada Lovelace", date = JavaLocalDate.of(1999, 11, 4), ignoreYear = true)

    assertEquals("Ada Lovelace", result?.name)
    assertEquals("1999-11-04", result?.date)
    assertEquals(4, result?.day)
    assertEquals(10, result?.month)
    assertEquals("4|10", result?.dayMonth)
    assertEquals(true, result?.ignoreYear)
    assertEquals(3L, result?.version)
    coVerify { birthdayRepository.save(result!!) }
    coVerify { birthdayRepository.updateSyncState("birthday-1", SyncState.WaitingForUpload) }
  }

  @Test
  fun `invoke returns null and does not save when no birthday exists`() = runTest {
    coEvery { birthdayRepository.getById("missing") } returns null

    val result = useCase(id = "missing", name = "Ada", date = JavaLocalDate.of(1999, 10, 3), ignoreYear = false)

    assertNull(result)
    coVerify(exactly = 0) { birthdayRepository.save(any()) }
    coVerify(exactly = 0) { birthdayRepository.updateSyncState(any(), any()) }
  }
}
