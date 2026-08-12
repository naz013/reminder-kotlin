package com.github.naz013.repository.entity

import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTemplateCategory
import com.github.naz013.domain.workflow.WorkflowTrigger
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDateTime

class WorkflowTemplateMapperTest {

  @Test
  fun `toEntity then toDomain round trips a built-in template supporting every scope type`() {
    val template = WorkflowTemplate(
      id = "template-1",
      title = "Archive completed reminders after 30 days",
      description = "Automatically archives a reminder once it's been completed for a while.",
      category = WorkflowTemplateCategory.REMINDER_LIFECYCLE,
      supportedScopeTypes = listOf(WorkflowScopeType.GLOBAL, WorkflowScopeType.GROUP, WorkflowScopeType.REMINDER),
      trigger = WorkflowTrigger.ReminderAgeExceeded(days = 30),
      action = WorkflowAction.ArchiveReminder,
      isBuiltIn = true,
      useCount = 4,
      createdAt = LocalDateTime.of(2026, 1, 1, 0, 0)
    )

    val roundTripped = template.toEntity().toDomain()

    assertEquals(template, roundTripped)
  }

  @Test
  fun `toEntity then toDomain round trips a user-created template restricted to one scope type`() {
    val template = WorkflowTemplate(
      id = "template-2",
      title = "Escalate on repeated snooze",
      description = null,
      category = WorkflowTemplateCategory.NOTIFICATION_ESCALATION,
      supportedScopeTypes = listOf(WorkflowScopeType.GROUP),
      trigger = WorkflowTrigger.ReminderSnoozedNTimes(count = 3),
      action = WorkflowAction.ApplyNotificationOverride(
        override = NotificationSettingsOverride(priority = ReminderPriority.HIGH, bypassDoNotDisturb = true)
      ),
      isBuiltIn = false,
      useCount = 0,
      createdAt = LocalDateTime.of(2026, 4, 10, 12, 0)
    )

    val roundTripped = template.toEntity().toDomain()

    assertEquals(template, roundTripped)
  }
}
