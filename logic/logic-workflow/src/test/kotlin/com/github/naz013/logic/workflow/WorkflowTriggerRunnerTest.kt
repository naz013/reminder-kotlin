package com.github.naz013.logic.workflow

import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.workflow.WorkflowAction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class WorkflowTriggerRunnerTest {

  private val workflowEngine = mockk<WorkflowEngine>()
  private val workflowActionDispatcher = mockk<WorkflowActionDispatcher>(relaxed = true)
  private val workflowConfig = mockk<WorkflowConfig> { every { isEnabled } returns true }

  private lateinit var runner: WorkflowTriggerRunner

  @Before
  fun setUp() {
    runner = WorkflowTriggerRunner(workflowEngine, workflowActionDispatcher, workflowConfig)
  }

  @Test
  fun `runDailyPolling dispatches every pending action from age-based and group-completion rules`() = runTest {
    val ageBased = PendingWorkflowAction(WorkflowAction.ArchiveReminder, "age-reminder")
    val groupCompletion = PendingWorkflowAction(WorkflowAction.CompleteReminder, "group-reminder")
    coEvery { workflowEngine.runAgeBasedRules(any()) } returns listOf(ageBased)
    coEvery { workflowEngine.runGroupCompletionRules(any()) } returns listOf(groupCompletion)
    coEvery { workflowEngine.runScheduleRules(any()) } returns emptyList()

    runner.runDailyPolling()

    coVerify(exactly = 1) { workflowActionDispatcher.dispatch(ageBased) }
    coVerify(exactly = 1) { workflowActionDispatcher.dispatch(groupCompletion) }
  }

  @Test
  fun `runDailyPolling dispatches nothing when both rule sets are empty`() = runTest {
    coEvery { workflowEngine.runAgeBasedRules(any()) } returns emptyList()
    coEvery { workflowEngine.runGroupCompletionRules(any()) } returns emptyList()
    coEvery { workflowEngine.runScheduleRules(any()) } returns emptyList()

    runner.runDailyPolling()

    coVerify(exactly = 0) { workflowActionDispatcher.dispatch(any()) }
  }

  @Test
  fun `runUnacknowledgedPolling dispatches every pending action from the engine`() = runTest {
    val pending = PendingWorkflowAction(WorkflowAction.ArchiveReminder, "unacked-reminder")
    coEvery { workflowEngine.runUnacknowledgedRules(any()) } returns listOf(pending)

    runner.runUnacknowledgedPolling()

    coVerify(exactly = 1) { workflowActionDispatcher.dispatch(pending) }
  }

  @Test
  fun `onReminderCompleted dispatches every pending action for that reminder`() = runTest {
    val pending = PendingWorkflowAction(WorkflowAction.ArchiveReminder, "reminder-1")
    coEvery { workflowEngine.runReminderCompletedRules("reminder-1", any()) } returns listOf(pending)

    runner.onReminderCompleted("reminder-1")

    coVerify(exactly = 1) { workflowActionDispatcher.dispatch(pending) }
  }

  @Test
  fun `onReminderCreated dispatches every pending action for that reminder`() = runTest {
    val pending = PendingWorkflowAction(WorkflowAction.ArchiveReminder, "reminder-1")
    coEvery { workflowEngine.runReminderCreatedRules("reminder-1", any()) } returns listOf(pending)

    runner.onReminderCreated("reminder-1")

    coVerify(exactly = 1) { workflowActionDispatcher.dispatch(pending) }
  }

  @Test
  fun `onReminderSnoozed dispatches every pending action for that reminder`() = runTest {
    val pending = PendingWorkflowAction(
      WorkflowAction.ApplyNotificationOverride(override = NotificationSettingsOverride()),
      "reminder-1"
    )
    coEvery { workflowEngine.runSnoozeCountRules("reminder-1", any()) } returns listOf(pending)

    runner.onReminderSnoozed("reminder-1")

    coVerify(exactly = 1) { workflowActionDispatcher.dispatch(pending) }
  }

  @Test
  fun `onLocationEntered dispatches every pending action for that reminder`() = runTest {
    val pending = PendingWorkflowAction(WorkflowAction.CompleteReminder, "reminder-1")
    coEvery { workflowEngine.runLocationEnteredRules("reminder-1", any()) } returns listOf(pending)

    runner.onLocationEntered("reminder-1")

    coVerify(exactly = 1) { workflowActionDispatcher.dispatch(pending) }
  }

  @Test
  fun `onLocationExited dispatches every pending action for that reminder`() = runTest {
    val pending = PendingWorkflowAction(WorkflowAction.CompleteReminder, "reminder-1")
    coEvery { workflowEngine.runLocationExitedRules("reminder-1", any()) } returns listOf(pending)

    runner.onLocationExited("reminder-1")

    coVerify(exactly = 1) { workflowActionDispatcher.dispatch(pending) }
  }

  @Test
  fun `onBluetoothConnected dispatches every pending action for that device`() = runTest {
    val pending = PendingWorkflowAction(WorkflowAction.ArchiveReminder, "reminder-1")
    coEvery { workflowEngine.runBluetoothConnectedRules("AA:BB:CC:DD:EE:FF") } returns listOf(pending)

    runner.onBluetoothConnected("AA:BB:CC:DD:EE:FF")

    coVerify(exactly = 1) { workflowActionDispatcher.dispatch(pending) }
  }

  @Test
  fun `onBluetoothDisconnected dispatches every pending action for that device`() = runTest {
    val pending = PendingWorkflowAction(WorkflowAction.ArchiveReminder, "reminder-1")
    coEvery { workflowEngine.runBluetoothDisconnectedRules("AA:BB:CC:DD:EE:FF") } returns listOf(pending)

    runner.onBluetoothDisconnected("AA:BB:CC:DD:EE:FF")

    coVerify(exactly = 1) { workflowActionDispatcher.dispatch(pending) }
  }

  @Test
  fun `onWifiConnected dispatches every pending action for that network`() = runTest {
    val pending = PendingWorkflowAction(WorkflowAction.ArchiveReminder, "reminder-1")
    coEvery { workflowEngine.runWifiConnectedRules("Home WiFi") } returns listOf(pending)

    runner.onWifiConnected("Home WiFi")

    coVerify(exactly = 1) { workflowActionDispatcher.dispatch(pending) }
  }

  @Test
  fun `onWifiDisconnected dispatches every pending action for that network`() = runTest {
    val pending = PendingWorkflowAction(WorkflowAction.ArchiveReminder, "reminder-1")
    coEvery { workflowEngine.runWifiDisconnectedRules("Home WiFi") } returns listOf(pending)

    runner.onWifiDisconnected("Home WiFi")

    coVerify(exactly = 1) { workflowActionDispatcher.dispatch(pending) }
  }

  @Test
  fun `every method no-ops without touching the engine when the workflow feature flag is disabled`() = runTest {
    every { workflowConfig.isEnabled } returns false

    runner.runDailyPolling()
    runner.runUnacknowledgedPolling()
    runner.onReminderCompleted("reminder-1")
    runner.onReminderSnoozed("reminder-1")
    runner.onReminderCreated("reminder-1")
    runner.onLocationEntered("reminder-1")
    runner.onLocationExited("reminder-1")
    runner.onBluetoothConnected("AA:BB:CC:DD:EE:FF")
    runner.onBluetoothDisconnected("AA:BB:CC:DD:EE:FF")
    runner.onWifiConnected("Home WiFi")
    runner.onWifiDisconnected("Home WiFi")

    coVerify(exactly = 0) { workflowEngine.runAgeBasedRules(any()) }
    coVerify(exactly = 0) { workflowEngine.runGroupCompletionRules(any()) }
    coVerify(exactly = 0) { workflowEngine.runScheduleRules(any()) }
    coVerify(exactly = 0) { workflowEngine.runUnacknowledgedRules(any()) }
    coVerify(exactly = 0) { workflowEngine.runReminderCompletedRules(any(), any()) }
    coVerify(exactly = 0) { workflowEngine.runSnoozeCountRules(any(), any()) }
    coVerify(exactly = 0) { workflowEngine.runReminderCreatedRules(any(), any()) }
    coVerify(exactly = 0) { workflowEngine.runLocationEnteredRules(any(), any()) }
    coVerify(exactly = 0) { workflowEngine.runLocationExitedRules(any(), any()) }
    coVerify(exactly = 0) { workflowEngine.runBluetoothConnectedRules(any()) }
    coVerify(exactly = 0) { workflowEngine.runBluetoothDisconnectedRules(any()) }
    coVerify(exactly = 0) { workflowEngine.runWifiConnectedRules(any()) }
    coVerify(exactly = 0) { workflowEngine.runWifiDisconnectedRules(any()) }
    coVerify(exactly = 0) { workflowActionDispatcher.dispatch(any()) }
  }

  @Test
  fun `dispatches multiple pending actions in order when a single trigger returns several`() = runTest {
    val first = PendingWorkflowAction(WorkflowAction.CompleteReminder, "reminder-1")
    val second = PendingWorkflowAction(WorkflowAction.ActivateReminder(reminderId = "reminder-2"), "reminder-1")
    coEvery { workflowEngine.runReminderCompletedRules("reminder-1", any()) } returns listOf(first, second)

    runner.onReminderCompleted("reminder-1")

    coVerifyOrder {
      workflowActionDispatcher.dispatch(first)
      workflowActionDispatcher.dispatch(second)
    }
  }
}
