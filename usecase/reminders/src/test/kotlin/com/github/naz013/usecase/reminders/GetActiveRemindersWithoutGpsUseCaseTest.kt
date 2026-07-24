package com.github.naz013.usecase.reminders

import com.github.naz013.domain.Reminder
import com.github.naz013.repository.ReminderRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetActiveRemindersWithoutGpsUseCaseTest {

  @Test
  fun `invoke returns the active non-gps reminders from the repository`() = runTest {
    val repository = mockk<ReminderRepository>()
    val reminders = listOf(Reminder(uuId = "1"))
    coEvery { repository.getActiveWithoutGpsTypes() } returns reminders
    val useCase = GetActiveRemindersWithoutGpsUseCase(repository)

    val result = useCase()

    assertEquals(reminders, result)
  }
}
