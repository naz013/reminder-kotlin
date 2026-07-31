package com.github.naz013.appfunctions.reminder

import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime as JavaLocalDateTime
import org.threeten.bp.LocalDateTime as ThreeTenLocalDateTime

class CreateSimpleReminderUseCaseTest {

  private val reminderV2Repository = mockk<ReminderV2Repository>(relaxUnitFun = true)
  private val dateTimeManager = mockk<DateTimeManager>()
  private val useCase = CreateSimpleReminderUseCase(reminderV2Repository, dateTimeManager)

  @Test
  fun `invoke saves a reminder with the title, notes and converted due date`() = runTest {
    val dueDateTime = JavaLocalDateTime.of(2026, 8, 1, 9, 30)
    val localDateTime = ThreeTenLocalDateTime.of(2026, 8, 1, 9, 30)
    val utcDateTime = ThreeTenLocalDateTime.of(2026, 8, 1, 6, 30)
    every { dateTimeManager.localToUtc(localDateTime) } returns utcDateTime

    val result = useCase(title = "Pay rent", dueDateTime = dueDateTime, notes = "before 5pm")

    assertEquals("Pay rent", result.summary)
    assertEquals("before 5pm", result.description)
    assertEquals(utcDateTime, result.schedule.startDateTime)
    assertEquals(utcDateTime, result.schedule.eventDateTime)
    coVerify { reminderV2Repository.save(result) }
  }
}
