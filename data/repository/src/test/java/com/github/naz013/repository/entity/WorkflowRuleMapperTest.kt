package com.github.naz013.repository.entity

import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.ScheduleRecurrence
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowCondition
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
      templateId = "template-archive-30",
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

  @Test
  fun `toEntity then toDomain round trips a rule with multiple conditions`() {
    val rule = WorkflowRule(
      uuId = "rule-4",
      scope = WorkflowScope.Global,
      trigger = WorkflowTrigger.ReminderCompleted,
      conditions = listOf(
        WorkflowCondition.PriorityAtLeast(ReminderPriority.HIGH),
        WorkflowCondition.WithinTimeWindow(fromMinuteOfDay = 8 * 60, toMinuteOfDay = 22 * 60),
        WorkflowCondition.GroupIs(groupId = "group-1")
      ),
      action = WorkflowAction.ArchiveReminder,
      createdAt = LocalDateTime.of(2026, 6, 1, 0, 0)
    )

    val roundTripped = rule.toEntity().toDomain()

    assertEquals(rule, roundTripped)
  }

  @Test
  fun `toEntity then toDomain round trips a rule with no conditions`() {
    val rule = WorkflowRule(
      uuId = "rule-5",
      scope = WorkflowScope.Global,
      trigger = WorkflowTrigger.ReminderCompleted,
      action = WorkflowAction.ArchiveReminder,
      createdAt = LocalDateTime.of(2026, 6, 1, 0, 0)
    )

    val roundTripped = rule.toEntity().toDomain()

    assertEquals(rule, roundTripped)
    assertEquals(emptyList<WorkflowCondition>(), roundTripped.conditions)
  }

  @Test
  fun `toEntity then toDomain round trips a clear-notification-override rule`() {
    val rule = WorkflowRule(
      uuId = "rule-8",
      title = "Revert vacation-mode notification override",
      templateId = "pair-1",
      scope = WorkflowScope.ForGroup("group-1"),
      trigger = WorkflowTrigger.ScheduleReached(atDateTime = LocalDateTime.of(2026, 9, 1, 9, 0)),
      action = WorkflowAction.ClearNotificationOverride,
      createdAt = LocalDateTime.of(2026, 8, 1, 0, 0)
    )

    val roundTripped = rule.toEntity().toDomain()

    assertEquals(rule, roundTripped)
  }

  @Test
  fun `toEntity then toDomain round trips a purge-on-age rule`() {
    val rule = WorkflowRule(
      uuId = "rule-6",
      title = "Delete archived reminders after 90 days",
      scope = WorkflowScope.Global,
      trigger = WorkflowTrigger.ReminderAgeExceeded(days = 90),
      action = WorkflowAction.PurgeReminder,
      createdAt = LocalDateTime.of(2026, 7, 1, 0, 0)
    )

    val roundTripped = rule.toEntity().toDomain()

    assertEquals(rule, roundTripped)
  }

  @Test
  fun `toEntity then toDomain round trips a weekly schedule-reached rule`() {
    val rule = WorkflowRule(
      uuId = "rule-7",
      title = "Weekly reminder completion summary",
      scope = WorkflowScope.Global,
      trigger = WorkflowTrigger.ScheduleReached(
        atDateTime = LocalDateTime.of(2026, 8, 3, 9, 0),
        recurrence = ScheduleRecurrence.WEEKLY
      ),
      action = WorkflowAction.RunBackgroundTask(taskKey = "run_weekly_summary"),
      lastRunAt = LocalDateTime.of(2026, 8, 10, 9, 0),
      createdAt = LocalDateTime.of(2026, 7, 1, 0, 0)
    )

    val roundTripped = rule.toEntity().toDomain()

    assertEquals(rule, roundTripped)
  }

  @Test
  fun `toDomain falls back to ReminderCompleted when the trigger payload is malformed`() {
    val entity = legacyEntity(triggerType = "REMINDER_AGE_EXCEEDED", triggerPayload = "not-json")

    val trigger = entity.toDomain().trigger

    assertEquals(WorkflowTrigger.ReminderCompleted, trigger)
  }

  @Test
  fun `toDomain falls back to ArchiveReminder when the action payload is malformed`() {
    val entity = legacyEntity(actionType = "ACTIVATE_REMINDER", actionPayload = "not-json")

    val action = entity.toDomain().action

    assertEquals(WorkflowAction.ArchiveReminder, action)
  }

  @Test
  fun `toDomain drops a condition it can't parse instead of failing the whole list`() {
    val entity = legacyEntity(
      conditionsPayload = """[{"type":"WITHIN_TIME_WINDOW","payload":"not-json"},""" +
        """{"type":"GROUP_IS","payload":"{\"groupId\":\"group-1\"}"}]"""
    )

    val conditions = entity.toDomain().conditions

    assertEquals(listOf(WorkflowCondition.GroupIs(groupId = "group-1")), conditions)
  }

  private fun legacyEntity(
    triggerType: String = "REMINDER_COMPLETED",
    triggerPayload: String = "",
    actionType: String = "ARCHIVE_REMINDER",
    actionPayload: String = "",
    conditionsPayload: String = "[]"
  ) = WorkflowRuleEntity(
    uuId = "rule-legacy",
    scopeType = "GLOBAL",
    triggerType = triggerType,
    triggerPayload = triggerPayload,
    conditionsPayload = conditionsPayload,
    actionType = actionType,
    actionPayload = actionPayload,
    createdAt = 0L,
    syncState = SyncState.Synced.name
  )
}
