package com.github.naz013.insights.aggregator

import com.github.naz013.domain.routine.RoutineExecutionRecord

/** Per-step completion consistency for one routine, across its execution records - which steps
 * are reliably completed vs. frequently skipped or run out of time. */
internal object RoutineStepDropoffCalculator {

  data class StepDropoff(
    val stepId: String,
    val completedRuns: Int,
    val totalRuns: Int
  ) {
    val completionRate: Float
      get() = if (totalRuns == 0) 0f else completedRuns.toFloat() / totalRuns.toFloat()
  }

  /** [stepIds] should be the routine's *current* step ids - a record may reference a step id that
   * no longer exists if the routine was edited since, and there's nothing meaningful left to show
   * for a step that isn't in the routine anymore, so those are silently excluded. */
  fun calculate(records: List<RoutineExecutionRecord>, stepIds: List<String>): List<StepDropoff> {
    val totalRuns = records.size
    if (totalRuns == 0) return emptyList()
    return stepIds.map { stepId ->
      val completedRuns = records.count { stepId in it.completedStepIds }
      StepDropoff(stepId = stepId, completedRuns = completedRuns, totalRuns = totalRuns)
    }
  }
}
