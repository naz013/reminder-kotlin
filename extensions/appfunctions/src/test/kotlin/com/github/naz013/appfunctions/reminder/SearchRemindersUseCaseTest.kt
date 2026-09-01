package com.github.naz013.appfunctions.reminder

import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class SearchRemindersUseCaseTest {

  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val useCase = SearchRemindersUseCase(reminderV2Repository)

  @Test
  fun `invoke returns matches from the repository`() = runTest {
    val matches =
      listOf(
        ReminderV2(
          uuId = "reminder-1",
          summary = "Pay rent",
          schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 8, 1, 9, 0)),
        ),
      )
    coEvery { reminderV2Repository.search("rent") } returns matches

    val result = useCase("rent")

    assertEquals(matches, result)
  }
}
