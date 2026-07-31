package com.github.naz013.appfunctions.reminder

import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ListUpcomingRemindersUseCaseTest {

  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val dateTimeManager = mockk<DateTimeManager>()
  private val useCase = ListUpcomingRemindersUseCase(reminderV2Repository, dateTimeManager)

  private val now = LocalDateTime.of(2026, 8, 1, 0, 0)

  @Test
  fun `invoke queries active reminders from now through withinDays`() = runTest {
    every { dateTimeManager.getCurrentDateTime() } returns now
    every { dateTimeManager.localToUtc(now) } returns now
    coEvery {
      reminderV2Repository.getActiveInRange(removed = false, from = now, to = now.plusDays(7))
    } returns emptyList()

    useCase(withinDays = 7)

    coVerify { reminderV2Repository.getActiveInRange(removed = false, from = now, to = now.plusDays(7)) }
  }

  @Test
  fun `invoke sorts results by event date time, soonest first`() = runTest {
    every { dateTimeManager.getCurrentDateTime() } returns now
    every { dateTimeManager.localToUtc(now) } returns now
    val later = reminderDueAt(now.plusDays(5))
    val sooner = reminderDueAt(now.plusDays(1))
    coEvery { reminderV2Repository.getActiveInRange(removed = false, from = now, to = now.plusDays(7)) } returns
      listOf(later, sooner)

    val result = useCase(withinDays = 7)

    assertEquals(listOf(sooner, later), result)
  }

  private fun reminderDueAt(dateTime: LocalDateTime) = ReminderV2(
    schedule = ReminderSchedule(startDateTime = dateTime, eventDateTime = dateTime)
  )
}
