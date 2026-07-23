package com.github.naz013.usecase.reminders

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTemplateCategory
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.repository.WorkflowTemplateRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetWorkflowTemplatesUseCaseTest {

  @Test
  fun `returns every template from the repository`() = runTest {
    val templates = listOf(
      WorkflowTemplate(
        id = "1",
        title = "Archive after 30 days",
        category = WorkflowTemplateCategory.REMINDER_LIFECYCLE,
        trigger = WorkflowTrigger.ReminderAgeExceeded(days = 30),
        action = WorkflowAction.ArchiveReminder
      ),
      WorkflowTemplate(
        id = "2",
        title = "Archive group on completion",
        category = WorkflowTemplateCategory.GROUP,
        trigger = WorkflowTrigger.GroupAllCompleted,
        action = WorkflowAction.ArchiveReminder
      )
    )
    val repository = StubWorkflowTemplateRepository(templates)
    val useCase = GetWorkflowTemplatesUseCase(repository)

    val result = useCase()

    assertEquals(templates, result)
  }
}

private class StubWorkflowTemplateRepository(
  private val templates: List<WorkflowTemplate>
) : WorkflowTemplateRepository {
  override suspend fun save(template: WorkflowTemplate) = Unit
  override suspend fun getAll(): List<WorkflowTemplate> = templates
  override suspend fun getByCategory(category: WorkflowTemplateCategory): List<WorkflowTemplate> =
    templates.filter { it.category == category }
  override suspend fun getById(id: String): WorkflowTemplate? = templates.firstOrNull { it.id == id }
  override suspend fun delete(id: String) = Unit
  override suspend fun deleteAll() = Unit
  override suspend fun updateSyncState(id: String, state: SyncState) = Unit
  override suspend fun getIdsByState(syncStates: List<SyncState>): List<String> = emptyList()
  override suspend fun getAllIds(): List<String> = emptyList()
  override suspend fun countAll(): Int = templates.size
}
