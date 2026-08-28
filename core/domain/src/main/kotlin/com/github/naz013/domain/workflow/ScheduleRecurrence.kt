package com.github.naz013.domain.workflow

/** How often a [WorkflowTrigger.ScheduleReached] trigger re-fires once its wall-clock time has
 * first been reached. */
enum class ScheduleRecurrence {
  ONCE,
  WEEKLY
}
