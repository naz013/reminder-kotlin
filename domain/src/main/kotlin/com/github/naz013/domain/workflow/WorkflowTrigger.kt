package com.github.naz013.domain.workflow

sealed class WorkflowTrigger {
  data object ReminderCompleted : WorkflowTrigger()

  data class ReminderSnoozedNTimes(
    val count: Int
  ) : WorkflowTrigger()

  data object GroupAllCompleted : WorkflowTrigger()

  data object LocationEntered : WorkflowTrigger()

  data object LocationExited : WorkflowTrigger()

  /** Powers the auto-archive workflow: fires for completed reminders older than [days]. */
  data class ReminderAgeExceeded(
    val days: Int
  ) : WorkflowTrigger()

  data class ReminderUnacknowledgedFor(
    val minutes: Int
  ) : WorkflowTrigger()
}
