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
import org.junit.Assert.assertNull
import org.junit.Test

class ApplyWorkflowTemplateUseCaseTest {

  private val template = WorkflowTemplate(
    id = "template-1",
    title = "Archive completed reminders after 30 days",
    category = WorkflowTemplateCategory.REMINDER_LIFECYCLE,
    supportedScopeTypes = listOf(WorkflowScopeType.GLOBAL, WorkflowScopeType.GROUP),
    trigger = WorkflowTrigger.ReminderAgeExceeded(days = 30),
    action = WorkflowAction.ArchiveReminder
  )
  private val scheduleBackgroundWorkUseCase = mockk<ScheduleBackgroundWorkUseCase>(relaxed = true)

  private fun useCase(ruleRepository: WorkflowRuleRepository, templateRepository: WorkflowTemplateRepository) =
    ApplyWorkflowTemplateUseCase(
      SaveWorkflowTemplateUseCase(templateRepository, scheduleBackgroundWorkUseCase),
      SaveWorkflowRuleUseCase(ruleRepository, scheduleBackgroundWorkUseCase)
    )

  @Test
  fun `creates a rule with the template's trigger and action at the given scope`() = runTest {
    val ruleRepository = RecordingWorkflowRuleRepository()
    val templateRepository = RecordingWorkflowTemplateRepository(template)

    val rule = useCase(ruleRepository, templateRepository)(template, WorkflowScope.ForGroup("group-1"))

    assertEquals(template.trigger, rule?.trigger)
    assertEquals(template.action, rule?.action)
    assertEquals(WorkflowScope.ForGroup("group-1"), rule?.scope)
    assertEquals(template.id, rule?.templateId)
    assertEquals(rule, ruleRepository.saved.singleOrNull())
  }

  @Test
  fun `increments the template's useCount`() = runTest {
    val ruleRepository = RecordingWorkflowRuleRepository()
    val templateRepository = RecordingWorkflowTemplateRepository(template)

    useCase(ruleRepository, templateRepository)(template, WorkflowScope.Global)

    assertEquals(1, templateRepository.saved.single().useCount)
  }

  @Test
  fun `returns null when the scope type is not supported by the template`() = runTest {
    val ruleRepository = RecordingWorkflowRuleRepository()
    val templateRepository = RecordingWorkflowTemplateRepository(template)

    val rule = useCase(ruleRepository, templateRepository)(template, WorkflowScope.ForReminder("reminder-1"))

    assertNull(rule)
    assertEquals(0, ruleRepository.saved.size)
  }
}

private class RecordingWorkflowRuleRepository(
  private val existing: List<WorkflowRule> = emptyList()
) : WorkflowRuleRepository {
  val saved = mutableListOf<WorkflowRule>()

  override suspend fun save(rule: WorkflowRule) { saved.add(rule) }
  override suspend fun getAll(): List<WorkflowRule> = existing + saved
  override suspend fun getEnabled(): List<WorkflowRule> = (existing + saved).filter { it.isEnabled }
  override suspend fun getById(id: String): WorkflowRule? = (existing + saved).firstOrNull { it.uuId == id }
  override suspend fun getByScope(scopeType: String, scopeId: String?): List<WorkflowRule> = emptyList()
  override suspend fun getByTriggerType(triggerType: String): List<WorkflowRule> = emptyList()
  override suspend fun delete(id: String) = Unit
  override suspend fun deleteAll() = Unit
  override suspend fun updateSyncState(id: String, state: SyncState) = Unit
  override suspend fun getIdsByState(syncStates: List<SyncState>): List<String> = emptyList()
  override suspend fun getAllIds(): List<String> = emptyList()
  override suspend fun countAll(): Int = existing.size + saved.size
}

private class RecordingWorkflowTemplateRepository(
  private vararg val existing: WorkflowTemplate
) : WorkflowTemplateRepository {
  val saved = mutableListOf<WorkflowTemplate>()

  override suspend fun save(template: WorkflowTemplate) { saved.add(template) }
  override suspend fun getAll(): List<WorkflowTemplate> = existing.toList() + saved
  override suspend fun getByCategory(category: WorkflowTemplateCategory): List<WorkflowTemplate> =
    (existing.toList() + saved).filter { it.category == category }
  override suspend fun getById(id: String): WorkflowTemplate? = (existing.toList() + saved).firstOrNull { it.id == id }
  override suspend fun delete(id: String) = Unit
  override suspend fun deleteAll() = Unit
  override suspend fun updateSyncState(id: String, state: SyncState) = Unit
  override suspend fun getIdsByState(syncStates: List<SyncState>): List<String> = emptyList()
  override suspend fun getAllIds(): List<String> = emptyList()
  override suspend fun countAll(): Int = existing.size + saved.size
}
