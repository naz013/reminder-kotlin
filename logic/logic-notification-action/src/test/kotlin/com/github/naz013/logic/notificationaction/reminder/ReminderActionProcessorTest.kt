package com.github.naz013.logic.notificationaction.reminder

import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.common.TextProvider
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
import com.github.naz013.logic.notificationaction.ForegroundStateTracker
import com.github.naz013.logic.notificationaction.InAppAlertBus
import com.github.naz013.logic.notificationaction.InAppAlertPreferences
import com.github.naz013.logic.notificationaction.PhoneCallStateProvider
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.logic.reminder.query.ResolveReminderV2NotificationSettingsUseCase
import com.github.naz013.logic.workflow.WorkflowTriggerRunner
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.scheduler.JobSchedulerApi
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
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
  private val inAppAlertBus = mockk<InAppAlertBus>(relaxed = true)
  private val foregroundStateTracker = mockk<ForegroundStateTracker>()
  private val inAppAlertPreferences = mockk<InAppAlertPreferences>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val reminderPreferences = mockk<ReminderPreferences>(relaxed = true)
  private val handler = mockk<ActionHandler<ReminderV2>>(relaxed = true)

  private lateinit var processor: ReminderActionProcessor

  private val reminder = ReminderV2(uuId = "1", schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))

  private fun notificationSettings(
    priority: ReminderPriority = ReminderPriority.NORMAL,
    bypassDoNotDisturb: Boolean = false,
    repeatNotification: Boolean = false,
  ) = NotificationSettings(
    priority = priority,
    bypassDoNotDisturb = bypassDoNotDisturb,
    repeatNotification = repeatNotification,
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
    every { foregroundStateTracker.isForeground } returns MutableStateFlow(false)
    every { inAppAlertPreferences.isInAppAlertBannerEnabled } returns true
    every { reminderPreferences.maxRepeatCount } returns 10
    every { reminderPreferences.escalateAfterRepeats } returns 3

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
      inAppAlertBus = inAppAlertBus,
      foregroundStateTracker = foregroundStateTracker,
      inAppAlertPreferences = inAppAlertPreferences,
      textProvider = textProvider,
      reminderPreferences = reminderPreferences,
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

  @Test
  fun `process does not emit an in-app alert when the app is backgrounded`() =
    runTest {
      val settings = notificationSettings()
      coEvery { resolveReminderV2NotificationSettingsUseCase(reminder) } returns settings
      every { doNotDisturbManager.applyDoNotDisturb(any(), any()) } returns false
      every { foregroundStateTracker.isForeground } returns MutableStateFlow(false)

      processor.process("1")

      verify(exactly = 0) { inAppAlertBus.show(any()) }
    }

  @Test
  fun `process emits an in-app alert when the app is foregrounded`() =
    runTest {
      val settings = notificationSettings()
      coEvery { resolveReminderV2NotificationSettingsUseCase(reminder) } returns settings
      every { doNotDisturbManager.applyDoNotDisturb(any(), any()) } returns false
      every { foregroundStateTracker.isForeground } returns MutableStateFlow(true)

      processor.process("1")

      verify(exactly = 1) { inAppAlertBus.show(any()) }
    }

  @Test
  fun `process does not emit an in-app alert when the in-app banner setting is disabled`() =
    runTest {
      val settings = notificationSettings()
      coEvery { resolveReminderV2NotificationSettingsUseCase(reminder) } returns settings
      every { doNotDisturbManager.applyDoNotDisturb(any(), any()) } returns false
      every { foregroundStateTracker.isForeground } returns MutableStateFlow(true)
      every { inAppAlertPreferences.isInAppAlertBannerEnabled } returns false

      processor.process("1")

      verify(exactly = 0) { inAppAlertBus.show(any()) }
    }

  @Test
  fun `process does not emit an in-app alert when quiet hours suppress the notification`() =
    runTest {
      val settings = notificationSettings(bypassDoNotDisturb = false)
      coEvery { resolveReminderV2NotificationSettingsUseCase(reminder) } returns settings
      every { doNotDisturbManager.applyDoNotDisturb(any(), any()) } returns true
      every { doNotDisturbPreferences.doNotDisturbAction } returns 1
      every { foregroundStateTracker.isForeground } returns MutableStateFlow(true)

      processor.process("1")

      verify(exactly = 0) { inAppAlertBus.show(any()) }
    }

  @Test
  fun `process schedules a repeat when repeat notification is enabled and under the cap`() =
    runTest {
      val settings = notificationSettings(repeatNotification = true)
      coEvery { resolveReminderV2NotificationSettingsUseCase(reminder) } returns settings
      every { doNotDisturbManager.applyDoNotDisturb(any(), any()) } returns false

      processor.process("1", repeatCount = 2)

      coVerify(exactly = 1) { jobScheduler.scheduleReminderRepeat(reminder, 3) }
    }

  @Test
  fun `process does not schedule a repeat once the max repeat count is reached`() =
    runTest {
      val settings = notificationSettings(repeatNotification = true)
      coEvery { resolveReminderV2NotificationSettingsUseCase(reminder) } returns settings
      every { doNotDisturbManager.applyDoNotDisturb(any(), any()) } returns false
      every { reminderPreferences.maxRepeatCount } returns 3

      processor.process("1", repeatCount = 3)

      coVerify(exactly = 0) { jobScheduler.scheduleReminderRepeat(any(), any()) }
    }

  @Test
  fun `process does not schedule a repeat when repeat notification is disabled`() =
    runTest {
      val settings = notificationSettings(repeatNotification = false)
      coEvery { resolveReminderV2NotificationSettingsUseCase(reminder) } returns settings
      every { doNotDisturbManager.applyDoNotDisturb(any(), any()) } returns false

      processor.process("1", repeatCount = 1)

      coVerify(exactly = 0) { jobScheduler.scheduleReminderRepeat(any(), any()) }
    }

  @Test
  fun `process escalates delivery once the escalation threshold is reached`() =
    runTest {
      val settings = notificationSettings(priority = ReminderPriority.NORMAL, bypassDoNotDisturb = false)
      coEvery { resolveReminderV2NotificationSettingsUseCase(reminder) } returns settings
      every { reminderPreferences.escalateAfterRepeats } returns 3
      every { doNotDisturbManager.applyDoNotDisturb(any(), any()) } returns false

      processor.process("1", repeatCount = 3)

      coVerify(exactly = 1) {
        alertHandlerFactory.create(
          any(),
          match {
            it.bypassDoNotDisturb && it.wakeScreen && it.priority == ReminderPriority.HIGHEST
          },
        )
      }
    }

  @Test
  fun `process below the escalation threshold does not alter the resolved settings`() =
    runTest {
      val settings = notificationSettings(priority = ReminderPriority.NORMAL, bypassDoNotDisturb = false)
      coEvery { resolveReminderV2NotificationSettingsUseCase(reminder) } returns settings
      every { reminderPreferences.escalateAfterRepeats } returns 3
      every { doNotDisturbManager.applyDoNotDisturb(any(), any()) } returns false

      processor.process("1", repeatCount = 2)

      coVerify(exactly = 1) { alertHandlerFactory.create(any(), settings) }
    }

  @Test
  fun `process breaks through do not disturb once escalated even though the base settings do not bypass it`() =
    runTest {
      val settings = notificationSettings(bypassDoNotDisturb = false)
      coEvery { resolveReminderV2NotificationSettingsUseCase(reminder) } returns settings
      every { reminderPreferences.escalateAfterRepeats } returns 1
      every { doNotDisturbManager.applyDoNotDisturb(any(), any()) } returns true

      processor.process("1", repeatCount = 1)

      coVerify(exactly = 1) { handler.handle(reminder) }
    }
}
