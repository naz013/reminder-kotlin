package com.github.naz013.logic.notificationaction.reminder

import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.LockScreenVisibility
import com.github.naz013.domain.reminder.v2.NotificationSettings
import com.github.naz013.domain.reminder.v2.ReminderNotificationCategory
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.logic.notificationaction.ActionHandler
import com.github.naz013.logic.notificationaction.DoNotDisturbManager
import com.github.naz013.logic.notificationaction.DoNotDisturbPreferences
import com.github.naz013.logic.notificationaction.PhoneCallStateProvider
import com.github.naz013.logic.reminder.query.ResolveReminderV2NotificationSettingsUseCase
import com.github.naz013.logic.workflow.WorkflowTriggerRunner
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.scheduler.JobSchedulerApi
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ReminderActionProcessorTest {
  private val alertHandlerFactory = mockk<ReminderAlertHandlerFactory>()
  private val completeSnoozeFactory = mockk<ReminderCompleteSnoozeFactory>(relaxed = true)
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val doNotDisturbPreferences = mockk<DoNotDisturbPreferences>(relaxed = true)
  private val doNotDisturbManager = mockk<DoNotDisturbManager>()
  private val dateTimeManager = mockk<DateTimeManager>()
  private val jobScheduler = mockk<JobSchedulerApi>(relaxed = true)
  private val phoneCallStateProvider = mockk<PhoneCallStateProvider>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val workflowTriggerRunner = mockk<WorkflowTriggerRunner>(relaxed = true)
  private val resolveReminderV2NotificationSettingsUseCase = mockk<ResolveReminderV2NotificationSettingsUseCase>()
  private val handler = mockk<ActionHandler<ReminderV2>>(relaxed = true)

  private lateinit var processor: ReminderActionProcessor

  private val reminder = ReminderV2(uuId = "1", schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))

  private fun notificationSettings(
    priority: ReminderPriority = ReminderPriority.NORMAL,
    bypassDoNotDisturb: Boolean = false,
  ) = NotificationSettings(
    priority = priority,
    bypassDoNotDisturb = bypassDoNotDisturb,
    category = ReminderNotificationCategory.DEFAULT,
    lockScreenVisibility = LockScreenVisibility.PRIVATE,
  )

  @Before
  fun setUp() {
    coEvery { reminderV2Repository.getById("1") } returns reminder
    coEvery { reminderV2Repository.save(any()) } returns Unit
    every { dateTimeManager.localToUtc(any()) } returns LocalDateTime.now()
    every { alertHandlerFactory.create(any(), any()) } returns handler
    every { phoneCallStateProvider.isPhoneCallActive() } returns false

    processor = ReminderActionProcessor(
      dispatcherProvider = mockDispatcherProvider(),
      alertHandlerFactory = alertHandlerFactory,
      completeSnoozeFactory = completeSnoozeFactory,
      reminderV2Repository = reminderV2Repository,
      doNotDisturbPreferences = doNotDisturbPreferences,
      doNotDisturbManager = doNotDisturbManager,
      dateTimeManager = dateTimeManager,
      jobScheduler = jobScheduler,
      phoneCallStateProvider = phoneCallStateProvider,
      analyticsEventSender = analyticsEventSender,
      workflowTriggerRunner = workflowTriggerRunner,
      resolveReminderV2NotificationSettingsUseCase = resolveReminderV2NotificationSettingsUseCase,
    )
  }

  @Test
  fun `process suppresses the notification during quiet hours when bypassDoNotDisturb is off`() =
    runTest {
      val settings = notificationSettings(bypassDoNotDisturb = false)
      coEvery { resolveReminderV2NotificationSettingsUseCase(reminder) } returns settings
      every { doNotDisturbManager.applyDoNotDisturb(any(), any()) } returns true
      every { doNotDisturbPreferences.doNotDisturbAction } returns 1

      processor.process("1")

      coVerify(exactly = 0) { handler.handle(any()) }
    }

  @Test
  fun `process still shows the notification during quiet hours when bypassDoNotDisturb is on`() =
    runTest {
      val settings = notificationSettings(bypassDoNotDisturb = true)
      coEvery { resolveReminderV2NotificationSettingsUseCase(reminder) } returns settings
      every { doNotDisturbManager.applyDoNotDisturb(any(), any()) } returns true

      processor.process("1")

      coVerify(exactly = 1) { handler.handle(reminder) }
    }

  @Test
  fun `process builds the handler with the resolved notification settings`() =
    runTest {
      val settings = notificationSettings(priority = ReminderPriority.HIGH)
      coEvery { resolveReminderV2NotificationSettingsUseCase(reminder) } returns settings
      every { doNotDisturbManager.applyDoNotDisturb(any(), any()) } returns false

      processor.process("1")

      coVerify(exactly = 1) { alertHandlerFactory.create(any(), settings) }
    }
}
