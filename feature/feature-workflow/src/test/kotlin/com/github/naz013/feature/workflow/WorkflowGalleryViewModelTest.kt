package com.github.naz013.feature.workflow

import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTemplateCategory
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.logic.workflow.ApplyWorkflowTemplateUseCase
import com.github.naz013.logic.workflow.DeleteWorkflowRuleUseCase
import com.github.naz013.logic.workflow.GetGlobalWorkflowRulesUseCase
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class WorkflowGalleryViewModelTest : BaseTest() {
  private val getGlobalWorkflowRulesUseCase = mockk<GetGlobalWorkflowRulesUseCase>()
  private val getWorkflowTemplatesUseCase = mockk<GetWorkflowTemplatesUseCase>()
  private val applyWorkflowTemplateUseCase = mockk<ApplyWorkflowTemplateUseCase>(relaxed = true)
  private val saveWorkflowRuleAsTemplateUseCase = mockk<SaveWorkflowRuleAsTemplateUseCase>(relaxed = true)
  private val saveWorkflowRuleUseCase = mockk<SaveWorkflowRuleUseCase>(relaxed = true)
  private val deleteWorkflowRuleUseCase = mockk<DeleteWorkflowRuleUseCase>(relaxed = true)
  private val workflowRuleRepository = mockk<WorkflowRuleRepository>(relaxed = true)

  @Before
  override fun setUp() {
    super.setUp()
    coEvery { getGlobalWorkflowRulesUseCase() } returns emptyList()
    coEvery { getWorkflowTemplatesUseCase() } returns emptyList()
  }

  private fun createViewModel(): WorkflowGalleryViewModel =
    WorkflowGalleryViewModel(
      dispatcherProvider = mockDispatcherProvider(),
      getGlobalWorkflowRulesUseCase = getGlobalWorkflowRulesUseCase,
      getWorkflowTemplatesUseCase = getWorkflowTemplatesUseCase,
      applyWorkflowTemplateUseCase = applyWorkflowTemplateUseCase,
      saveWorkflowRuleAsTemplateUseCase = saveWorkflowRuleAsTemplateUseCase,
      saveWorkflowRuleUseCase = saveWorkflowRuleUseCase,
      deleteWorkflowRuleUseCase = deleteWorkflowRuleUseCase,
      workflowRuleRepository = workflowRuleRepository,
    )

  @Test
  fun `loads global rules and templates grouped by category`() = runTest {
    val rule = WorkflowRule(
      uuId = "rule-1",
      title = "Archive after 30 days",
      scope = WorkflowScope.Global,
      trigger = WorkflowTrigger.ReminderAgeExceeded(30),
      action = WorkflowAction.ArchiveReminder,
    )
    val template = WorkflowTemplate(
      id = "template-1",
      title = "Archive after 30 days",
      category = WorkflowTemplateCategory.REMINDER_LIFECYCLE,
      supportedScopeTypes = listOf(WorkflowScopeType.GLOBAL),
      trigger = WorkflowTrigger.ReminderAgeExceeded(30),
      action = WorkflowAction.ArchiveReminder,
    )
    coEvery { getGlobalWorkflowRulesUseCase() } returns listOf(rule)
    coEvery { getWorkflowTemplatesUseCase() } returns listOf(template)

    val state = createViewModel().state.value

    assertFalse(state.isLoading)
    assertEquals("rule-1", state.globalRules.single().id)
    assertEquals(
      listOf("template-1"),
      state.templatesByCategory.getValue(WorkflowTemplateCategory.REMINDER_LIFECYCLE).map { it.id }
    )
  }

  @Test
  fun `onRuleEnabledChange toggles the rule and reloads`() = runTest {
    val rule = WorkflowRule(
      uuId = "rule-1",
      scope = WorkflowScope.Global,
      trigger = WorkflowTrigger.ReminderAgeExceeded(30),
      action = WorkflowAction.ArchiveReminder,
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
      scope = WorkflowScope.Global,
      trigger = WorkflowTrigger.ReminderAgeExceeded(30),
      action = WorkflowAction.ArchiveReminder,
    )
    coEvery { workflowRuleRepository.getById("rule-1") } returns rule
    val viewModel = createViewModel()

    viewModel.onSaveRuleAsTemplateClick("rule-1")

    coVerify { saveWorkflowRuleAsTemplateUseCase(rule) }
  }

  @Test
  fun `onApplyTemplateClick applies the template globally`() = runTest {
    val template = WorkflowTemplate(
      id = "template-1",
      trigger = WorkflowTrigger.ReminderAgeExceeded(30),
      action = WorkflowAction.ArchiveReminder,
    )
    coEvery { getWorkflowTemplatesUseCase() } returns listOf(template)
    val viewModel = createViewModel()

    viewModel.onApplyTemplateClick("template-1")

    coVerify { applyWorkflowTemplateUseCase(template, WorkflowScope.Global) }
  }
}
