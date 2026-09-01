package com.github.naz013.appfunctions.reminder

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime as JavaLocalDateTime
import org.threeten.bp.LocalDateTime as ThreeTenLocalDateTime

class UpdateReminderUseCaseTest {

  private val reminderV2Repository = mockk<ReminderV2Repository>(relaxUnitFun = true)
  private val dateTimeManager = mockk<DateTimeManager>()
  private val useCase = UpdateReminderUseCase(reminderV2Repository, dateTimeManager)

  @Test
  fun `invoke updates title, notes and schedule when the reminder exists`() = runTest {
    val existing =
      ReminderV2(
        uuId = "reminder-1",
        summary = "Pay rent",
        schedule = ReminderSchedule(startDateTime = ThreeTenLocalDateTime.of(2026, 8, 1, 9, 0)),
      )
    coEvery { reminderV2Repository.getById("reminder-1") } returns existing
    every {
      dateTimeManager.localToUtc(ThreeTenLocalDateTime.of(2026, 8, 5, 10, 0))
    } returns ThreeTenLocalDateTime.of(2026, 8, 5, 10, 0)

    val result =
      useCase(
        id = "reminder-1",
        title = "Pay rent - updated",
        dueDateTime = JavaLocalDateTime.of(2026, 8, 5, 10, 0),
        notes = "before 5pm",
      )

    assertEquals("Pay rent - updated", result?.summary)
    assertEquals("before 5pm", result?.description)
    assertEquals(ThreeTenLocalDateTime.of(2026, 8, 5, 10, 0), result?.schedule?.startDateTime)
    assertEquals(ThreeTenLocalDateTime.of(2026, 8, 5, 10, 0), result?.schedule?.eventDateTime)
    coVerify { reminderV2Repository.save(result!!) }
  }

  @Test
  fun `invoke returns null and does not save when no reminder exists`() = runTest {
    coEvery { reminderV2Repository.getById("missing") } returns null

    val result =
      useCase(
        id = "missing",
        title = "Title",
        dueDateTime = JavaLocalDateTime.of(2026, 8, 5, 10, 0),
        notes = null,
      )

    assertNull(result)
    coVerify(exactly = 0) { reminderV2Repository.save(any()) }
  }
}
