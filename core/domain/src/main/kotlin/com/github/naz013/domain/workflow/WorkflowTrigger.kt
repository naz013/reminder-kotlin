package com.github.naz013.domain.workflow

import com.google.gson.annotations.SerializedName

/** Variants with a payload are Gson round-tripped directly (see `WorkflowTriggerActionCodec`), so
 * every field needs [SerializedName] - see [com.github.naz013.domain.reminder.v2.RecurrenceRule]
 * for why an unannotated field is a production-crash risk under R8. */
sealed class WorkflowTrigger {
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
}
