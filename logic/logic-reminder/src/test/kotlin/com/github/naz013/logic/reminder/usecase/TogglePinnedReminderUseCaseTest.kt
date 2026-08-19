package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class TogglePinnedReminderUseCaseTest {
  private lateinit var saveReminderUseCase: SaveReminderUseCase

  private lateinit var useCase: TogglePinnedReminderUseCase

  @Before
  fun setUp() {
    saveReminderUseCase = mockk(relaxed = true)
    useCase = TogglePinnedReminderUseCase(saveReminderUseCase)
  }

  @Test
  fun `pins a reminder that was not pinned`() = runTest {
    val reminder = ReminderV2(
      uuId = "id-1",
      summary = "Test",
      schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
      isPinned = false
    )

    val result = useCase(reminder)

    assertTrue(result.isPinned)
    coVerify(exactly = 1) { saveReminderUseCase(match { it.uuId == "id-1" && it.isPinned }) }
  }

  @Test
  fun `unpins a reminder that was pinned`() = runTest {
    val reminder = ReminderV2(
      uuId = "id-2",
      summary = "Test",
      schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
      isPinned = true
    )

    val result = useCase(reminder)

    assertFalse(result.isPinned)
    coVerify(exactly = 1) { saveReminderUseCase(match { it.uuId == "id-2" && !it.isPinned }) }
  }

  @Test
  fun `does not mutate other reminder fields`() = runTest {
    val reminder = ReminderV2(
      uuId = "id-3",
      summary = "Keep me",
      schedule = ReminderSchedule(startDateTime = LocalDateTime.now()),
      isPinned = false
    )

    val result = useCase(reminder)

    assertEquals(reminder.copy(isPinned = true), result)
  }
}
