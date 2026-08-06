package com.elementary.tasks.workflow

import com.elementary.tasks.BaseTest
import com.elementary.tasks.mockDispatcherProvider
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.usecase.reminders.ApplyWorkflowTemplateUseCase
import com.github.naz013.usecase.reminders.GetWorkflowRulesForGroupUseCase
import com.github.naz013.usecase.reminders.GetWorkflowTemplatesUseCase
import com.github.naz013.usecase.reminders.SaveWorkflowRuleAsTemplateUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class WorkflowRulesForGroupViewModelTest : BaseTest() {
  private val groupId = "group-1"
  private val getWorkflowRulesForGroupUseCase = mockk<GetWorkflowRulesForGroupUseCase>()
  private val getWorkflowTemplatesUseCase = mockk<GetWorkflowTemplatesUseCase>()
  private val applyWorkflowTemplateUseCase = mockk<ApplyWorkflowTemplateUseCase>(relaxed = true)
  private val saveWorkflowRuleAsTemplateUseCase = mockk<SaveWorkflowRuleAsTemplateUseCase>(relaxed = true)
  private val workflowRuleRepository = mockk<WorkflowRuleRepository>(relaxed = true)

  @Before
  override fun setUp() {
    super.setUp()
    coEvery { getWorkflowRulesForGroupUseCase(groupId) } returns emptyList()
    coEvery { getWorkflowTemplatesUseCase() } returns emptyList()
  }

  private fun createViewModel(): WorkflowRulesForGroupViewModel =
    WorkflowRulesForGroupViewModel(
      groupId = groupId,
      dispatcherProvider = mockDispatcherProvider(),
      getWorkflowRulesForGroupUseCase = getWorkflowRulesForGroupUseCase,
      getWorkflowTemplatesUseCase = getWorkflowTemplatesUseCase,
      applyWorkflowTemplateUseCase = applyWorkflowTemplateUseCase,
      saveWorkflowRuleAsTemplateUseCase = saveWorkflowRuleAsTemplateUseCase,
      workflowRuleRepository = workflowRuleRepository,
    )

  @Test
  fun `onApplyTemplateClick applies the template to this group`() = runTest {
    val template = WorkflowTemplate(
      id = "template-1",
      trigger = WorkflowTrigger.GroupAllCompleted,
      action = WorkflowAction.ArchiveReminder,
    )
    coEvery { getWorkflowTemplatesUseCase() } returns listOf(template)
    val viewModel = createViewModel()

    viewModel.onApplyTemplateClick("template-1")

    coVerify { applyWorkflowTemplateUseCase(template, WorkflowScope.ForGroup(groupId)) }
  }

  @Test
  fun `onDeleteRuleClick deletes the rule`() = runTest {
    val viewModel = createViewModel()

    viewModel.onDeleteRuleClick("rule-1")

    coVerify { workflowRuleRepository.delete("rule-1") }
  }

  @Test
  fun `onSaveRuleAsTemplateClick saves the rule as a template`() = runTest {
    val rule = WorkflowRule(
      uuId = "rule-1",
      scope = WorkflowScope.ForGroup(groupId),
      trigger = WorkflowTrigger.GroupAllCompleted,
      action = WorkflowAction.ArchiveReminder,
    )
    coEvery { workflowRuleRepository.getById("rule-1") } returns rule
    val viewModel = createViewModel()

    viewModel.onSaveRuleAsTemplateClick("rule-1")

    coVerify { saveWorkflowRuleAsTemplateUseCase(rule) }
  }
}
