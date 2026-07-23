package com.elementary.tasks.workflow

import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.repository.WorkflowRuleRepository

/**
 * Seeds the built-in "auto-archive completed reminders" rule on first run. There's no rule
 * management UI yet (see docs/workflow-engine-research.md) — this is the only workflow rule that
 * ships today, and it isn't user-configurable.
 */
class WorkflowRulesUtil(
  private val workflowRuleRepository: WorkflowRuleRepository
) {

  suspend fun initDefaultIfEmpty() {
    if (workflowRuleRepository.countAll() == 0) {
      workflowRuleRepository.save(
        WorkflowRule(
          uuId = BUILT_IN_ARCHIVE_RULE_ID,
          title = "Archive completed reminders after $ARCHIVE_AFTER_DAYS days",
          scope = WorkflowScope.Global,
          trigger = WorkflowTrigger.ReminderAgeExceeded(days = ARCHIVE_AFTER_DAYS),
          action = WorkflowAction.ArchiveReminder
        )
      )
    }
  }

  companion object {
    private const val BUILT_IN_ARCHIVE_RULE_ID = "built_in_archive_completed_reminders"
    private const val ARCHIVE_AFTER_DAYS = 30
  }
}
