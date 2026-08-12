package com.github.naz013.repository.impl

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.logging.Logger
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.repository.dao.WorkflowRuleDao
import com.github.naz013.repository.entity.toDomain
import com.github.naz013.repository.entity.toEntity
import com.github.naz013.repository.observer.TableChangeNotifier
import com.github.naz013.repository.table.Table

internal class WorkflowRuleRepositoryImpl(
  private val dao: WorkflowRuleDao,
  private val tableChangeNotifier: TableChangeNotifier
) : WorkflowRuleRepository {

  private val table = Table.WorkflowRule

  override suspend fun save(rule: WorkflowRule) {
    Logger.d(TAG, "Save workflow rule: ${rule.uuId}")
    dao.insert(rule.toEntity())
    tableChangeNotifier.notify(table)
  }

  override suspend fun getAll(): List<WorkflowRule> {
    Logger.d(TAG, "Get all workflow rules")
    return dao.getAll().map { it.toDomain() }
  }

  override suspend fun getEnabled(): List<WorkflowRule> {
    Logger.d(TAG, "Get enabled workflow rules")
    return dao.getEnabled().map { it.toDomain() }
  }

  override suspend fun getById(id: String): WorkflowRule? {
    Logger.d(TAG, "Get workflow rule by id: $id")
    return dao.getById(id)?.toDomain()
  }

  override suspend fun getByScope(scopeType: String, scopeId: String?): List<WorkflowRule> {
    Logger.d(TAG, "Get workflow rules by scope: $scopeType/$scopeId")
    return dao.getByScope(scopeType, scopeId).map { it.toDomain() }
  }

  override suspend fun getByTriggerType(triggerType: String): List<WorkflowRule> {
    Logger.d(TAG, "Get workflow rules by trigger type: $triggerType")
    return dao.getByTriggerType(triggerType).map { it.toDomain() }
  }

  override suspend fun delete(id: String) {
    Logger.d(TAG, "Delete workflow rule by id: $id")
    dao.delete(id)
    tableChangeNotifier.notify(table)
  }

  override suspend fun deleteAll() {
    Logger.d(TAG, "Delete all workflow rules")
    dao.deleteAll()
    tableChangeNotifier.notify(table)
  }

  override suspend fun updateSyncState(id: String, state: SyncState) {
    Logger.d(TAG, "Update workflow rule sync state: $id to $state")
    dao.updateSyncState(id, state.name)
    tableChangeNotifier.notify(table)
  }

  override suspend fun getIdsByState(syncStates: List<SyncState>): List<String> {
    Logger.d(TAG, "Get workflow rule ids by sync states: $syncStates")
    return dao.getBySyncStates(syncStates.map { it.name })
  }

  override suspend fun getAllIds(): List<String> {
    Logger.d(TAG, "Get all workflow rule ids")
    return dao.getAllIds()
  }

  override suspend fun countAll(): Int {
    Logger.d(TAG, "Count all workflow rules")
    return dao.countAll()
  }

  companion object {
    private const val TAG = "WorkflowRuleRepository"
  }
}
