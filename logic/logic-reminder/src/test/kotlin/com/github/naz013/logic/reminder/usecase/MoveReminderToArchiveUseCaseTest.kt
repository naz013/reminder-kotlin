package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.threeten.bp.LocalDateTime

class MoveReminderToArchiveUseCaseTest {
  private val repository = mockk<ReminderV2Repository>()
  private val deactivateReminderUseCase = mockk<DeactivateReminderUseCase>(relaxed = true)
  private val activateChainedRemindersUseCase = mockk<ActivateChainedRemindersUseCase>(relaxed = true)

  private val useCase = MoveReminderToArchiveUseCase(
    repository,
    deactivateReminderUseCase,
    activateChainedRemindersUseCase,
  )

  @Test
  fun `starts reminders waiting for the archived one`() = runTest {
    val reminder = ReminderV2(uuId = "pred", schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))
    coEvery { repository.getById("pred") } returns reminder

    useCase("pred")

    coVerify(exactly = 1) { deactivateReminderUseCase(match { it.isRemoved }) }
    coVerify(exactly = 1) { activateChainedRemindersUseCase("pred") }
  }

  @Test
  fun `does not start anything when the reminder does not exist`() = runTest {
    coEvery { repository.getById("missing") } returns null

    useCase("missing")

    coVerify(exactly = 0) { activateChainedRemindersUseCase(any()) }
  }
}
