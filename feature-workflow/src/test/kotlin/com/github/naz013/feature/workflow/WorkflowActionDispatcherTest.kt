package com.github.naz013.feature.workflow

import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.logic.reminder.usecase.ActivateReminderUseCase
import com.github.naz013.logic.reminder.usecase.CompleteReminderUseCase
import com.github.naz013.logic.workflow.PendingWorkflowAction
import com.github.naz013.repository.ReminderV2Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class WorkflowActionDispatcherTest {

  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val activateReminderUseCase = mockk<ActivateReminderUseCase>()
  private val completeReminderUseCase = mockk<CompleteReminderUseCase>()

  private lateinit var dispatcher: WorkflowActionDispatcher

  private fun reminder(id: String) = ReminderV2(
    uuId = id,
    schedule = ReminderSchedule(startDateTime = LocalDateTime.of(2026, 6, 1, 0, 0))
  )

  @Before
  fun setUp() {
    dispatcher = WorkflowActionDispatcher(reminderV2Repository, activateReminderUseCase, completeReminderUseCase)
  }

  @Test
  fun `dispatches CompleteReminder using the pending action's context reminder id`() = runTest {
    val reminder = reminder("triggering-reminder")
    coEvery { reminderV2Repository.getById("triggering-reminder") } returns reminder
    coEvery { completeReminderUseCase(reminder) } returns reminder

    dispatcher.dispatch(PendingWorkflowAction(WorkflowAction.CompleteReminder, "triggering-reminder"))

    coVerify(exactly = 1) { completeReminderUseCase(reminder) }
  }

  @Test
  fun `dispatches ActivateReminder using the action's own target id, not the context reminder id`() = runTest {
    val target = reminder("other-reminder")
    coEvery { reminderV2Repository.getById("other-reminder") } returns target
    coEvery { activateReminderUseCase(target) } returns target

    dispatcher.dispatch(
      PendingWorkflowAction(WorkflowAction.ActivateReminder(reminderId = "other-reminder"), "triggering-reminder")
    )

    coVerify(exactly = 1) { activateReminderUseCase(target) }
    coVerify(exactly = 0) { reminderV2Repository.getById("triggering-reminder") }
  }

  @Test
  fun `no-ops when the target reminder no longer exists`() = runTest {
    coEvery { reminderV2Repository.getById("missing") } returns null

    dispatcher.dispatch(PendingWorkflowAction(WorkflowAction.CompleteReminder, "missing"))

    coVerify(exactly = 0) { completeReminderUseCase(any()) }
  }
}
