package com.github.naz013.usecase.birthdays

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.BirthdayRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetBirthdaysByDayMonthUseCaseTest {

  @Test
  fun `invoke forwards the day and month to the repository`() = runTest {
    val repository = mockk<BirthdayRepository>()
    val birthdays = listOf(Birthday(uuId = "1", syncState = SyncState.Synced))
    coEvery { repository.getByDayMonth(17, 6) } returns birthdays
    val useCase = GetBirthdaysByDayMonthUseCase(repository)

    val result = useCase(day = 17, month = 6)

    assertEquals(birthdays, result)
  }
}
