package com.github.naz013.sync.local

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.repository.WorkflowRuleRepository

internal class WorkflowRuleRepositoryCaller(
  private val workflowRuleRepository: WorkflowRuleRepository
) : DataTypeRepositoryCaller<WorkflowRule> {

  override suspend fun getById(id: String): WorkflowRule? {
    return workflowRuleRepository.getById(id)
  }

  override suspend fun getIdsByState(states: List<SyncState>): List<String> {
    return workflowRuleRepository.getIdsByState(states)
  }

  override suspend fun updateSyncState(
    id: String,
    state: SyncState
  ) {
    workflowRuleRepository.updateSyncState(id, state)
  }

  override suspend fun insertOrUpdate(item: Any) {
    if (item !is WorkflowRule) {
      throw IllegalArgumentException("Expected WorkflowRule type but got: ${item::class}")
    }
    workflowRuleRepository.save(item.copy(syncState = SyncState.Synced))
  }

  override suspend fun getAllIds(): List<String> {
    return workflowRuleRepository.getAllIds()
  }
}
