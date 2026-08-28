package com.github.naz013.feature.workflow

import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.logic.workflow.ApplyWorkflowTemplateUseCase
import com.github.naz013.logic.workflow.DeleteWorkflowRuleUseCase
import com.github.naz013.logic.workflow.GetWorkflowRulesForReminderUseCase
import com.github.naz013.logic.workflow.GetWorkflowTemplatesUseCase
import com.github.naz013.logic.workflow.SaveWorkflowRuleAsTemplateUseCase
import com.github.naz013.logic.workflow.SaveWorkflowRuleUseCase
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.testing.BaseTest
import com.github.naz013.testing.mockDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class WorkflowRulesForReminderViewModelTest : BaseTest() {
  private val reminderId = "reminder-1"
  private val getWorkflowRulesForReminderUseCase = mockk<GetWorkflowRulesForReminderUseCase>()
  private val getWorkflowTemplatesUseCase = mockk<GetWorkflowTemplatesUseCase>()
  private val applyWorkflowTemplateUseCase = mockk<ApplyWorkflowTemplateUseCase>(relaxed = true)
  private val saveWorkflowRuleAsTemplateUseCase = mockk<SaveWorkflowRuleAsTemplateUseCase>(relaxed = true)
  private val saveWorkflowRuleUseCase = mockk<SaveWorkflowRuleUseCase>(relaxed = true)
  private val deleteWorkflowRuleUseCase = mockk<DeleteWorkflowRuleUseCase>(relaxed = true)
  private val workflowRuleRepository = mockk<WorkflowRuleRepository>(relaxed = true)

  @Before
  override fun setUp() {
    super.setUp()
    coEvery { getWorkflowRulesForReminderUseCase(reminderId) } returns emptyList()
    coEvery { getWorkflowTemplatesUseCase() } returns emptyList()
  }

  private fun createViewModel(): WorkflowRulesForReminderViewModel =
    WorkflowRulesForReminderViewModel(
      reminderId = reminderId,
      dispatcherProvider = mockDispatcherProvider(),
      getWorkflowRulesForReminderUseCase = getWorkflowRulesForReminderUseCase,
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

  @Test
  fun `onDeleteRuleClick deletes the rule`() = runTest {
    val viewModel = createViewModel()

    viewModel.onDeleteRuleClick("rule-1")

    coVerify { deleteWorkflowRuleUseCase("rule-1") }
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
