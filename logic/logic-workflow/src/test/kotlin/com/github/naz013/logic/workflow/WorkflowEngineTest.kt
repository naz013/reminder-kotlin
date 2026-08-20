package com.github.naz013.logic.workflow

import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.reminder.v2.ReminderSchedule
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowCondition
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.workapi.ExistingWorkPolicy
import com.github.naz013.workapi.PeriodicWorkRequest
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkScheduler
import com.github.naz013.workapi.WorkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDateTime

class WorkflowEngineTest {

  private val now = LocalDateTime.of(2026, 6, 1, 0, 0)

  /** [WorkflowEngine.runUnacknowledgedRules] converts its `now` param from the system default
   * zone to UTC before comparing against `lastShownAt` (which is stored UTC, per the write side
   * in `ReminderActionProcessor`) - fixtures must be built relative to this same converted value,
   * not the raw [now], so the tests pass regardless of the host machine's time zone. */
  private val nowUtc = now.atZone(org.threeten.bp.ZoneId.systemDefault())
    .withZoneSameInstant(org.threeten.bp.ZoneOffset.UTC)
    .toLocalDateTime()

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

  private fun engine(
    ruleRepository: WorkflowRuleRepository,
    reminderRepository: ReminderV2Repository,
    workScheduler: WorkScheduler = FakeWorkScheduler()
  ) = WorkflowEngine(ruleRepository, reminderRepository, NoOpGroupV2Repository(), workScheduler)

  @Test
  fun `archives a completed reminder older than the cutoff`() = runTest {
    val old = completedReminder("old", updatedAt = now.minusDays(40))
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(old.uuId to old))
    val ruleRepository = FakeWorkflowRuleRepository(
      listOf(archiveRule(days = 30))
    )
    val result = engine(ruleRepository, reminderRepository).runAgeBasedRules(now)

    assertTrue(reminderRepository.saved.getValue("old").isRemoved)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `does not archive a completed reminder newer than the cutoff`() = runTest {
    val recent = completedReminder("recent", updatedAt = now.minusDays(2))
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(recent.uuId to recent))
    val ruleRepository = FakeWorkflowRuleRepository(listOf(archiveRule(days = 30)))

    engine(ruleRepository, reminderRepository).runAgeBasedRules(now)

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

    engine(ruleRepository, reminderRepository).runAgeBasedRules(now)

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

    engine(ruleRepository, reminderRepository).runAgeBasedRules(now)

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

    engine(ruleRepository, reminderRepository).runGroupCompletionRules()

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

    engine(ruleRepository, reminderRepository).runGroupCompletionRules()

    assertEquals(0, reminderRepository.saved.size)
  }

  @Test
  fun `skips group-completion rules that are not scoped to a group`() = runTest {
    val completed = completedReminder("completed", updatedAt = now, groupId = "group-1")
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(completed.uuId to completed))
    val ruleRepository = FakeWorkflowRuleRepository(
      listOf(groupCompletionRule(WorkflowScope.Global))
    )

    engine(ruleRepository, reminderRepository).runGroupCompletionRules()

    assertEquals(0, reminderRepository.saved.size)
  }

  @Test
  fun `applies a notification override inline without returning a pending action`() = runTest {
    val reminder = completedReminder("r1", updatedAt = now)
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(reminder.uuId to reminder))
    val override = NotificationSettingsOverride(priority = ReminderPriority.HIGH)
    val rule = WorkflowRule(
      uuId = "rule-notify",
      trigger = WorkflowTrigger.ReminderCompleted,
      action = WorkflowAction.ApplyNotificationOverride(override),
      scope = WorkflowScope.Global,
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(rule))

    val result = engine(ruleRepository, reminderRepository).runReminderCompletedRules("r1")

    assertEquals(override, reminderRepository.saved.getValue("r1").notification)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `enqueues a background task inline without returning a pending action`() = runTest {
    val reminder = completedReminder("r1", updatedAt = now)
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(reminder.uuId to reminder))
    val rule = WorkflowRule(
      uuId = "rule-task",
      trigger = WorkflowTrigger.ReminderCompleted,
      action = WorkflowAction.RunBackgroundTask("some_task"),
      scope = WorkflowScope.Global,
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(rule))
    val workScheduler = FakeWorkScheduler()

    val result = engine(ruleRepository, reminderRepository, workScheduler).runReminderCompletedRules("r1")

    assertEquals(1, workScheduler.enqueued.size)
    assertEquals("some_task", workScheduler.enqueued.single().taskKey)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `returns a pending action for CompleteReminder`() = runTest {
    val reminder = completedReminder("r1", updatedAt = now)
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(reminder.uuId to reminder))
    val rule = WorkflowRule(
      uuId = "rule-complete",
      trigger = WorkflowTrigger.ReminderCompleted,
      action = WorkflowAction.CompleteReminder,
      scope = WorkflowScope.Global,
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(rule))

    val result = engine(ruleRepository, reminderRepository).runReminderCompletedRules("r1")

    assertEquals(1, result.size)
    assertEquals(WorkflowAction.CompleteReminder, result.single().action)
    assertEquals("r1", result.single().contextReminderId)
  }

  @Test
  fun `returns a pending action for ActivateReminder with its own target id, distinct from the trigger`() = runTest {
    val reminder = completedReminder("r1", updatedAt = now)
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(reminder.uuId to reminder))
    val rule = WorkflowRule(
      uuId = "rule-activate",
      trigger = WorkflowTrigger.ReminderCompleted,
      action = WorkflowAction.ActivateReminder(reminderId = "other-reminder"),
      scope = WorkflowScope.Global,
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(rule))

    val result = engine(ruleRepository, reminderRepository).runReminderCompletedRules("r1")

    assertEquals(1, result.size)
    assertEquals(WorkflowAction.ActivateReminder("other-reminder"), result.single().action)
    assertEquals("r1", result.single().contextReminderId)
  }

  @Test
  fun `returns nothing for a missing reminder id`() = runTest {
    val reminderRepository = FakeReminderV2Repository(mutableMapOf())
    val ruleRepository = FakeWorkflowRuleRepository(
      listOf(
        WorkflowRule(
          uuId = "rule-complete",
          trigger = WorkflowTrigger.ReminderCompleted,
          action = WorkflowAction.CompleteReminder,
          scope = WorkflowScope.Global,
          createdAt = now
        )
      )
    )

    val result = engine(ruleRepository, reminderRepository).runReminderCompletedRules("missing")

    assertTrue(result.isEmpty())
  }

  @Test
  fun `fires a snooze-count rule only at the exact matching count`() = runTest {
    val snoozedTwice = ReminderV2(
      uuId = "r1",
      schedule = ReminderSchedule(startDateTime = now),
      snoozeCount = 2
    )
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(snoozedTwice.uuId to snoozedTwice))
    val rule = WorkflowRule(
      uuId = "rule-snooze",
      trigger = WorkflowTrigger.ReminderSnoozedNTimes(count = 2),
      action = WorkflowAction.ArchiveReminder,
      scope = WorkflowScope.Global,
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(rule))

    val result = engine(ruleRepository, reminderRepository).runSnoozeCountRules("r1")

    assertTrue(reminderRepository.saved.getValue("r1").isRemoved)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `does not fire a snooze-count rule before its threshold`() = runTest {
    val snoozedOnce = ReminderV2(
      uuId = "r1",
      schedule = ReminderSchedule(startDateTime = now),
      snoozeCount = 1
    )
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(snoozedOnce.uuId to snoozedOnce))
    val rule = WorkflowRule(
      uuId = "rule-snooze",
      trigger = WorkflowTrigger.ReminderSnoozedNTimes(count = 2),
      action = WorkflowAction.ArchiveReminder,
      scope = WorkflowScope.Global,
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(rule))

    engine(ruleRepository, reminderRepository).runSnoozeCountRules("r1")

    assertEquals(0, reminderRepository.saved.size)
  }

  @Test
  fun `does not re-fire a snooze-count rule past its threshold`() = runTest {
    val snoozedFiveTimes = ReminderV2(
      uuId = "r1",
      schedule = ReminderSchedule(startDateTime = now),
      snoozeCount = 5
    )
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(snoozedFiveTimes.uuId to snoozedFiveTimes))
    val rule = WorkflowRule(
      uuId = "rule-snooze",
      trigger = WorkflowTrigger.ReminderSnoozedNTimes(count = 2),
      action = WorkflowAction.ArchiveReminder,
      scope = WorkflowScope.Global,
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(rule))

    engine(ruleRepository, reminderRepository).runSnoozeCountRules("r1")

    assertEquals(0, reminderRepository.saved.size)
  }

  @Test
  fun `fires a location-entered rule scoped to the exact reminder`() = runTest {
    val reminder = completedReminder("r1", updatedAt = now)
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(reminder.uuId to reminder))
    val rule = WorkflowRule(
      uuId = "rule-loc",
      trigger = WorkflowTrigger.LocationEntered,
      action = WorkflowAction.ArchiveReminder,
      scope = WorkflowScope.ForReminder("r1"),
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(rule))

    engine(ruleRepository, reminderRepository).runLocationEnteredRules("r1")

    assertTrue(reminderRepository.saved.getValue("r1").isRemoved)
  }

  @Test
  fun `skips location rules scoped globally or to a group`() = runTest {
    val reminder = completedReminder("r1", updatedAt = now)
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(reminder.uuId to reminder))
    val globalRule = WorkflowRule(
      uuId = "rule-loc-global",
      trigger = WorkflowTrigger.LocationEntered,
      action = WorkflowAction.ArchiveReminder,
      scope = WorkflowScope.Global,
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(globalRule))

    engine(ruleRepository, reminderRepository).runLocationEnteredRules("r1")

    assertEquals(0, reminderRepository.saved.size)
  }

  @Test
  fun `fires a location-exited rule for the matching reminder`() = runTest {
    val reminder = completedReminder("r1", updatedAt = now)
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(reminder.uuId to reminder))
    val rule = WorkflowRule(
      uuId = "rule-loc-exit",
      trigger = WorkflowTrigger.LocationExited,
      action = WorkflowAction.ArchiveReminder,
      scope = WorkflowScope.ForReminder("r1"),
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(rule))

    engine(ruleRepository, reminderRepository).runLocationExitedRules("r1")

    assertTrue(reminderRepository.saved.getValue("r1").isRemoved)
  }

  @Test
  fun `fires an unacknowledged rule once the threshold has elapsed`() = runTest {
    val unacknowledged = ReminderV2(
      uuId = "r1",
      schedule = ReminderSchedule(startDateTime = now),
      isActive = true,
      lastShownAt = nowUtc.minusMinutes(30)
    )
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(unacknowledged.uuId to unacknowledged))
    val rule = WorkflowRule(
      uuId = "rule-unack",
      trigger = WorkflowTrigger.ReminderUnacknowledgedFor(minutes = 20),
      action = WorkflowAction.ArchiveReminder,
      scope = WorkflowScope.Global,
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(rule))

    engine(ruleRepository, reminderRepository).runUnacknowledgedRules(now)

    assertTrue(reminderRepository.saved.getValue("r1").isRemoved)
  }

  @Test
  fun `does not fire an unacknowledged rule before its threshold elapses`() = runTest {
    val recentlyShown = ReminderV2(
      uuId = "r1",
      schedule = ReminderSchedule(startDateTime = now),
      isActive = true,
      lastShownAt = nowUtc.minusMinutes(5)
    )
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(recentlyShown.uuId to recentlyShown))
    val rule = WorkflowRule(
      uuId = "rule-unack",
      trigger = WorkflowTrigger.ReminderUnacknowledgedFor(minutes = 20),
      action = WorkflowAction.ArchiveReminder,
      scope = WorkflowScope.Global,
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(rule))

    engine(ruleRepository, reminderRepository).runUnacknowledgedRules(now)

    assertEquals(0, reminderRepository.saved.size)
  }

  @Test
  fun `skips reminders that have not been shown yet for unacknowledged rules`() = runTest {
    val neverShown = ReminderV2(
      uuId = "r1",
      schedule = ReminderSchedule(startDateTime = now),
      isActive = true,
      lastShownAt = null
    )
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(neverShown.uuId to neverShown))
    val rule = WorkflowRule(
      uuId = "rule-unack",
      trigger = WorkflowTrigger.ReminderUnacknowledgedFor(minutes = 20),
      action = WorkflowAction.ArchiveReminder,
      scope = WorkflowScope.Global,
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(rule))

    engine(ruleRepository, reminderRepository).runUnacknowledgedRules(now)

    assertEquals(0, reminderRepository.saved.size)
  }

  @Test
  fun `does not fire a rule when a PriorityAtLeast condition is not met`() = runTest {
    val lowPriorityReminder = ReminderV2(
      uuId = "r1",
      schedule = ReminderSchedule(startDateTime = now),
      notification = NotificationSettingsOverride(priority = ReminderPriority.LOW),
    )
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(lowPriorityReminder.uuId to lowPriorityReminder))
    val rule = WorkflowRule(
      uuId = "rule-cond",
      trigger = WorkflowTrigger.ReminderCompleted,
      conditions = listOf(WorkflowCondition.PriorityAtLeast(ReminderPriority.HIGH)),
      action = WorkflowAction.ArchiveReminder,
      scope = WorkflowScope.Global,
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(rule))

    engine(ruleRepository, reminderRepository).runReminderCompletedRules("r1")

    assertEquals(0, reminderRepository.saved.size)
  }

  @Test
  fun `fires a rule when a PriorityAtLeast condition is met`() = runTest {
    val highPriorityReminder = ReminderV2(
      uuId = "r1",
      schedule = ReminderSchedule(startDateTime = now),
      notification = NotificationSettingsOverride(priority = ReminderPriority.HIGHEST),
    )
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(highPriorityReminder.uuId to highPriorityReminder))
    val rule = WorkflowRule(
      uuId = "rule-cond",
      trigger = WorkflowTrigger.ReminderCompleted,
      conditions = listOf(WorkflowCondition.PriorityAtLeast(ReminderPriority.HIGH)),
      action = WorkflowAction.ArchiveReminder,
      scope = WorkflowScope.Global,
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(rule))

    engine(ruleRepository, reminderRepository).runReminderCompletedRules("r1")

    assertTrue(reminderRepository.saved.getValue("r1").isRemoved)
  }

  @Test
  fun `does not fire a rule when a GroupIs condition does not match`() = runTest {
    val reminder = completedReminder("r1", updatedAt = now, groupId = "group-2")
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(reminder.uuId to reminder))
    val rule = WorkflowRule(
      uuId = "rule-cond",
      trigger = WorkflowTrigger.ReminderCompleted,
      conditions = listOf(WorkflowCondition.GroupIs("group-1")),
      action = WorkflowAction.ArchiveReminder,
      scope = WorkflowScope.Global,
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(rule))

    engine(ruleRepository, reminderRepository).runReminderCompletedRules("r1")

    assertEquals(0, reminderRepository.saved.size)
  }

  @Test
  fun `fires a rule when the current time is within a WithinTimeWindow condition`() = runTest {
    val reminder = completedReminder("r1", updatedAt = now)
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(reminder.uuId to reminder))
    val rule = WorkflowRule(
      uuId = "rule-cond",
      trigger = WorkflowTrigger.ReminderCompleted,
      conditions = listOf(WorkflowCondition.WithinTimeWindow(fromMinuteOfDay = 0, toMinuteOfDay = 23 * 60 + 59)),
      action = WorkflowAction.ArchiveReminder,
      scope = WorkflowScope.Global,
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(rule))

    engine(ruleRepository, reminderRepository).runReminderCompletedRules("r1", now)

    assertTrue(reminderRepository.saved.getValue("r1").isRemoved)
  }

  @Test
  fun `does not fire a rule when the current time is outside a WithinTimeWindow condition`() = runTest {
    val reminder = completedReminder("r1", updatedAt = now)
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(reminder.uuId to reminder))
    // `now` is midnight (00:00) - a window entirely within business hours must exclude it.
    val rule = WorkflowRule(
      uuId = "rule-cond",
      trigger = WorkflowTrigger.ReminderCompleted,
      conditions = listOf(WorkflowCondition.WithinTimeWindow(fromMinuteOfDay = 9 * 60, toMinuteOfDay = 17 * 60)),
      action = WorkflowAction.ArchiveReminder,
      scope = WorkflowScope.Global,
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(rule))

    engine(ruleRepository, reminderRepository).runReminderCompletedRules("r1", now)

    assertEquals(0, reminderRepository.saved.size)
  }

  @Test
  fun `requires every condition to hold - an AND chain, not any one of them`() = runTest {
    val reminder = completedReminder("r1", updatedAt = now, groupId = "group-1").copy(
      notification = NotificationSettingsOverride(priority = ReminderPriority.HIGHEST)
    )
    val reminderRepository = FakeReminderV2Repository(mutableMapOf(reminder.uuId to reminder))
    val rule = WorkflowRule(
      uuId = "rule-cond",
      trigger = WorkflowTrigger.ReminderCompleted,
      conditions = listOf(
        WorkflowCondition.PriorityAtLeast(ReminderPriority.HIGH),
        WorkflowCondition.GroupIs("group-2"),
      ),
      action = WorkflowAction.ArchiveReminder,
      scope = WorkflowScope.Global,
      createdAt = now
    )
    val ruleRepository = FakeWorkflowRuleRepository(listOf(rule))

    engine(ruleRepository, reminderRepository).runReminderCompletedRules("r1")

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

  override suspend fun count(active: Boolean, removed: Boolean): Int = getAll(active, removed).size
  override suspend fun getByRemovedStatus(removed: Boolean): List<ReminderV2> =
    reminders.values.filter { it.isRemoved == removed }
  override suspend fun getActiveInRange(
    removed: Boolean,
    from: LocalDateTime,
    to: LocalDateTime
  ): List<ReminderV2> = emptyList()
  override fun observeActiveInRange(
    removed: Boolean,
    from: LocalDateTime,
    to: LocalDateTime
  ): Flow<List<ReminderV2>> = emptyFlow()
  override suspend fun getByGroupId(groupId: String): List<ReminderV2> =
    reminders.values.filter { it.groupId == groupId }
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
  override suspend fun clearGroupId(groupId: String) = Unit
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
        "REMINDER_COMPLETED" -> it.trigger is WorkflowTrigger.ReminderCompleted
        "REMINDER_SNOOZED_N_TIMES" -> it.trigger is WorkflowTrigger.ReminderSnoozedNTimes
        "GROUP_ALL_COMPLETED" -> it.trigger is WorkflowTrigger.GroupAllCompleted
        "LOCATION_ENTERED" -> it.trigger is WorkflowTrigger.LocationEntered
        "LOCATION_EXITED" -> it.trigger is WorkflowTrigger.LocationExited
        "REMINDER_AGE_EXCEEDED" -> it.trigger is WorkflowTrigger.ReminderAgeExceeded
        "REMINDER_UNACKNOWLEDGED_FOR" -> it.trigger is WorkflowTrigger.ReminderUnacknowledgedFor
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

private class FakeWorkScheduler : WorkScheduler {
  val enqueued = mutableListOf<WorkRequest>()

  override fun enqueue(request: WorkRequest): String {
    enqueued.add(request)
    return request.tag
  }

  override fun enqueueUnique(uniqueName: String, policy: ExistingWorkPolicy, request: WorkRequest): String {
    enqueued.add(request)
    return uniqueName
  }

  override fun enqueuePeriodic(request: PeriodicWorkRequest): String = request.tag
  override fun cancelByTag(tag: String) = Unit
  override fun cancelUniqueWork(uniqueName: String) = Unit
  override fun observeUniqueWork(uniqueName: String) = emptyFlow<WorkState>()
}
