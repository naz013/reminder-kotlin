package com.github.naz013.appfunctions.reminder

import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.LocalDateTime

class CompleteReminderUseCaseTest {

  private val reminderV2Repository = mockk<ReminderV2Repository>(relaxUnitFun = true)
  private val useCase = CompleteReminderUseCase(reminderV2Repository)

  @Test
  fun `invoke marks the reminder inactive and saves it`() = runTest {
    val reminder = ReminderV2(
      uuId = "reminder-1",
      isActive = true,
      schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 8, 1, 9, 0))
    )
    coEvery { reminderV2Repository.getById("reminder-1") } returns reminder

    val result = useCase("reminder-1")

    assertEquals(false, result?.isActive)
    coVerify { reminderV2Repository.save(reminder.copy(isActive = false)) }
  }

  @Test
  fun `invoke returns null when no reminder exists with the given id`() = runTest {
    coEvery { reminderV2Repository.getById("missing") } returns null

    val result = useCase("missing")

    assertNull(result)
    coVerify(exactly = 0) { reminderV2Repository.save(any()) }
  }
}
