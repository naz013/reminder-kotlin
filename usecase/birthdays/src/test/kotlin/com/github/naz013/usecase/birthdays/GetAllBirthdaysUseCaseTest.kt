package com.github.naz013.usecase.birthdays

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.BirthdayRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAllBirthdaysUseCaseTest {

  @Test
  fun `invoke returns every birthday from the repository`() = runTest {
    val repository = mockk<BirthdayRepository>()
    val birthdays = listOf(
      Birthday(uuId = "1", syncState = SyncState.Synced),
      Birthday(uuId = "2", syncState = SyncState.Synced)
    )
    coEvery { repository.getAll() } returns birthdays
    val useCase = GetAllBirthdaysUseCase(repository)

    val result = useCase()

    assertEquals(birthdays, result)
  }
}
