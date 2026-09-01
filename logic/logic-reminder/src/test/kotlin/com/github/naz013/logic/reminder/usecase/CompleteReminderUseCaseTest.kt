package com.github.naz013.logic.reminder.usecase

import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.reminder.behavior.BehaviorStrategyResolverV2
import com.github.naz013.logic.reminder.behavior.ReminderBehaviorStrategyV2
import com.github.naz013.scheduler.JobSchedulerApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class CompleteReminderUseCaseTest {
  private val strategyResolver = mockk<BehaviorStrategyResolverV2>()
  private val strategy = mockk<ReminderBehaviorStrategyV2>()
  private val deactivateReminderUseCase = mockk<DeactivateReminderUseCase>(relaxed = true)
  private val dateTimeManager = mockk<DateTimeManager>(relaxed = true)
  private val activateReminderUseCase = mockk<ActivateReminderUseCase>(relaxed = true)
  private val addReminderToHistoryUseCase = mockk<AddReminderToHistoryUseCase>(relaxed = true)
  private val jobScheduler = mockk<JobSchedulerApi>(relaxed = true)

  private lateinit var useCase: CompleteReminderUseCase

  private val reminder = ReminderV2(uuId = "1", schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))

  @Before
  fun setUp() {
    every { strategyResolver.resolve(any()) } returns strategy
    coEvery { deactivateReminderUseCase(any()) } returns reminder

    useCase = CompleteReminderUseCase(
      strategyResolver = strategyResolver,
      deactivateReminderUseCase = deactivateReminderUseCase,
      dateTimeManager = dateTimeManager,
      activateReminderUseCase = activateReminderUseCase,
      addReminderToHistoryUseCase = addReminderToHistoryUseCase,
      jobScheduler = jobScheduler,
    )
  }

  @Test
  fun `invoke cancels any pending repeat or escalation alarm when deactivating`() =
    runTest {
      every { strategy.canSkip(any()) } returns false

      useCase(reminder)

      coVerify(exactly = 1) { jobScheduler.cancelReminder(reminder.uniqueId) }
    }

  @Test
  fun `invoke cancels any pending repeat or escalation alarm when rescheduling the next occurrence`() =
    runTest {
      every { strategy.canSkip(any()) } returns true
      every { strategy.calculateNextOccurrence(any(), any()) } returns LocalDateTime.now().plusDays(1)
      coEvery { activateReminderUseCase(any(), any(), any()) } returns reminder

      useCase(reminder)

      coVerify(exactly = 1) { jobScheduler.cancelReminder(reminder.uniqueId) }
    }
}
