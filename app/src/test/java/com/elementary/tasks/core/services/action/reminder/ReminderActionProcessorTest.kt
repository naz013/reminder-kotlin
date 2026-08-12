package com.elementary.tasks.core.services.action.reminder

import android.content.Context
import android.media.AudioManager
import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.services.action.ActionHandler
import com.elementary.tasks.core.utils.datetime.DoNotDisturbManager
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.common.ContextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.domain.reminder.v2.LockScreenVisibility
import com.github.naz013.domain.reminder.v2.NotificationSettings
import com.github.naz013.domain.reminder.v2.ReminderNotificationCategory
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.workflow.WorkflowTriggerRunner
import com.github.naz013.logic.reminder.query.ResolveReminderV2NotificationSettingsUseCase
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.scheduler.JobSchedulerApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ReminderActionProcessorTest : BaseTest() {
  private val reminderHandlerFactory = mockk<ReminderHandlerFactory>()
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val prefs = mockk<Prefs>(relaxed = true)
  private val doNotDisturbManager = mockk<DoNotDisturbManager>()
  private val dateTimeManager = mockk<DateTimeManager>()
  private val jobScheduler = mockk<JobSchedulerApi>(relaxed = true)
  private val contextProvider = mockk<ContextProvider>(relaxed = true)
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
  override fun setUp() {
    super.setUp()
    coEvery { reminderV2Repository.getById("1") } returns reminder
    coEvery { reminderV2Repository.save(any()) } returns Unit
    every { dateTimeManager.localToUtc(any()) } returns LocalDateTime.now()
    every { reminderHandlerFactory.createAction(any(), any()) } returns handler

    // SuperUtil.isPhoneCallActive(context) reads AudioManager.mode via contextProvider.context -
    // stub the chain so it resolves to "not in a call" instead of throwing on a bare relaxed mock.
    val audioManager = mockk<AudioManager> { every { mode } returns AudioManager.MODE_NORMAL }
    val context = mockk<Context> { every { getSystemService(Context.AUDIO_SERVICE) } returns audioManager }
    every { contextProvider.context } returns context

    processor = ReminderActionProcessor(
      dispatcherProvider = mockDispatcherProvider(),
      reminderHandlerFactory = reminderHandlerFactory,
      reminderV2Repository = reminderV2Repository,
      prefs = prefs,
      doNotDisturbManager = doNotDisturbManager,
      dateTimeManager = dateTimeManager,
      jobScheduler = jobScheduler,
      contextProvider = contextProvider,
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
      every { prefs.doNotDisturbAction } returns 1

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

      coVerify(exactly = 1) { reminderHandlerFactory.createAction(any(), settings) }
    }
}
