package com.github.naz013.repository

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTemplateCategory

interface WorkflowTemplateRepository {
  suspend fun save(template: WorkflowTemplate)

  suspend fun getAll(): List<WorkflowTemplate>
  suspend fun getByCategory(category: WorkflowTemplateCategory): List<WorkflowTemplate>
  suspend fun getById(id: String): WorkflowTemplate?

  suspend fun delete(id: String)
  suspend fun deleteAll()

  suspend fun updateSyncState(id: String, state: SyncState)
  suspend fun getIdsByState(syncStates: List<SyncState>): List<String>
  suspend fun getAllIds(): List<String>

  suspend fun countAll(): Int
}
