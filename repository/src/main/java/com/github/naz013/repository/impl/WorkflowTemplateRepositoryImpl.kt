package com.github.naz013.repository.impl

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTemplateCategory
import com.github.naz013.logging.Logger
import com.github.naz013.repository.WorkflowTemplateRepository
import com.github.naz013.repository.dao.WorkflowTemplateDao
import com.github.naz013.repository.entity.toDomain
import com.github.naz013.repository.entity.toEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table

internal class WorkflowTemplateRepositoryImpl(
  private val dao: WorkflowTemplateDao,
  private val tableChangeNotifier: TableChangeNotifier
) : WorkflowTemplateRepository {

  private val table = Table.WorkflowTemplate

  override suspend fun save(template: WorkflowTemplate) {
    Logger.d(TAG, "Save workflow template: ${template.id}")
    dao.insert(template.toEntity())
    tableChangeNotifier.notify(table)
  }

  override suspend fun getAll(): List<WorkflowTemplate> {
    Logger.d(TAG, "Get all workflow templates")
    return dao.getAll().map { it.toDomain() }
  }

  override suspend fun getByCategory(category: WorkflowTemplateCategory): List<WorkflowTemplate> {
    Logger.d(TAG, "Get workflow templates by category: $category")
    return dao.getByCategory(category.name).map { it.toDomain() }
  }

  override suspend fun getById(id: String): WorkflowTemplate? {
    Logger.d(TAG, "Get workflow template by id: $id")
    return dao.getById(id)?.toDomain()
  }

  override suspend fun delete(id: String) {
    Logger.d(TAG, "Delete workflow template by id: $id")
    dao.delete(id)
    tableChangeNotifier.notify(table)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all workflow templates")
    dao.deleteAll()
    tableChangeNotifier.notify(table)
  }

  override suspend fun updateSyncState(id: String, state: SyncState) {
    Logger.d(TAG, "Update workflow template sync state: $id to $state")
    dao.updateSyncState(id, state.name)
    tableChangeNotifier.notify(table)
  }

  override suspend fun getIdsByState(syncStates: List<SyncState>): List<String> {
    Logger.d(TAG, "Get workflow template ids by sync states: $syncStates")
    return dao.getBySyncStates(syncStates.map { it.name })
  }

  override suspend fun getAllIds(): List<String> {
    Logger.d(TAG, "Get all workflow template ids")
    return dao.getAllIds()
  }

  override suspend fun countAll(): Int {
    Logger.d(TAG, "Count all workflow templates")
    return dao.countAll()
  }

  companion object {
    private const val TAG = "WorkflowTemplateRepository"
  }
}
