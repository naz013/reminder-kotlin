package com.github.naz013.usecase.reminders

import com.github.naz013.domain.Reminder
import com.github.naz013.repository.ReminderRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetReminderByIdUseCaseTest {

  @Test
  fun `invoke returns the reminder for the given id`() = runTest {
    val repository = mockk<ReminderRepository>()
    val reminder = Reminder(uuId = "1")
    coEvery { repository.getById("1") } returns reminder
    val useCase = GetReminderByIdUseCase(repository)

    val result = useCase("1")

    assertEquals(reminder, result)
  }

  @Test
  fun `invoke returns null when the repository has no match`() = runTest {
    val repository = mockk<ReminderRepository>()
    coEvery { repository.getById("missing") } returns null
    val useCase = GetReminderByIdUseCase(repository)

    val result = useCase("missing")

    assertNull(result)
  }
}
