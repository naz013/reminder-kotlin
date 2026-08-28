package com.github.naz013.domain.workflow

import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDateTime

/** Variants with a payload are Gson round-tripped directly (see `WorkflowTriggerActionCodec`), so
 * every field needs [SerializedName] - see [com.github.naz013.domain.reminder.v2.RecurrenceRule]
 * for why an unannotated field is a production-crash risk under R8. */
sealed class WorkflowTrigger {
  /** Fires the first time a reminder is ever saved - see `SaveReminderUseCase` for how "first
   * time" is detected. Powers keyword-based auto-grouping paired with [WorkflowCondition.TitleContains]
   * and [WorkflowAction.MoveToGroup]. */
  data object ReminderCreated : WorkflowTrigger()

  data object ReminderCompleted : WorkflowTrigger()

  data class ReminderSnoozedNTimes(
    @SerializedName("count")
    val count: Int
  ) : WorkflowTrigger()

  data object GroupAllCompleted : WorkflowTrigger()

  data object LocationEntered : WorkflowTrigger()

  data object LocationExited : WorkflowTrigger()

  /** Powers the auto-archive workflow: fires for completed reminders older than [days]. */
  data class ReminderAgeExceeded(
    @SerializedName("days")
    val days: Int
  ) : WorkflowTrigger()

  data class ReminderUnacknowledgedFor(
    @SerializedName("minutes")
    val minutes: Int
  ) : WorkflowTrigger()

  /** An absolute wall-clock trigger, not tied to any reminder's own state - see
   * `WorkflowEngine.runScheduleRules` for how it's evaluated and why it currently only supports
   * [WorkflowAction.RunBackgroundTask]. */
  data class ScheduleReached(
    @SerializedName("atDateTime")
    val atDateTime: LocalDateTime,
    @SerializedName("recurrence")
    val recurrence: ScheduleRecurrence = ScheduleRecurrence.ONCE
  ) : WorkflowTrigger()
}
