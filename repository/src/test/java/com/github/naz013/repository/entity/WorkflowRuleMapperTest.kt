package com.github.naz013.repository.entity

import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowTrigger
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class WorkflowRuleMapperTest {

  @Test
  fun `toEntity then toDomain round trips the built-in archive rule`() {
    val rule = WorkflowRule(
      uuId = "rule-1",
      title = "Archive completed reminders after 30 days",
      scope = WorkflowScope.Global,
      trigger = WorkflowTrigger.ReminderAgeExceeded(days = 30),
      action = WorkflowAction.ArchiveReminder,
      isEnabled = true,
      createdAt = LocalDateTime.of(2026, 1, 1, 0, 0)
    )

    val roundTripped = rule.toEntity().toDomain()

    assertEquals(rule, roundTripped)
  }

  @Test
  fun `toEntity then toDomain round trips a group-scoped snooze escalation rule`() {
    val rule = WorkflowRule(
      uuId = "rule-2",
      title = "Escalate work reminders snoozed 3 times",
      scope = WorkflowScope.ForGroup(groupId = "group-1"),
      trigger = WorkflowTrigger.ReminderSnoozedNTimes(count = 3),
      action = WorkflowAction.ApplyNotificationOverride(
        override = NotificationSettingsOverride(priority = ReminderPriority.HIGH, bypassDoNotDisturb = true)
      ),
      isEnabled = false,
      createdAt = LocalDateTime.of(2026, 3, 5, 8, 30),
      lastRunAt = LocalDateTime.of(2026, 3, 6, 9, 0),
      version = 2L,
      syncState = SyncState.Synced
    )

    val roundTripped = rule.toEntity().toDomain()

    assertEquals(rule, roundTripped)
  }

  @Test
  fun `toEntity then toDomain round trips a reminder-scoped chained-activation rule`() {
    val rule = WorkflowRule(
      uuId = "rule-3",
      scope = WorkflowScope.ForReminder(reminderId = "reminder-1"),
      trigger = WorkflowTrigger.ReminderCompleted,
      action = WorkflowAction.ActivateReminder(reminderId = "reminder-2"),
      createdAt = LocalDateTime.of(2026, 5, 1, 0, 0)
    )

    val roundTripped = rule.toEntity().toDomain()

    assertEquals(rule, roundTripped)
  }
}
