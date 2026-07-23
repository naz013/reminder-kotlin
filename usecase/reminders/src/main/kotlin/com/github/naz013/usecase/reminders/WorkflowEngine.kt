package com.github.naz013.usecase.reminders

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.WorkflowRuleRepository
import org.threeten.bp.LocalDateTime

/**
 * Evaluates enabled [WorkflowRule]s against current ReminderV2/GroupV2 state and runs their
 * action for any reminder that qualifies. Each trigger gets its own `run*Rules()` method, added
 * as its owning phase is implemented — see docs/workflow-engine-research.md for the full catalog
 * and rollout plan. Only age-based rules (the auto-archive workflow) are implemented so far.
 */
class WorkflowEngine(
  private val workflowRuleRepository: WorkflowRuleRepository,
  private val reminderV2Repository: ReminderV2Repository,
  @Suppress("unused") private val groupV2Repository: GroupV2Repository
) {

  /** Archives every completed reminder older than the threshold of any enabled
   * [WorkflowTrigger.ReminderAgeExceeded] rule in scope for it. [now] defaults to the real
   * current time; tests inject a fixed value instead. */
  suspend fun runAgeBasedRules(now: LocalDateTime = LocalDateTime.now()) {
    val rules = workflowRuleRepository.getByTriggerType(TRIGGER_TYPE_REMINDER_AGE_EXCEEDED)
      .filter { it.isEnabled }
    if (rules.isEmpty()) return

    val completedReminders = reminderV2Repository.getAll(active = false, removed = false)

    for (rule in rules) {
      val trigger = rule.trigger as? WorkflowTrigger.ReminderAgeExceeded ?: continue
      val cutoff = now.minusDays(trigger.days.toLong())
      completedReminders
        .asSequence()
        .filter { isInScope(it, rule.scope) }
        .filter { referenceDateTime(it).isBefore(cutoff) }
        .forEach { reminder -> apply(rule.action, reminder) }
    }
  }

  private fun referenceDateTime(reminder: ReminderV2): LocalDateTime =
    reminder.schedule.updatedAt ?: reminder.schedule.startDateTime

  private fun isInScope(reminder: ReminderV2, scope: WorkflowScope): Boolean = when (scope) {
    is WorkflowScope.Global -> true
    is WorkflowScope.ForGroup -> reminder.groupId == scope.groupId
    is WorkflowScope.ForReminder -> reminder.uuId == scope.reminderId
  }

  private suspend fun apply(action: WorkflowAction, reminder: ReminderV2) {
    when (action) {
      is WorkflowAction.ArchiveReminder -> reminderV2Repository.save(reminder.copy(isRemoved = true))
      // Other action cases belong to trigger phases not implemented yet (see class doc).
      is WorkflowAction.CompleteReminder,
      is WorkflowAction.ApplyNotificationOverride,
      is WorkflowAction.ActivateReminder,
      is WorkflowAction.RunBackgroundTask -> Unit
    }
  }

  companion object {
    private const val TRIGGER_TYPE_REMINDER_AGE_EXCEEDED = "REMINDER_AGE_EXCEEDED"
  }
}
