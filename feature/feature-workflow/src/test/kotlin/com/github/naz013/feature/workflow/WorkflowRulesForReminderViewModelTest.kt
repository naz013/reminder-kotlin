package com.github.naz013.feature.workflow

import com.github.naz013.common.TextProvider
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.feature.common.viewmodel.UndoEvent
import com.github.naz013.logic.workflow.ApplyWorkflowTemplateUseCase
import com.github.naz013.logic.workflow.DeleteWorkflowRuleUseCase
import com.github.naz013.logic.workflow.GetWorkflowTemplatesUseCase
import com.github.naz013.logic.workflow.SaveWorkflowRuleAsTemplateUseCase
import com.github.naz013.logic.workflow.SaveWorkflowRuleUseCase
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class WorkflowRulesForReminderViewModelTest : BaseTest() {
  private val reminderId = "reminder-1"
  private val getWorkflowTemplatesUseCase = mockk<GetWorkflowTemplatesUseCase>()
  private val applyWorkflowTemplateUseCase = mockk<ApplyWorkflowTemplateUseCase>(relaxed = true)
  private val saveWorkflowRuleAsTemplateUseCase = mockk<SaveWorkflowRuleAsTemplateUseCase>(relaxed = true)
  private val saveWorkflowRuleUseCase = mockk<SaveWorkflowRuleUseCase>(relaxed = true)
  private val deleteWorkflowRuleUseCase = mockk<DeleteWorkflowRuleUseCase>(relaxed = true)
  private val workflowRuleRepository = mockk<WorkflowRuleRepository>(relaxed = true)
  private val textProvider = mockk<TextProvider>(relaxed = true)

  @Before
  override fun setUp() {
    super.setUp()
    every { workflowRuleRepository.observeByScope(any(), any()) } returns flowOf(emptyList())
    coEvery { getWorkflowTemplatesUseCase() } returns emptyList()
  }

  private fun createViewModel(): WorkflowRulesForReminderViewModel =
    WorkflowRulesForReminderViewModel(
      reminderId = reminderId,
      dispatcherProvider = mockDispatcherProvider(),
      textProvider = textProvider,
      getWorkflowTemplatesUseCase = getWorkflowTemplatesUseCase,
      applyWorkflowTemplateUseCase = applyWorkflowTemplateUseCase,
      saveWorkflowRuleAsTemplateUseCase = saveWorkflowRuleAsTemplateUseCase,
      saveWorkflowRuleUseCase = saveWorkflowRuleUseCase,
      deleteWorkflowRuleUseCase = deleteWorkflowRuleUseCase,
      workflowRuleRepository = workflowRuleRepository,
    )

  @Test
  fun `onApplyTemplateClick applies the template to this reminder`() = runTest {
    val template = WorkflowTemplate(
      id = "template-1",
      trigger = WorkflowTrigger.LocationEntered,
      action = WorkflowAction.CompleteReminder,
    )
    coEvery { getWorkflowTemplatesUseCase() } returns listOf(template)
    val viewModel = createViewModel()

    viewModel.onApplyTemplateClick("template-1")

    coVerify { applyWorkflowTemplateUseCase(template, WorkflowScope.ForReminder(reminderId)) }
  }

  @Test
  fun `onRuleEnabledChange toggles the rule`() = runTest {
    val rule = WorkflowRule(
      uuId = "rule-1",
      scope = WorkflowScope.ForReminder(reminderId),
      trigger = WorkflowTrigger.LocationEntered,
      action = WorkflowAction.CompleteReminder,
      isEnabled = true,
    )
    coEvery { workflowRuleRepository.getById("rule-1") } returns rule
    val viewModel = createViewModel()

    viewModel.onRuleEnabledChange("rule-1", false)

    coVerify { saveWorkflowRuleUseCase(rule.copy(isEnabled = false)) }
  }

  private fun rule() = WorkflowRule(
    uuId = "rule-1",
    scope = WorkflowScope.ForReminder(reminderId),
    trigger = WorkflowTrigger.LocationEntered,
    action = WorkflowAction.CompleteReminder,
  )

  @Test
  fun `onDeleteRuleClick hides the rule without deleting it before commit`() = runTest {
    every { textProvider.getText(R.string.workflow_rule_deleted) } returns "Rule deleted"
    every { workflowRuleRepository.observeByScope(any(), any()) } returns flowOf(listOf(rule()))
    val viewModel = createViewModel()

    viewModel.onDeleteRuleClick("rule-1")

    coVerify(exactly = 0) { deleteWorkflowRuleUseCase(any()) }
    assertEquals(emptyList<String>(), viewModel.state.value.rules.map { it.id })
    assertEquals(
      UndoEvent(batchKey = "rule-1", message = "Rule deleted", token = 1L),
      viewModel.undoDelete.undoEvents.first(),
    )
  }

  @Test
  fun `commit deletes the rule exactly once`() = runTest {
    val viewModel = createViewModel()
    viewModel.onDeleteRuleClick("rule-1")

    viewModel.undoDelete.commit("rule-1")
    viewModel.undoDelete.commit("rule-1")

    coVerify(exactly = 1) { deleteWorkflowRuleUseCase("rule-1") }
  }

  @Test
  fun `undo keeps the rule visible and never deletes it`() = runTest {
    every { workflowRuleRepository.observeByScope(any(), any()) } returns flowOf(listOf(rule()))
    val viewModel = createViewModel()
    viewModel.onDeleteRuleClick("rule-1")

    viewModel.undoDelete.undo("rule-1")
    viewModel.undoDelete.commit("rule-1")

    assertEquals(listOf("rule-1"), viewModel.state.value.rules.map { it.id })
    coVerify(exactly = 0) { deleteWorkflowRuleUseCase(any()) }
  }

  @Test
  fun `clearing the view model commits a pending rule delete`() = runTest {
    val viewModel = createViewModel()
    viewModel.onDeleteRuleClick("rule-1")

    WorkflowRulesForReminderViewModel::class.java.getDeclaredMethod("onCleared").apply { isAccessible = true }.invoke(viewModel)

    coVerify(exactly = 1) { deleteWorkflowRuleUseCase("rule-1") }
  }

  @Test
  fun `onSaveRuleAsTemplateClick saves the rule as a template`() = runTest {
    val rule = WorkflowRule(
      uuId = "rule-1",
      scope = WorkflowScope.ForReminder(reminderId),
      trigger = WorkflowTrigger.LocationEntered,
      action = WorkflowAction.CompleteReminder,
    )
    coEvery { workflowRuleRepository.getById("rule-1") } returns rule
    val viewModel = createViewModel()

    viewModel.onSaveRuleAsTemplateClick("rule-1")

    coVerify { saveWorkflowRuleAsTemplateUseCase(rule) }
  }
}
