package com.github.naz013.appfunctions.birthday

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.BirthdayRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchBirthdaysUseCaseTest {

  private val birthdayRepository = mockk<BirthdayRepository>()
  private val useCase = SearchBirthdaysUseCase(birthdayRepository)

  @Test
  fun `invoke returns matches from the repository`() = runTest {
    val matches = listOf(Birthday(uuId = "birthday-1", name = "Ada", syncState = SyncState.Synced))
    coEvery { birthdayRepository.searchByName("ada") } returns matches

    val result = useCase("ada")

    assertEquals(matches, result)
  }
}
