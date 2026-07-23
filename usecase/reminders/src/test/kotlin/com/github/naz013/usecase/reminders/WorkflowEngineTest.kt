package com.github.naz013.usecase.reminders

import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.WorkflowRuleRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime

class WorkflowEngineTest {

  private val now = LocalDateTime.of(2026, 6, 1, 0, 0)

  private fun completedReminder(
    id: String,
    updatedAt: LocalDateTime,
    groupId: String? = null
  ) = ReminderV2(
    uuId = id,
    groupId = groupId,
    schedule = ReminderSchedule(startDateTime = updatedAt, updatedAt = updatedAt),
    isActive = false,
    isRemoved = false
  )

  @Test
  fun `archives a completed reminder older than the cutoff`() = runTest {
    val old = completedReminder("old", updatedAt = now.minusDays(40))
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(old.uuId to old))
    val ruleRepository = FakeWorkflowRuleRepository(
      listOf(archiveRule(days = 30))
    )
    val engine = WorkflowEngine(ruleRepository, reminderRepository, NoOpGroupV2Repository())

    engine.runAgeBasedRules(now)

    assertTrue(reminderRepository.saved.getValue("old").isRemoved)
  }

  @Test
  fun `does not archive a completed reminder newer than the cutoff`() = runTest {
    val recent = completedReminder("recent", updatedAt = now.minusDays(2))
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(recent.uuId to recent))
    val ruleRepository = FakeWorkflowRuleRepository(listOf(archiveRule(days = 30)))
    val engine = WorkflowEngine(ruleRepository, reminderRepository, NoOpGroupV2Repository())

    engine.runAgeBasedRules(now)

    assertFalse(reminderRepository.saved.containsKey("recent"))
  }

  @Test
  fun `only archives reminders in the rule's group scope`() = runTest {
    val inScope = completedReminder("in-scope", updatedAt = now.minusDays(40), groupId = "group-1")
    val outOfScope = completedReminder("out-of-scope", updatedAt = now.minusDays(40), groupId = "group-2")
    val reminderRepository = FakeReminderV2Repository(
      mutableMapOf(inScope.uuId to inScope, outOfScope.uuId to outOfScope)
    )
    val ruleRepository = FakeWorkflowRuleRepository(
      listOf(archiveRule(days = 30, scope = WorkflowScope.ForGroup("group-1")))
    )
    val engine = WorkflowEngine(ruleRepository, reminderRepository, NoOpGroupV2Repository())

    engine.runAgeBasedRules(now)

    assertTrue(reminderRepository.saved.containsKey("in-scope"))
    assertFalse(reminderRepository.saved.containsKey("out-of-scope"))
  }

  @Test
  fun `skips disabled rules`() = runTest {
    val old = completedReminder("old", updatedAt = now.minusDays(40))
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(old.uuId to old))
    val ruleRepository = FakeWorkflowRuleRepository(
      listOf(archiveRule(days = 30).let { WorkflowRuleFixture.disabled(it) })
    )
    val engine = WorkflowEngine(ruleRepository, reminderRepository, NoOpGroupV2Repository())

    engine.runAgeBasedRules(now)

    assertEquals(0, reminderRepository.saved.size)
  }

  private fun archiveRule(days: Int, scope: WorkflowScope = WorkflowScope.Global) = WorkflowRule(
    uuId = "rule-${scope::class.simpleName}-$days",
    trigger = WorkflowTrigger.ReminderAgeExceeded(days = days),
    action = WorkflowAction.ArchiveReminder,
    scope = scope,
    createdAt = now
  )

  private fun groupCompletionRule(scope: WorkflowScope) = WorkflowRule(
    uuId = "rule-group-completion-${scope::class.simpleName}",
    trigger = WorkflowTrigger.GroupAllCompleted,
    action = WorkflowAction.ArchiveReminder,
    scope = scope,
    createdAt = now
  )

  @Test
  fun `archives every completed reminder once a group has no active reminders left`() = runTest {
    val completed = completedReminder("completed", updatedAt = now, groupId = "group-1")
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(completed.uuId to completed))
    val ruleRepository = FakeWorkflowRuleRepository(
      listOf(groupCompletionRule(WorkflowScope.ForGroup("group-1")))
    )
    val engine = WorkflowEngine(ruleRepository, reminderRepository, NoOpGroupV2Repository())

    engine.runGroupCompletionRules()

    assertTrue(reminderRepository.saved.getValue("completed").isRemoved)
  }

  @Test
  fun `does not archive a group's reminders while one is still active`() = runTest {
    val completed = completedReminder("completed", updatedAt = now, groupId = "group-1")
    val active = completed.copy(uuId = "active", isActive = true)
    val reminderRepository = FakeReminderV2Repository(
      mutableMapOf(completed.uuId to completed, active.uuId to active)
    )
    val ruleRepository = FakeWorkflowRuleRepository(
      listOf(groupCompletionRule(WorkflowScope.ForGroup("group-1")))
    )
    val engine = WorkflowEngine(ruleRepository, reminderRepository, NoOpGroupV2Repository())

    engine.runGroupCompletionRules()

    assertEquals(0, reminderRepository.saved.size)
  }

  @Test
  fun `skips group-completion rules that are not scoped to a group`() = runTest {
    val completed = completedReminder("completed", updatedAt = now, groupId = "group-1")
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(completed.uuId to completed))
    val ruleRepository = FakeWorkflowRuleRepository(
      listOf(groupCompletionRule(WorkflowScope.Global))
    )
    val engine = WorkflowEngine(ruleRepository, reminderRepository, NoOpGroupV2Repository())

    engine.runGroupCompletionRules()

    assertEquals(0, reminderRepository.saved.size)
  }
}

private object WorkflowRuleFixture {
  fun disabled(rule: WorkflowRule): WorkflowRule = rule.copy(isEnabled = false)
}

private class FakeReminderV2Repository(
  private val reminders: MutableMap<String, ReminderV2>
) : ReminderV2Repository {
  val saved = mutableMapOf<String, ReminderV2>()

  override suspend fun save(reminder: ReminderV2) {
    reminders[reminder.uuId] = reminder
    saved[reminder.uuId] = reminder
  }

  override suspend fun getById(id: String): ReminderV2? = reminders[id]
  override suspend fun getAll(): List<ReminderV2> = reminders.values.toList()
  override suspend fun getAll(active: Boolean, removed: Boolean): List<ReminderV2> =
    reminders.values.filter { it.isActive == active && it.isRemoved == removed }
  override suspend fun getActiveInRange(removed: Boolean, from: LocalDateTime, to: LocalDateTime): List<ReminderV2> = emptyList()
  override suspend fun getByGroupId(groupId: String): List<ReminderV2> = reminders.values.filter { it.groupId == groupId }
  override suspend fun countActiveByGroupId(groupId: String): Int =
    reminders.values.count { it.groupId == groupId && it.isActive && !it.isRemoved }
  override suspend fun getByNoteId(noteId: String): List<ReminderV2> = emptyList()
  override suspend fun search(query: String): List<ReminderV2> = emptyList()
  override suspend fun delete(id: String) { reminders.remove(id) }
  override suspend fun deleteAll(ids: List<String>) { ids.forEach { reminders.remove(it) } }
  override suspend fun deleteAll() { reminders.clear() }
  override suspend fun getIdsByState(syncStates: List<SyncState>): List<String> = emptyList()
  override suspend fun updateSyncState(id: String, state: SyncState) = Unit
  override suspend fun getAllIds(): List<String> = reminders.keys.toList()
}

private class NoOpGroupV2Repository : GroupV2Repository {
  override suspend fun save(group: GroupV2) = Unit
  override suspend fun saveAll(groups: List<GroupV2>) = Unit
  override suspend fun getAll(): List<GroupV2> = emptyList()
  override suspend fun getById(id: String): GroupV2? = null
  override suspend fun defaultGroup(isDef: Boolean): GroupV2? = null
  override suspend fun search(query: String): List<GroupV2> = emptyList()
  override suspend fun delete(id: String) = Unit
  override suspend fun deleteAll() = Unit
  override suspend fun updateSyncState(id: String, state: SyncState) = Unit
  override suspend fun getIdsByState(syncStates: List<SyncState>): List<String> = emptyList()
  override suspend fun getAllIds(): List<String> = emptyList()
  override suspend fun setDefaultGroup(id: String, isDef: Boolean) = Unit
  override suspend fun countAll(): Int = 0
}

private class FakeWorkflowRuleRepository(
  private val rules: List<WorkflowRule>
) : WorkflowRuleRepository {
  override suspend fun save(rule: WorkflowRule) = Unit
  override suspend fun getAll(): List<WorkflowRule> = rules
  override suspend fun getEnabled(): List<WorkflowRule> = rules.filter { it.isEnabled }
  override suspend fun getById(id: String): WorkflowRule? = rules.firstOrNull { it.uuId == id }
  override suspend fun getByScope(scopeType: String, scopeId: String?): List<WorkflowRule> = emptyList()
  override suspend fun getByTriggerType(triggerType: String): List<WorkflowRule> =
    rules.filter {
      when (triggerType) {
        "REMINDER_AGE_EXCEEDED" -> it.trigger is WorkflowTrigger.ReminderAgeExceeded
        "GROUP_ALL_COMPLETED" -> it.trigger is WorkflowTrigger.GroupAllCompleted
        else -> false
      }
    }
  override suspend fun delete(id: String) = Unit
  override suspend fun deleteAll() = Unit
  override suspend fun updateSyncState(id: String, state: SyncState) = Unit
  override suspend fun getIdsByState(syncStates: List<SyncState>): List<String> = emptyList()
  override suspend fun getAllIds(): List<String> = emptyList()
  override suspend fun countAll(): Int = rules.size
}
