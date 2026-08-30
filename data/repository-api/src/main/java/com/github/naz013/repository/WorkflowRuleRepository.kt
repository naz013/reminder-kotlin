package com.github.naz013.repository

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowRule
import kotlinx.coroutines.flow.Flow

interface WorkflowRuleRepository {
  suspend fun save(rule: WorkflowRule)

  suspend fun getAll(): List<WorkflowRule>
  suspend fun getEnabled(): List<WorkflowRule>
  suspend fun getById(id: String): WorkflowRule?
  suspend fun getByScope(scopeType: String, scopeId: String?): List<WorkflowRule>
  fun observeByScope(scopeType: String, scopeId: String?): Flow<List<WorkflowRule>>
  suspend fun getByTriggerType(triggerType: String): List<WorkflowRule>

  suspend fun delete(id: String)
  suspend fun deleteAll()

  suspend fun updateSyncState(id: String, state: SyncState)
  suspend fun getIdsByState(syncStates: List<SyncState>): List<String>
  suspend fun getAllIds(): List<String>

  suspend fun countAll(): Int
}
