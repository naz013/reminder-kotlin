package com.github.naz013.logic.workflow

import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.workflow.WorkflowAction
import com.github.naz013.domain.workflow.WorkflowCondition
import com.github.naz013.domain.workflow.WorkflowRule
import com.github.naz013.domain.workflow.WorkflowScope
import com.github.naz013.domain.workflow.WorkflowTrigger
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderV2Repository
import com.github.naz013.repository.WorkflowRuleRepository
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkScheduler
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset

/**
 * A [WorkflowAction] the engine could not finish applying itself — [WorkflowAction.CompleteReminder]
 * and [WorkflowAction.ActivateReminder] both need app-module-only use cases (JobScheduler, Google
 * Tasks/Calendar sync, ...) this pure-JVM module can't depend on. [contextReminderId] is the
 * reminder that *matched the rule*, not necessarily the action's own target — for
 * [WorkflowAction.ActivateReminder] the reminder to activate is [WorkflowAction.ActivateReminder.reminderId]
 * on the action itself, which may be a different reminder than the one that triggered the rule
 * (chained/dependent reminders).
 */
data class PendingWorkflowAction(
  val action: WorkflowAction,
  val contextReminderId: String
)

/**
 * Evaluates enabled [WorkflowRule]s against current ReminderV2/GroupV2 state and runs their
 * action for any reminder that qualifies (in scope, and every one of the rule's [WorkflowCondition]s
 * holds). Each trigger gets its own `run*Rules()` method — see docs/workflow-engine-research.md for
 * the full catalog. [WorkflowAction.ArchiveReminder], [WorkflowAction.ApplyNotificationOverride],
 * and [WorkflowAction.RunBackgroundTask] are applied directly; [WorkflowAction.CompleteReminder] and
 * [WorkflowAction.ActivateReminder] are returned as a [PendingWorkflowAction] for an app-module
 * dispatcher to finish.
 */
class WorkflowEngine(
  private val workflowRuleRepository: WorkflowRuleRepository,
  private val reminderV2Repository: ReminderV2Repository,
  @Suppress("unused") private val groupV2Repository: GroupV2Repository,
  private val workScheduler: WorkScheduler
) {

  /** Archives every completed reminder older than the threshold of any enabled
   * [WorkflowTrigger.ReminderAgeExceeded] rule in scope for it. [now] defaults to the real
   * current time (this device's local zone); tests inject a fixed value instead. Converted to
   * UTC before comparing, since [referenceDateTime] reads `ReminderV2.schedule` fields, which are
   * stored UTC-zoned - this module has no `DateTimeManager` dependency (pure Kotlin/JVM), so the
   * zone conversion is done directly here rather than via that shared helper. */
  suspend fun runAgeBasedRules(now: LocalDateTime = LocalDateTime.now()): List<PendingWorkflowAction> {
    val rules = workflowRuleRepository.getByTriggerType(TRIGGER_TYPE_REMINDER_AGE_EXCEEDED)
      .filter { it.isEnabled }
    if (rules.isEmpty()) return emptyList()

    val completedReminders = reminderV2Repository.getAll(active = false, removed = false)
    val nowUtc = now.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()

    val pending = mutableListOf<PendingWorkflowAction>()
    for (rule in rules) {
      val trigger = rule.trigger as? WorkflowTrigger.ReminderAgeExceeded ?: continue
      val cutoff = nowUtc.minusDays(trigger.days.toLong())
      completedReminders
        .asSequence()
        .filter { qualifies(it, rule, now) }
        .filter { referenceDateTime(it).isBefore(cutoff) }
        .forEach { reminder -> apply(rule.action, reminder)?.let { pending.add(it) } }
    }
    return pending
  }

  /** Archives every completed reminder in a group once none of the group's reminders are still
   * active — only meaningful for rules scoped to a specific group; [WorkflowScope.Global] and
   * [WorkflowScope.ForReminder] rules of this trigger type are skipped since the trigger needs a
   * concrete group to check completion against. */
  suspend fun runGroupCompletionRules(now: LocalDateTime = LocalDateTime.now()): List<PendingWorkflowAction> {
    val rules = workflowRuleRepository.getByTriggerType(TRIGGER_TYPE_GROUP_ALL_COMPLETED)
      .filter { it.isEnabled }

    val pending = mutableListOf<PendingWorkflowAction>()
    for (rule in rules) {
      checkGroupCompletion(rule, now, pending)
    }
    return pending
  }

  private suspend fun checkGroupCompletion(
    rule: WorkflowRule,
    now: LocalDateTime,
    pending: MutableList<PendingWorkflowAction>
  ) {
    val groupId = (rule.scope as? WorkflowScope.ForGroup)?.groupId ?: return
    if (reminderV2Repository.countActiveByGroupId(groupId) > 0) return
    reminderV2Repository.getByGroupId(groupId)
      .filter { !it.isActive && !it.isRemoved && matchesConditions(it, rule.conditions, now) }
      .forEach { reminder -> apply(rule.action, reminder)?.let { pending.add(it) } }
  }

  /** Fires every enabled, in-scope [WorkflowTrigger.ReminderCompleted] rule for [reminderId]. */
  suspend fun runReminderCompletedRules(
    reminderId: String,
    now: LocalDateTime = LocalDateTime.now()
  ): List<PendingWorkflowAction> {
    val reminder = reminderV2Repository.getById(reminderId) ?: return emptyList()
    val rules = workflowRuleRepository.getByTriggerType(TRIGGER_TYPE_REMINDER_COMPLETED)
      .filter { it.isEnabled && qualifies(reminder, it, now) }
    return rules.mapNotNull { apply(it.action, reminder) }
  }

  /** Fires every enabled, in-scope [WorkflowTrigger.ReminderSnoozedNTimes] rule whose count
   * exactly matches [reminderId]'s current `snoozeCount` — an exact match (not `>=`) so a rule
   * fires once at the threshold snooze rather than on every subsequent snooze past it. */
  suspend fun runSnoozeCountRules(
    reminderId: String,
    now: LocalDateTime = LocalDateTime.now()
  ): List<PendingWorkflowAction> {
    val reminder = reminderV2Repository.getById(reminderId) ?: return emptyList()
    val rules = workflowRuleRepository.getByTriggerType(TRIGGER_TYPE_REMINDER_SNOOZED_N_TIMES)
      .filter { it.isEnabled && qualifies(reminder, it, now) }
      .filter { (it.trigger as? WorkflowTrigger.ReminderSnoozedNTimes)?.count?.toLong() == reminder.snoozeCount }
    return rules.mapNotNull { apply(it.action, reminder) }
  }

  /** Fires every enabled [WorkflowTrigger.LocationEntered] rule for [reminderId]. Restricted to
   * [WorkflowScope.ForReminder] rules matching this exact reminder — location checks only ever
   * evaluate a reminder's own places (see `CheckLocationReminderUseCase`), so there is no
   * group/global geofence concept to fan out to yet. */
  suspend fun runLocationEnteredRules(
    reminderId: String,
    now: LocalDateTime = LocalDateTime.now()
  ): List<PendingWorkflowAction> = runLocationRules(reminderId, TRIGGER_TYPE_LOCATION_ENTERED, now)

  /** Fires every enabled [WorkflowTrigger.LocationExited] rule for [reminderId]. Same
   * `ForReminder`-only scope restriction as [runLocationEnteredRules]. */
  suspend fun runLocationExitedRules(
    reminderId: String,
    now: LocalDateTime = LocalDateTime.now()
  ): List<PendingWorkflowAction> = runLocationRules(reminderId, TRIGGER_TYPE_LOCATION_EXITED, now)

  private suspend fun runLocationRules(
    reminderId: String,
    triggerType: String,
    now: LocalDateTime
  ): List<PendingWorkflowAction> {
    val reminder = reminderV2Repository.getById(reminderId) ?: return emptyList()
    val rules = workflowRuleRepository.getByTriggerType(triggerType)
      .filter { it.isEnabled && (it.scope as? WorkflowScope.ForReminder)?.reminderId == reminderId }
      .filter { matchesConditions(reminder, it.conditions, now) }
    return rules.mapNotNull { apply(it.action, reminder) }
  }

  /** Polling counterpart of the notification-fired write in `ReminderActionProcessor.process()` -
   * fires every enabled, in-scope [WorkflowTrigger.ReminderUnacknowledgedFor] rule for any active
   * reminder whose `lastShownAt` is at least the rule's threshold in the past. [now] defaults to
   * the real current time; tests inject a fixed value instead. */
  suspend fun runUnacknowledgedRules(now: LocalDateTime = LocalDateTime.now()): List<PendingWorkflowAction> {
    val rules = workflowRuleRepository.getByTriggerType(TRIGGER_TYPE_REMINDER_UNACKNOWLEDGED_FOR)
      .filter { it.isEnabled }
    if (rules.isEmpty()) return emptyList()

    val shownReminders = reminderV2Repository.getAll(active = true, removed = false)
      .filter { it.lastShownAt != null }
    val nowUtc = now.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()

    val pending = mutableListOf<PendingWorkflowAction>()
    for (rule in rules) {
      val trigger = rule.trigger as? WorkflowTrigger.ReminderUnacknowledgedFor ?: continue
      shownReminders
        .asSequence()
        .filter { qualifies(it, rule, now) }
        .filter { reminder -> !reminder.lastShownAt!!.plusMinutes(trigger.minutes.toLong()).isAfter(nowUtc) }
        .forEach { reminder -> apply(rule.action, reminder)?.let { pending.add(it) } }
    }
    return pending
  }

  private fun referenceDateTime(reminder: ReminderV2): LocalDateTime =
    reminder.schedule.updatedAt ?: reminder.schedule.startDateTime

  private fun isInScope(reminder: ReminderV2, scope: WorkflowScope): Boolean = when (scope) {
    is WorkflowScope.Global -> true
    is WorkflowScope.ForGroup -> reminder.groupId == scope.groupId
    is WorkflowScope.ForReminder -> reminder.uuId == scope.reminderId
  }

  /** Whether [reminder] is both in [WorkflowRule.scope] and passes every one of its
   * [WorkflowRule.conditions] (an AND-chain). */
  private fun qualifies(reminder: ReminderV2, rule: WorkflowRule, now: LocalDateTime): Boolean =
    isInScope(reminder, rule.scope) && matchesConditions(reminder, rule.conditions, now)

  private fun matchesConditions(
    reminder: ReminderV2,
    conditions: List<WorkflowCondition>,
    now: LocalDateTime
  ): Boolean = conditions.all { condition ->
    when (condition) {
      is WorkflowCondition.PriorityAtLeast ->
        (reminder.notification.priority ?: ReminderPriority.NORMAL).ordinal >= condition.priority.ordinal

      is WorkflowCondition.WithinTimeWindow -> {
        val minuteOfDay = now.hour * 60 + now.minute
        if (condition.fromMinuteOfDay <= condition.toMinuteOfDay) {
          minuteOfDay in condition.fromMinuteOfDay until condition.toMinuteOfDay
        } else {
          minuteOfDay >= condition.fromMinuteOfDay || minuteOfDay < condition.toMinuteOfDay
        }
      }

      is WorkflowCondition.GroupIs -> reminder.groupId == condition.groupId
    }
  }

  private suspend fun apply(action: WorkflowAction, reminder: ReminderV2): PendingWorkflowAction? =
    when (action) {
      is WorkflowAction.ArchiveReminder -> {
        reminderV2Repository.save(reminder.copy(isRemoved = true))
        null
      }

      is WorkflowAction.ApplyNotificationOverride -> {
        reminderV2Repository.save(reminder.copy(notification = action.override))
        null
      }

      is WorkflowAction.RunBackgroundTask -> {
        workScheduler.enqueue(
          WorkRequest(taskKey = action.taskKey, tag = "workflow-${action.taskKey}-${reminder.uuId}")
        )
        null
      }

      is WorkflowAction.CompleteReminder,
      is WorkflowAction.ActivateReminder -> PendingWorkflowAction(action, reminder.uuId)
    }

  companion object {
    private const val TRIGGER_TYPE_REMINDER_COMPLETED = "REMINDER_COMPLETED"
    private const val TRIGGER_TYPE_REMINDER_SNOOZED_N_TIMES = "REMINDER_SNOOZED_N_TIMES"
    private const val TRIGGER_TYPE_GROUP_ALL_COMPLETED = "GROUP_ALL_COMPLETED"
    private const val TRIGGER_TYPE_LOCATION_ENTERED = "LOCATION_ENTERED"
    private const val TRIGGER_TYPE_LOCATION_EXITED = "LOCATION_EXITED"
    private const val TRIGGER_TYPE_REMINDER_AGE_EXCEEDED = "REMINDER_AGE_EXCEEDED"
    private const val TRIGGER_TYPE_REMINDER_UNACKNOWLEDGED_FOR = "REMINDER_UNACKNOWLEDGED_FOR"
  }
}
