package com.github.naz013.appfunctions.birthday

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.BirthdayRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeleteBirthdayUseCaseTest {

  private val birthdayRepository = mockk<BirthdayRepository>(relaxUnitFun = true)
  private val useCase = DeleteBirthdayUseCase(birthdayRepository)

  @Test
  fun `invoke deletes and returns the birthday when it exists`() = runTest {
    val birthday = Birthday(uuId = "birthday-1", name = "Ada", syncState = SyncState.Synced)
    coEvery { birthdayRepository.getById("birthday-1") } returns birthday

    val result = useCase("birthday-1")

    assertEquals(birthday, result)
    coVerify { birthdayRepository.delete("birthday-1") }
  }

  @Test
  fun `invoke returns null and does not delete when no birthday exists`() = runTest {
    coEvery { birthdayRepository.getById("missing") } returns null

    val result = useCase("missing")

    assertNull(result)
    coVerify(exactly = 0) { birthdayRepository.delete(any()) }
  }
}
