package com.github.naz013.feature.workflow

import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.workflow.ScheduleRecurrence
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowScopeType
import com.github.naz013.domain.workflow.WorkflowTemplate
import com.github.naz013.domain.workflow.WorkflowTemplateCategory
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.logic.workflow.ApplyWorkflowTemplateUseCase
import com.github.naz013.logic.workflow.SaveWorkflowTemplateUseCase
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.repository.WorkflowTemplateRepository
import org.threeten.bp.LocalDateTime

/**
 * Seeds the built-in workflow template gallery on first run, and applies the archive template
 * globally so there's always at least one active rule to see the first time a user opens the
 * Workflow gallery. Only called once [com.github.naz013.logic.workflow.WorkflowConfig.isEnabled]
 * is true (see `BottomNavInitViewModel.checkDb()`) — the rule and templates are fully
 * user-manageable through the builder/gallery screens in this module, see
 * docs/workflow-engine-research.md for the broader catalog this is a starting point for.
 */
class WorkflowRulesUtil(
  private val workflowTemplateRepository: WorkflowTemplateRepository,
  private val workflowRuleRepository: WorkflowRuleRepository,
  private val applyWorkflowTemplateUseCase: ApplyWorkflowTemplateUseCase,
  private val saveWorkflowTemplateUseCase: SaveWorkflowTemplateUseCase
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
    saveWorkflowTemplateUseCase(archiveTemplate())
    saveWorkflowTemplateUseCase(escalateTemplate())
    saveWorkflowTemplateUseCase(groupCompleteTemplate())
    saveWorkflowTemplateUseCase(purgeTemplate())
    saveWorkflowTemplateUseCase(weeklySummaryTemplate())
  }

  private fun archiveTemplate() = WorkflowTemplate(
    id = ARCHIVE_TEMPLATE_ID,
    title = "Archive completed reminders after $ARCHIVE_AFTER_DAYS days",
    description = "Automatically archives a reminder once it's been completed for a while.",
    category = WorkflowTemplateCategory.REMINDER_LIFECYCLE,
    supportedScopeTypes = listOf(WorkflowScopeType.GLOBAL, WorkflowScopeType.GROUP, WorkflowScopeType.REMINDER),
    trigger = WorkflowTrigger.ReminderAgeExceeded(days = ARCHIVE_AFTER_DAYS),
    action = WorkflowAction.ArchiveReminder
  )

  private fun escalateTemplate() = WorkflowTemplate(
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

  private fun groupCompleteTemplate() = WorkflowTemplate(
    id = GROUP_COMPLETE_TEMPLATE_ID,
    title = "Archive group once everything is completed",
    description = "Archives every reminder in a group once none of its reminders are still active.",
    category = WorkflowTemplateCategory.GROUP,
    supportedScopeTypes = listOf(WorkflowScopeType.GROUP),
    trigger = WorkflowTrigger.GroupAllCompleted,
    action = WorkflowAction.ArchiveReminder
  )

  private fun purgeTemplate() = WorkflowTemplate(
    id = PURGE_TEMPLATE_ID,
    title = "Permanently delete reminders archived $PURGE_AFTER_DAYS+ days",
    description = "Frees up space by hard-deleting reminders that have been archived for a while. " +
      "This cannot be undone.",
    category = WorkflowTemplateCategory.PRIVACY_DATA_HYGIENE,
    supportedScopeTypes = listOf(WorkflowScopeType.GLOBAL, WorkflowScopeType.GROUP, WorkflowScopeType.REMINDER),
    trigger = WorkflowTrigger.ReminderAgeExceeded(days = PURGE_AFTER_DAYS),
    action = WorkflowAction.PurgeReminder
  )

  private fun weeklySummaryTemplate() = WorkflowTemplate(
    id = WEEKLY_SUMMARY_TEMPLATE_ID,
    title = "Weekly reminder completion summary",
    description = "Sends a notification once a week with how many reminders you completed.",
    category = WorkflowTemplateCategory.SYSTEM_INTEGRATION,
    supportedScopeTypes = listOf(WorkflowScopeType.GLOBAL),
    trigger = WorkflowTrigger.ScheduleReached(
      atDateTime = LocalDateTime.now().plusDays(WEEKLY_SUMMARY_FIRST_RUN_DELAY_DAYS),
      recurrence = ScheduleRecurrence.WEEKLY
    ),
    action = WorkflowAction.RunBackgroundTask(taskKey = WeeklySummaryTask.TASK_KEY)
  )

  companion object {
    private const val ARCHIVE_TEMPLATE_ID = "built_in_template_archive_completed_reminders"
    private const val ESCALATE_TEMPLATE_ID = "built_in_template_escalate_on_repeated_snooze"
    private const val GROUP_COMPLETE_TEMPLATE_ID = "built_in_template_archive_group_on_completion"
    private const val PURGE_TEMPLATE_ID = "built_in_template_purge_archived_reminders"
    private const val WEEKLY_SUMMARY_TEMPLATE_ID = "built_in_template_weekly_summary"
    private const val ARCHIVE_AFTER_DAYS = 30
    private const val ESCALATE_AFTER_SNOOZES = 3
    private const val PURGE_AFTER_DAYS = 90
    private const val WEEKLY_SUMMARY_FIRST_RUN_DELAY_DAYS = 7L
  }
}
