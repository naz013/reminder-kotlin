package com.github.naz013.feature.workflow

import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTemplateCategory
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.logic.workflow.ApplyWorkflowTemplateUseCase
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.repository.WorkflowTemplateRepository

/**
 * Seeds the built-in workflow template gallery on first run, and applies the archive template
 * globally to create the one live rule [com.github.naz013.logic.workflow.WorkflowEngine]
 * actually runs today. There's no rule/template management UI yet (see
 * docs/workflow-engine-research.md) — the archive rule isn't user-configurable, and the other
 * seeded templates exist in the gallery for a future UI/engine phase to pick up.
 */
class WorkflowRulesUtil(
  private val workflowTemplateRepository: WorkflowTemplateRepository,
  private val workflowRuleRepository: WorkflowRuleRepository,
  private val applyWorkflowTemplateUseCase: ApplyWorkflowTemplateUseCase
) {

  suspend fun initDefaultIfEmpty() {
    if (workflowTemplateRepository.countAll() == 0) {
      seedBuiltInTemplates()
    }
    if (workflowRuleRepository.countAll() == 0) {
      val archiveTemplate = workflowTemplateRepository.getAll()
        .firstOrNull { it.id == ARCHIVE_TEMPLATE_ID }
      if (archiveTemplate != null) {
        applyWorkflowTemplateUseCase(archiveTemplate, WorkflowScope.Global)
      }
    }
  }

  private suspend fun seedBuiltInTemplates() {
    workflowTemplateRepository.save(
      WorkflowTemplate(
        id = ARCHIVE_TEMPLATE_ID,
        title = "Archive completed reminders after $ARCHIVE_AFTER_DAYS days",
        description = "Automatically archives a reminder once it's been completed for a while.",
        category = WorkflowTemplateCategory.REMINDER_LIFECYCLE,
        supportedScopeTypes = listOf(WorkflowScopeType.GLOBAL, WorkflowScopeType.GROUP, WorkflowScopeType.REMINDER),
        trigger = WorkflowTrigger.ReminderAgeExceeded(days = ARCHIVE_AFTER_DAYS),
        action = WorkflowAction.ArchiveReminder
      )
    )
    workflowTemplateRepository.save(
      WorkflowTemplate(
        id = ESCALATE_TEMPLATE_ID,
        title = "Escalate after $ESCALATE_AFTER_SNOOZES repeated snoozes",
        description = "Bypasses Do Not Disturb and raises priority once a reminder is snoozed too often.",
        category = WorkflowTemplateCategory.NOTIFICATION_ESCALATION,
        supportedScopeTypes = listOf(WorkflowScopeType.GLOBAL, WorkflowScopeType.GROUP, WorkflowScopeType.REMINDER),
        trigger = WorkflowTrigger.ReminderSnoozedNTimes(count = ESCALATE_AFTER_SNOOZES),
        action = WorkflowAction.ApplyNotificationOverride(
          override = NotificationSettingsOverride(priority = ReminderPriority.HIGH, bypassDoNotDisturb = true)
        )
      )
    )
    workflowTemplateRepository.save(
      WorkflowTemplate(
        id = GROUP_COMPLETE_TEMPLATE_ID,
        title = "Archive group once everything is completed",
        description = "Archives every reminder in a group once none of its reminders are still active.",
        category = WorkflowTemplateCategory.GROUP,
        supportedScopeTypes = listOf(WorkflowScopeType.GROUP),
        trigger = WorkflowTrigger.GroupAllCompleted,
        action = WorkflowAction.ArchiveReminder
      )
    )
  }

  companion object {
    private const val ARCHIVE_TEMPLATE_ID = "built_in_template_archive_completed_reminders"
    private const val ESCALATE_TEMPLATE_ID = "built_in_template_escalate_on_repeated_snooze"
    private const val GROUP_COMPLETE_TEMPLATE_ID = "built_in_template_archive_group_on_completion"
    private const val ARCHIVE_AFTER_DAYS = 30
    private const val ESCALATE_AFTER_SNOOZES = 3
  }
}
