package com.github.naz013.domain.workflow

import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.google.gson.annotations.SerializedName

/** An extra, optional filter narrowing when a [WorkflowRule]'s trigger actually fires its action.
 * All of a rule's conditions must hold (AND-chain) - a minimal, extensible v1 vocabulary, not a
 * full boolean-expression system.
 *
 * Variants are Gson round-tripped directly (see `WorkflowTriggerActionCodec`), so every field
 * needs [SerializedName] - see [com.github.naz013.domain.reminder.v2.RecurrenceRule] for why an
 * unannotated field is a production-crash risk under R8. */
sealed class WorkflowCondition {
  /** Matches when the reminder's resolved priority is [priority] or higher. */
  data class PriorityAtLeast(
    @SerializedName("priority")
    val priority: ReminderPriority
  ) : WorkflowCondition()

  /** Matches when the current time of day falls in `[fromMinuteOfDay, toMinuteOfDay)`
   * (0..1439 each), wrapping past midnight if `fromMinuteOfDay > toMinuteOfDay`. */
  data class WithinTimeWindow(
    @SerializedName("fromMinuteOfDay")
    val fromMinuteOfDay: Int,
    @SerializedName("toMinuteOfDay")
    val toMinuteOfDay: Int
  ) : WorkflowCondition()

  /** Restricts a rule's effect to one group, without duplicating the rule per-group - distinct
   * from [WorkflowScope.ForGroup], which is about where the rule is *managed*, not an extra
   * filter on a rule managed elsewhere (e.g. a Global rule that should only ever fire for one
   * particular group). */
  data class GroupIs(
    @SerializedName("groupId")
    val groupId: String
  ) : WorkflowCondition()
}
