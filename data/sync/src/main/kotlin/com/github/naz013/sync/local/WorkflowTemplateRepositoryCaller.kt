package com.github.naz013.sync.local

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.repository.WorkflowTemplateRepository

internal class WorkflowTemplateRepositoryCaller(
  private val workflowTemplateRepository: WorkflowTemplateRepository
) : DataTypeRepositoryCaller<WorkflowTemplate> {

  override suspend fun getById(id: String): WorkflowTemplate? {
    return workflowTemplateRepository.getById(id)
  }

  override suspend fun getIdsByState(states: List<SyncState>): List<String> {
    return workflowTemplateRepository.getIdsByState(states)
  }

  override suspend fun updateSyncState(
    id: String,
    state: SyncState
  ) {
    workflowTemplateRepository.updateSyncState(id, state)
  }

  override suspend fun insertOrUpdate(item: Any) {
    if (item !is WorkflowTemplate) {
      throw IllegalArgumentException("Expected WorkflowTemplate type but got: ${item::class}")
    }
    workflowTemplateRepository.save(item.copy(syncState = SyncState.Synced))
  }

  override suspend fun getAllIds(): List<String> {
    return workflowTemplateRepository.getAllIds()
  }
}
