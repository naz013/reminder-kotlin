package com.github.naz013.digest

import com.github.naz013.domain.Birthday
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.repository.BirthdayRepository
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class DigestContentBuilderTest {
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val birthdayRepository = mockk<BirthdayRepository>()
  private lateinit var builder: DigestContentBuilder

  private val today = LocalDate.of(2026, 3, 15)

  private fun reminder(title: String, time: LocalDateTime) = ReminderV2(
    summary = title,
    schedule = ReminderSchedule(startDateTime = time),
  )

  private fun birthday(name: String) = Birthday(name = name, syncState = SyncState.Synced)

  @Before
  fun setUp() {
    builder = DigestContentBuilder(reminderV2Repository, birthdayRepository)
    coEvery { birthdayRepository.getByDayMonth(any(), any()) } returns emptyList()
  }

  @Test
  fun `buildDaily reads the active range for the given day`() = runTest {
    coEvery {
      reminderV2Repository.getActiveInRange(
        removed = false,
        from = today.atStartOfDay(),
        to = today.plusDays(1).atStartOfDay(),
      )
    } returns emptyList()

    val input = builder.buildDaily(today)

    assertEquals(true, input.isEmpty)
  }

  @Test
  fun `buildDaily passes the day-of-month with a 0-indexed month to the birthday lookup`() = runTest {
    coEvery { reminderV2Repository.getActiveInRange(any(), any(), any()) } returns emptyList()
    coEvery { birthdayRepository.getByDayMonth(day = 15, month = 2) } returns listOf(birthday("Alex"))

    val input = builder.buildDaily(today)

    assertEquals(listOf("Alex"), input.birthdays)
  }

  @Test
  fun `buildDaily sorts reminders by time`() = runTest {
    val late = reminder("Call dentist", today.atTime(14, 0))
    val early = reminder("Pay rent", today.atTime(9, 0))
    coEvery { reminderV2Repository.getActiveInRange(any(), any(), any()) } returns listOf(late, early)

    val input = builder.buildDaily(today)

    assertEquals(listOf("Pay rent", "Call dentist"), input.reminders.map { it.title })
  }

  @Test
  fun `buildDaily caps at 15 reminders and reports the rest as overflow`() = runTest {
    val reminders = (0 until 20).map { reminder("Reminder $it", today.atTime(0, it)) }
    coEvery { reminderV2Repository.getActiveInRange(any(), any(), any()) } returns reminders

    val input = builder.buildDaily(today)

    assertEquals(15, input.reminders.size)
    assertEquals(5, input.overflowCount)
  }
}
