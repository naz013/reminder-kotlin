package com.github.naz013.feature.workflow.builder

import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowCondition
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.logic.workflow.CreateWorkflowRuleUseCase
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class WorkflowRuleBuilderViewModelTest : BaseTest() {
  private val workflowRuleRepository = mockk<WorkflowRuleRepository>(relaxed = true)
  private val createWorkflowRuleUseCase = mockk<CreateWorkflowRuleUseCase>(relaxed = true)
  private val reminderV2Repository = mockk<ReminderV2Repository>()
  private val groupV2Repository = mockk<GroupV2Repository>()

  @Before
  override fun setUp() {
    super.setUp()
    coEvery { reminderV2Repository.getAll(active = true, removed = false) } returns emptyList()
    coEvery { groupV2Repository.getAll() } returns emptyList()
  }

  private fun createViewModel(
    scopeType: WorkflowScopeType = WorkflowScopeType.GLOBAL,
    scopeId: String? = null,
    editingRuleId: String? = null,
  ): WorkflowRuleBuilderViewModel =
    WorkflowRuleBuilderViewModel(
      scopeType,
      scopeId,
      editingRuleId,
      mockDispatcherProvider(),
      workflowRuleRepository,
      createWorkflowRuleUseCase,
      reminderV2Repository,
      groupV2Repository,
    )

  @Test
  fun `starts with no trigger, no conditions, and no action`() = runTest {
    val state = createViewModel().state.value

    assertNull(state.trigger)
    assertTrue(state.conditions.isEmpty())
    assertNull(state.action)
    assertFalse(state.canSave)
  }

  @Test
  fun `onTriggerSelected sets the trigger and closes the picker`() = runTest {
    val viewModel = createViewModel()
    viewModel.onTriggerRowClick()

    viewModel.onTriggerSelected(WorkflowTrigger.ReminderCompleted)

    val state = viewModel.state.value
    assertEquals(WorkflowTrigger.ReminderCompleted, state.trigger)
    assertFalse(state.isTriggerPickerVisible)
  }

  @Test
  fun `canSave is true only once both a trigger and an action are set`() = runTest {
    val viewModel = createViewModel()

    viewModel.onTriggerSelected(WorkflowTrigger.ReminderCompleted)
    assertFalse(viewModel.state.value.canSave)

    viewModel.onActionSelected(WorkflowAction.ArchiveReminder)
    assertTrue(viewModel.state.value.canSave)
  }

  @Test
  fun `onConditionSelected appends a new condition when not editing an existing one`() = runTest {
    val viewModel = createViewModel()

    viewModel.onAddConditionClick()
    viewModel.onConditionSelected(WorkflowCondition.GroupIs("group-1"))
    viewModel.onAddConditionClick()
    viewModel.onConditionSelected(WorkflowCondition.GroupIs("group-2"))

    assertEquals(
      listOf(WorkflowCondition.GroupIs("group-1"), WorkflowCondition.GroupIs("group-2")),
      viewModel.state.value.conditions,
    )
  }

  @Test
  fun `onConditionSelected replaces the condition at editingConditionIndex when editing`() = runTest {
    val viewModel = createViewModel()
    viewModel.onAddConditionClick()
    viewModel.onConditionSelected(WorkflowCondition.GroupIs("group-1"))

    viewModel.onEditConditionClick(0)
    viewModel.onConditionSelected(WorkflowCondition.GroupIs("group-2"))

    assertEquals(listOf(WorkflowCondition.GroupIs("group-2")), viewModel.state.value.conditions)
  }

  @Test
  fun `onRemoveConditionClick removes only the condition at that index`() = runTest {
    val viewModel = createViewModel()
    viewModel.onAddConditionClick()
    viewModel.onConditionSelected(WorkflowCondition.GroupIs("group-1"))
    viewModel.onAddConditionClick()
    viewModel.onConditionSelected(WorkflowCondition.GroupIs("group-2"))

    viewModel.onRemoveConditionClick(0)

    assertEquals(listOf(WorkflowCondition.GroupIs("group-2")), viewModel.state.value.conditions)
  }

  @Test
  fun `onSaveClick creates a new rule scoped to the group when scopeType is GROUP`() = runTest {
    val viewModel = createViewModel(scopeType = WorkflowScopeType.GROUP, scopeId = "group-1")
    viewModel.onTriggerSelected(WorkflowTrigger.GroupAllCompleted)
    viewModel.onActionSelected(WorkflowAction.ArchiveReminder)

    viewModel.onSaveClick()

    coVerify {
      createWorkflowRuleUseCase(
        title = any(),
        scope = WorkflowScope.ForGroup("group-1"),
        trigger = WorkflowTrigger.GroupAllCompleted,
        conditions = emptyList(),
        action = WorkflowAction.ArchiveReminder,
      )
    }
    assertTrue(viewModel.state.value.didSave)
  }

  @Test
  fun `onSaveClick does nothing without a trigger or an action`() = runTest {
    val viewModel = createViewModel()
    viewModel.onTriggerSelected(WorkflowTrigger.ReminderCompleted)

    viewModel.onSaveClick()

    coVerify(exactly = 0) { createWorkflowRuleUseCase(any(), any(), any(), any(), any()) }
    assertFalse(viewModel.state.value.didSave)
  }

  @Test
  fun `onSaveClick updates the existing rule in place when editing`() = runTest {
    val existing = WorkflowRule(
      uuId = "rule-1",
      scope = WorkflowScope.Global,
      trigger = WorkflowTrigger.ReminderCompleted,
      action = WorkflowAction.ArchiveReminder,
    )
    coEvery { workflowRuleRepository.getById("rule-1") } returns existing
    val viewModel = createViewModel(editingRuleId = "rule-1")
    viewModel.onTriggerSelected(WorkflowTrigger.GroupAllCompleted)
    viewModel.onActionSelected(WorkflowAction.CompleteReminder)

    viewModel.onSaveClick()

    coVerify {
      workflowRuleRepository.save(
        existing.copy(trigger = WorkflowTrigger.GroupAllCompleted, action = WorkflowAction.CompleteReminder)
      )
    }
    coVerify(exactly = 0) { createWorkflowRuleUseCase(any(), any(), any(), any(), any()) }
  }

  @Test
  fun `loads existing trigger, conditions, and action when editing a rule`() = runTest {
    val existing = WorkflowRule(
      uuId = "rule-1",
      scope = WorkflowScope.Global,
      trigger = WorkflowTrigger.ReminderAgeExceeded(30),
      conditions = listOf(WorkflowCondition.GroupIs("group-1")),
      action = WorkflowAction.ArchiveReminder,
    )
    coEvery { workflowRuleRepository.getById("rule-1") } returns existing

    val state = createViewModel(editingRuleId = "rule-1").state.value

    assertEquals(WorkflowTrigger.ReminderAgeExceeded(30), state.trigger)
    assertEquals(listOf(WorkflowCondition.GroupIs("group-1")), state.conditions)
    assertEquals(WorkflowAction.ArchiveReminder, state.action)
  }

  @Test
  fun `loads available groups and active reminders for the pickers`() = runTest {
    coEvery { groupV2Repository.getAll() } returns listOf(GroupV2(uuId = "group-1", title = "Work"))
    coEvery { reminderV2Repository.getAll(active = true, removed = false) } returns listOf(
      ReminderV2(uuId = "reminder-1", summary = "Buy milk", schedule = ReminderSchedule(startDateTime = LocalDateTime.now()))
    )

    val state = createViewModel().state.value

    assertEquals(listOf(UiWorkflowGroupOption("group-1", "Work")), state.availableGroups)
    assertEquals(listOf(UiWorkflowReminderOption("reminder-1", "Buy milk")), state.availableReminders)
  }
}
