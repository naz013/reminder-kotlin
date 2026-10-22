package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.chain.ReminderChainValidator
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class FindStartAfterCandidatesUseCaseTest {
  private val repository = mockk<ReminderV2Repository>()
  private val useCase = FindStartAfterCandidatesUseCase(repository, ReminderChainValidator())

  private fun reminder(id: String, active: Boolean = true, after: String? = null) = ReminderV2(
    uuId = id,
    summary = id,
    isActive = active,
    startAfterReminderId = after,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 6, 1, 9, 0)),
  )

  @Test
  fun `excludes the reminder itself and anything that would create a cycle`() = runTest {
    coEvery { repository.getAll(active = true, removed = false) } returns listOf(reminder("a"), reminder("c"))
    coEvery { repository.getAll(active = false, removed = false) } returns
      listOf(reminder("b", active = false, after = "a"), reminder("paused", active = false))

    val result = useCase("a").map { it.uuId }

    assertEquals(listOf("c"), result)
  }

  @Test
  fun `includes waiting reminders so chains can be longer than two and sorts by title`() = runTest {
    coEvery { repository.getAll(active = true, removed = false) } returns listOf(reminder("z"), reminder("m"))
    coEvery { repository.getAll(active = false, removed = false) } returns
      listOf(reminder("w", active = false, after = "z"))

    val result = useCase("new").map { it.uuId }

    assertEquals(listOf("m", "w", "z"), result)
  }
}
