package com.github.naz013.logic.workflow

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTemplateCategory
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.logic.schedule.ScheduleBackgroundWorkUseCase
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.repository.WorkflowTemplateRepository
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveWorkflowRuleAsTemplateUseCaseTest {

  private val scheduleBackgroundWorkUseCase = mockk<ScheduleBackgroundWorkUseCase>(relaxed = true)

  private fun useCase(ruleRepository: WorkflowRuleRepository, templateRepository: WorkflowTemplateRepository) =
    SaveWorkflowRuleAsTemplateUseCase(
      SaveWorkflowTemplateUseCase(templateRepository, scheduleBackgroundWorkUseCase),
      SaveWorkflowRuleUseCase(ruleRepository, scheduleBackgroundWorkUseCase)
    )

  @Test
  fun `creates a user-defined template from the rule's trigger and action`() = runTest {
    val rule = WorkflowRule(
      uuId = "rule-1",
      scope = WorkflowScope.ForGroup("group-1"),
      trigger = WorkflowTrigger.ReminderSnoozedNTimes(count = 3),
      action = WorkflowAction.ArchiveReminder
    )
    val ruleRepository = SavingWorkflowRuleRepository()
    val templateRepository = SavingWorkflowTemplateRepository()

    val template = useCase(ruleRepository, templateRepository)(rule, title = "My custom rule")

    assertEquals("My custom rule", template.title)
    assertEquals(rule.trigger, template.trigger)
    assertEquals(rule.action, template.action)
    assertEquals(false, template.isBuiltIn)
    assertEquals(WorkflowScopeType.entries, template.supportedScopeTypes)
  }

  @Test
  fun `links the originating rule back to the new template`() = runTest {
    val rule = WorkflowRule(
      uuId = "rule-1",
      scope = WorkflowScope.Global,
      trigger = WorkflowTrigger.ReminderCompleted,
      action = WorkflowAction.CompleteReminder
    )
    val ruleRepository = SavingWorkflowRuleRepository()
    val templateRepository = SavingWorkflowTemplateRepository()

    val template = useCase(ruleRepository, templateRepository)(rule)

    val savedRule = ruleRepository.saved.single()
    assertEquals(template.id, savedRule.templateId)
  }
}

private class SavingWorkflowRuleRepository : WorkflowRuleRepository {
  val saved = mutableListOf<WorkflowRule>()

  override suspend fun save(rule: WorkflowRule) { saved.add(rule) }
  override suspend fun getAll(): List<WorkflowRule> = saved
  override suspend fun getEnabled(): List<WorkflowRule> = saved.filter { it.isEnabled }
  override suspend fun getById(id: String): WorkflowRule? = saved.firstOrNull { it.uuId == id }
  override suspend fun getByScope(scopeType: String, scopeId: String?): List<WorkflowRule> = emptyList()
  override suspend fun getByTriggerType(triggerType: String): List<WorkflowRule> = emptyList()
  override suspend fun delete(id: String) = Unit
  override suspend fun deleteAll() = Unit
  override suspend fun updateSyncState(id: String, state: SyncState) = Unit
  override suspend fun getIdsByState(syncStates: List<SyncState>): List<String> = emptyList()
  override suspend fun getAllIds(): List<String> = emptyList()
  override suspend fun countAll(): Int = saved.size
}

private class SavingWorkflowTemplateRepository : WorkflowTemplateRepository {
  val saved = mutableListOf<WorkflowTemplate>()

  override suspend fun save(template: WorkflowTemplate) { saved.add(template) }
  override suspend fun getAll(): List<WorkflowTemplate> = saved
  override suspend fun getByCategory(category: WorkflowTemplateCategory): List<WorkflowTemplate> =
    saved.filter { it.category == category }
  override suspend fun getById(id: String): WorkflowTemplate? = saved.firstOrNull { it.id == id }
  override suspend fun delete(id: String) = Unit
  override suspend fun deleteAll() = Unit
  override suspend fun updateSyncState(id: String, state: SyncState) = Unit
  override suspend fun getIdsByState(syncStates: List<SyncState>): List<String> = emptyList()
  override suspend fun getAllIds(): List<String> = emptyList()
  override suspend fun countAll(): Int = saved.size
}
