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

class DeleteReminderUseCaseTest {

  private val reminderV2Repository = mockk<ReminderV2Repository>(relaxUnitFun = true)
  private val useCase = DeleteReminderUseCase(reminderV2Repository)

  @Test
  fun `invoke deletes and returns the reminder when it exists`() = runTest {
    val reminder = ReminderV2(
      uuId = "reminder-1",
      schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 8, 1, 9, 0))
    )
    coEvery { reminderV2Repository.getById("reminder-1") } returns reminder

    val result = useCase("reminder-1")

    assertEquals(reminder, result)
    coVerify { reminderV2Repository.delete("reminder-1") }
  }

  @Test
  fun `invoke returns null and does not delete when no reminder exists`() = runTest {
    coEvery { reminderV2Repository.getById("missing") } returns null

    val result = useCase("missing")

    assertNull(result)
    coVerify(exactly = 0) { reminderV2Repository.delete(any()) }
  }
}
