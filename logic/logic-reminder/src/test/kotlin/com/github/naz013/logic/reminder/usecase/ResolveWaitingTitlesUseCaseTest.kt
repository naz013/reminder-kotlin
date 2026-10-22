package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ResolveWaitingTitlesUseCaseTest {
  private val repository = mockk<ReminderV2Repository>()
  private val useCase = ResolveWaitingTitlesUseCase(repository)

  private fun reminder(id: String, title: String = id, after: String? = null, removed: Boolean = false) =
    ReminderV2(
      uuId = id,
      summary = title,
      isRemoved = removed,
      startAfterReminderId = after,
      schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 6, 1, 9, 0)),
    )

  @Test
  fun `uses the predecessor from the same list without querying the repository`() = runTest {
    val result = useCase(listOf(reminder("a", "Take pills"), reminder("b", after = "a")))

    assertEquals(mapOf("b" to "Take pills"), result)
    coVerify(exactly = 0) { repository.getById(any()) }
  }

  @Test
  fun `loads a predecessor missing from the list from the repository`() = runTest {
    coEvery { repository.getById("a") } returns reminder("a", "Take pills")

    val result = useCase(listOf(reminder("b", after = "a")))

    assertEquals(mapOf("b" to "Take pills"), result)
  }

  @Test
  fun `leaves out reminders whose predecessor is gone or archived`() = runTest {
    coEvery { repository.getById("gone") } returns null
    coEvery { repository.getById("old") } returns reminder("old", removed = true)

    val result = useCase(listOf(reminder("b", after = "gone"), reminder("c", after = "old")))

    assertEquals(emptyMap<String, String>(), result)
  }
}
