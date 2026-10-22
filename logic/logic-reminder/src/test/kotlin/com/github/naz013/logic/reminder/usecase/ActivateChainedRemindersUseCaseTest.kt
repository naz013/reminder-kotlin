package com.github.naz013.logic.reminder.usecase

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.behavior.BehaviorStrategyResolverV2
import com.github.naz013.logic.reminder.behavior.ReminderBehaviorStrategyV2
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ActivateChainedRemindersUseCaseTest {
  private val repository = mockk<ReminderV2Repository>()
  private val activateReminderUseCase = mockk<ActivateReminderUseCase>(relaxed = true)
  private val saveReminderUseCase = mockk<SaveReminderUseCase>(relaxed = true)
  private val strategyResolver = mockk<BehaviorStrategyResolverV2>()
  private val strategy = mockk<ReminderBehaviorStrategyV2>()
  private val dateTimeManager = mockk<DateTimeManager>()

  private val useCase = ActivateChainedRemindersUseCase(
    repository,
    activateReminderUseCase,
    saveReminderUseCase,
    strategyResolver,
    dateTimeManager,
  )

  private fun waiting(id: String, removed: Boolean = false) = ReminderV2(
    uuId = id,
    isActive = false,
    isRemoved = removed,
    startAfterReminderId = "pred",
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 6, 1, 9, 0)),
  )

  @Test
  fun `activates every successor and clears the link`() = runTest {
    coEvery { repository.getByStartAfterReminderId("pred") } returns listOf(waiting("s1"), waiting("s2"))
    every { strategyResolver.resolve(any()) } returns strategy
    every { strategy.canSkip(any()) } returns false

    useCase("pred")

    coVerify(exactly = 1) {
      activateReminderUseCase(match { it.uuId == "s1" && it.startAfterReminderId == null }, true, any())
    }
    coVerify(exactly = 1) {
      activateReminderUseCase(match { it.uuId == "s2" && it.startAfterReminderId == null }, true, any())
    }
  }

  @Test
  fun `does nothing when no reminder waits for the predecessor`() = runTest {
    coEvery { repository.getByStartAfterReminderId("pred") } returns emptyList()

    useCase("pred")

    coVerify(exactly = 0) { activateReminderUseCase(any(), any(), any()) }
  }

  @Test
  fun `only unlinks an archived successor without activating it`() = runTest {
    coEvery { repository.getByStartAfterReminderId("pred") } returns listOf(waiting("old", removed = true))

    useCase("pred")

    coVerify(exactly = 1) { saveReminderUseCase(match { it.uuId == "old" && it.startAfterReminderId == null }) }
    coVerify(exactly = 0) { activateReminderUseCase(any(), any(), any()) }
  }

  @Test
  fun `recalculates the next fire time of a stale recurring successor`() = runTest {
    val next = LocalDateTime.of(2026, 6, 2, 9, 0)
    val nextUtc = LocalDateTime.of(2026, 6, 2, 7, 0)
    coEvery { repository.getByStartAfterReminderId("pred") } returns listOf(waiting("rec"))
    every { strategyResolver.resolve(any()) } returns strategy
    every { strategy.canSkip(any()) } returns true
    every { strategy.canStartImmediately(any()) } returns false
    every { dateTimeManager.getCurrentDateTime() } returns LocalDateTime.of(2026, 6, 1, 12, 0)
    every { strategy.calculateNextOccurrence(any(), any()) } returns next
    every { dateTimeManager.localToUtc(next) } returns nextUtc

    useCase("pred")

    coVerify(exactly = 1) { activateReminderUseCase(match { it.schedule.eventDateTime == nextUtc }, true, any()) }
  }
}
