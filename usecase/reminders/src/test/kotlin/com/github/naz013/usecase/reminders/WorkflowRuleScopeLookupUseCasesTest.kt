package com.github.naz013.usecase.reminders

import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.repository.WorkflowRuleRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkflowRuleScopeLookupUseCasesTest {

  private val globalRule = rule("global", WorkflowScope.Global)
  private val group1Rule = rule("group-1-rule", WorkflowScope.ForGroup("group-1"))
  private val group2Rule = rule("group-2-rule", WorkflowScope.ForGroup("group-2"))
  private val reminder1Rule = rule("reminder-1-rule", WorkflowScope.ForReminder("reminder-1"))
  private val reminder2Rule = rule("reminder-2-rule", WorkflowScope.ForReminder("reminder-2"))

  private val repository = ScopeFilteringWorkflowRuleRepository(
    listOf(globalRule, group1Rule, group2Rule, reminder1Rule, reminder2Rule)
  )

  @Test
  fun `returns only rules attached to the given reminder`() = runTest {
    val result = GetWorkflowRulesForReminderUseCase(repository)("reminder-1")

    assertEquals(listOf(reminder1Rule), result)
  }

  @Test
  fun `returns only rules attached to the given group`() = runTest {
    val result = GetWorkflowRulesForGroupUseCase(repository)("group-2")

    assertEquals(listOf(group2Rule), result)
  }

  @Test
  fun `returns only global rules`() = runTest {
    val result = GetGlobalWorkflowRulesUseCase(repository)()

    assertEquals(listOf(globalRule), result)
  }

  private fun rule(id: String, scope: WorkflowScope) = WorkflowRule(
    uuId = id,
    scope = scope,
    trigger = WorkflowTrigger.ReminderCompleted,
    action = WorkflowAction.CompleteReminder
  )
}

/** Filters by scope the same way the real WorkflowRuleDao.getByScope query does, so these tests
 * actually exercise the scopeType/scopeId matching rather than just echoing a stub. */
private class ScopeFilteringWorkflowRuleRepository(
  private val rules: List<WorkflowRule>
) : WorkflowRuleRepository {
  override suspend fun save(rule: WorkflowRule) = Unit
  override suspend fun getAll(): List<WorkflowRule> = rules
  override suspend fun getEnabled(): List<WorkflowRule> = rules.filter { it.isEnabled }
  override suspend fun getById(id: String): WorkflowRule? = rules.firstOrNull { it.uuId == id }

  override suspend fun getByScope(scopeType: String, scopeId: String?): List<WorkflowRule> =
    rules.filter { rule ->
      when (val scope = rule.scope) {
        is WorkflowScope.Global -> scopeType == "GLOBAL" && scopeId == null
        is WorkflowScope.ForGroup -> scopeType == "GROUP" && scope.groupId == scopeId
        is WorkflowScope.ForReminder -> scopeType == "REMINDER" && scope.reminderId == scopeId
      }
    }

  override suspend fun getByTriggerType(triggerType: String): List<WorkflowRule> = emptyList()
  override suspend fun delete(id: String) = Unit
  override suspend fun deleteAll() = Unit
  override suspend fun updateSyncState(id: String, state: SyncState) = Unit
  override suspend fun getIdsByState(syncStates: List<SyncState>): List<String> = emptyList()
  override suspend fun getAllIds(): List<String> = rules.map { it.uuId }
  override suspend fun countAll(): Int = rules.size
}
