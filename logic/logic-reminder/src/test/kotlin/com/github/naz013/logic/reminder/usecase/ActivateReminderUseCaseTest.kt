package com.github.naz013.logic.reminder.usecase

import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.TaskExportSettings
import com.github.naz013.logic.reminder.behavior.BehaviorStrategyResolverV2
import com.github.naz013.logic.reminder.behavior.ReminderBehaviorStrategyV2
import com.github.naz013.logic.reminder.usecase.google.SaveReminderToGoogleCalendarUseCase
import com.github.naz013.logic.reminder.usecase.google.SaveReminderToGoogleTasksUseCase
import com.github.naz013.platform.SystemInfo
import com.github.naz013.scheduler.JobSchedulerApi
import com.github.naz013.workapi.WorkScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ActivateReminderUseCaseTest {
  private val strategyResolver = mockk<BehaviorStrategyResolverV2>()
  private val strategy = mockk<ReminderBehaviorStrategyV2>(relaxed = true)
  private val saveReminderUseCase = mockk<SaveReminderUseCase>(relaxed = true)
  private val jobScheduler = mockk<JobSchedulerApi>(relaxed = true)
  private val saveToGoogleTasks = mockk<SaveReminderToGoogleTasksUseCase>(relaxed = true)
  private val resolveChainedStartUseCase = mockk<ResolveChainedStartUseCase>()
  private val pauseReminderUseCase = mockk<PauseReminderUseCase>(relaxed = true)
  private val deactivateReminderUseCase = mockk<DeactivateReminderUseCase>(relaxed = true)

  private val useCase = ActivateReminderUseCase(
    behaviorStrategyResolver = strategyResolver,
    deleteReminderUseCase = mockk(relaxed = true),
    startLocationTrackingUseCase = mockk(relaxed = true),
    saveReminderUseCase = saveReminderUseCase,
    jobScheduler = jobScheduler,
    updatePermanentReminderNotificationUseCase = mockk(relaxed = true),
    saveReminderToGoogleTasksUseCase = saveToGoogleTasks,
    saveReminderToGoogleCalendarUseCase = mockk<SaveReminderToGoogleCalendarUseCase>(relaxed = true),
    workScheduler = mockk<WorkScheduler>(relaxed = true),
    systemInfo = mockk<SystemInfo>(relaxed = true),
    resolveChainedStartUseCase = resolveChainedStartUseCase,
    pauseReminderUseCase = pauseReminderUseCase,
  )

  private fun reminder(after: String?) = ReminderV2(
    uuId = "r",
    isActive = true,
    startAfterReminderId = after,
    taskExport = TaskExportSettings(taskListId = "list"),
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 6, 1, 9, 0)),
  )

  @Test
  fun `parks a waiting reminder with a task export without completing its google task`() = runTest {
    val waiting = reminder(after = "a")
    coEvery { resolveChainedStartUseCase(waiting) } returns waiting.copy(isActive = false)

    val result = useCase(waiting, startAnyway = true)

    coVerify(exactly = 1) { saveReminderUseCase(match { !it.isActive && it.startAfterReminderId == "a" }) }
    coVerify(exactly = 1) { pauseReminderUseCase(any()) }
    coVerify(exactly = 0) { deactivateReminderUseCase(any()) }
    coVerify(exactly = 0) { jobScheduler.scheduleReminder(any()) }
    coVerify(exactly = 0) { saveToGoogleTasks(any()) }
    assert(!result.isActive)
  }

  @Test
  fun `exports to google tasks and schedules once the link is cleared`() = runTest {
    val released = reminder(after = null)
    coEvery { resolveChainedStartUseCase(released) } returns released
    every { strategyResolver.resolve(any()) } returns strategy
    every { strategy.requiresBackgroundService(any()) } returns false
    every { strategy.requiresTimeScheduling(any()) } returns true

    useCase(released, startAnyway = true)

    coVerify(exactly = 1) { jobScheduler.scheduleReminder(any()) }
    coVerify(exactly = 1) { saveToGoogleTasks(any()) }
  }
}
