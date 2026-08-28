package com.github.naz013.files

import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowCondition
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTemplateCategory
import com.github.naz013.domain.workflow.WorkflowTrigger
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

/**
 * Covers [WorkflowRule]/[WorkflowTemplate] <-> [WorkflowRuleJson]/[WorkflowTemplateJson] mapping
 * directly (not through [DataConverterImpl.toOutputStream]/[toData]) - those go through
 * `android.util.Base64OutputStream`, which throws under plain JUnit and isn't fully covered by
 * Robolectric's shadow either (see the `@Ignore`d `SyncDataConverterImplSettingsTest` in `app`).
 * What's under test here - the trigger/condition/action/scope wire encoding this module owns - is
 * exactly the part that doesn't depend on Android at all.
 */
class DataConverterImplTest {

  @Test
  fun `round trips a global workflow rule with a payload trigger, condition and action`() {
    val rule = WorkflowRule(
      uuId = "rule-1",
      title = "Escalate after 3 snoozes",
      templateId = "template-1",
      scope = WorkflowScope.Global,
      trigger = WorkflowTrigger.ReminderSnoozedNTimes(count = 3),
      conditions = listOf(WorkflowCondition.PriorityAtLeast(priority = ReminderPriority.HIGH)),
      action = WorkflowAction.ApplyNotificationOverride(
        override = NotificationSettingsOverride(bypassDoNotDisturb = true)
      ),
      isEnabled = true,
      createdAt = LocalDateTime.of(2026, 1, 1, 9, 0),
      lastRunAt = LocalDateTime.of(2026, 1, 2, 9, 0),
      version = 4L
    )

    val result = rule.toJson().toDomain()

    assertEquals(rule.copy(syncState = SyncState.Synced), result)
  }

  @Test
  fun `round trips a group-scoped workflow rule with a no-payload trigger and action`() {
    val rule = WorkflowRule(
      uuId = "rule-2",
      scope = WorkflowScope.ForGroup(groupId = "group-1"),
      trigger = WorkflowTrigger.GroupAllCompleted,
      action = WorkflowAction.ArchiveReminder
    )

    val result = rule.toJson().toDomain()

    assertEquals(rule.copy(syncState = SyncState.Synced), result)
  }

  @Test
  fun `round trips a reminder-scoped workflow rule with multiple conditions`() {
    val rule = WorkflowRule(
      uuId = "rule-3",
      scope = WorkflowScope.ForReminder(reminderId = "reminder-1"),
      trigger = WorkflowTrigger.LocationEntered,
      conditions = listOf(
        WorkflowCondition.WithinTimeWindow(fromMinuteOfDay = 480, toMinuteOfDay = 1020),
        WorkflowCondition.GroupIs(groupId = "group-2")
      ),
      action = WorkflowAction.CompleteReminder
    )

    val result = rule.toJson().toDomain()

    assertEquals(rule.copy(syncState = SyncState.Synced), result)
  }

  @Test
  fun `falls back to a safe default when the trigger payload is unparseable`() {
    val rule = WorkflowRule(
      uuId = "rule-4",
      trigger = WorkflowTrigger.ReminderSnoozedNTimes(count = 5),
      action = WorkflowAction.ArchiveReminder
    )
    val corruptJson = rule.toJson().copy(triggerPayload = "not json")

    val result = corruptJson.toDomain()

    assertEquals(WorkflowTrigger.ReminderCompleted, result.trigger)
  }

  @Test
  fun `round trips a workflow template`() {
    val template = WorkflowTemplate(
      id = "template-1",
      title = "Archive after 30 days",
      description = "Automatically archives a reminder once it's been completed for a while.",
      category = WorkflowTemplateCategory.REMINDER_LIFECYCLE,
      supportedScopeTypes = listOf(WorkflowScopeType.GLOBAL, WorkflowScopeType.GROUP),
      trigger = WorkflowTrigger.ReminderAgeExceeded(days = 30),
      action = WorkflowAction.ArchiveReminder,
      isBuiltIn = true,
      useCount = 7,
      createdAt = LocalDateTime.of(2026, 1, 1, 9, 0),
      version = 2L
    )

    val result = template.toJson().toDomain()

    assertEquals(template.copy(syncState = SyncState.Synced), result)
  }
}
