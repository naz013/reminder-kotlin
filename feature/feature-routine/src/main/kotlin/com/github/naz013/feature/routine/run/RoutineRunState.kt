package com.github.naz013.feature.routine.run

sealed interface RoutineRunState {
  data object Idle : RoutineRunState

  data class Loading(val routineId: String) : RoutineRunState

  data class NotFound(val routineId: String) : RoutineRunState

  data class Running(
    val routineId: String,
    val routineTitle: String,
    val stepIndex: Int,
    val stepCount: Int,
    val stepTitle: String,
    val scheduledTimeLabel: String?,
    val isTimed: Boolean,
    val remainingSeconds: Int,
    val stepTotalSeconds: Int,
    val isPaused: Boolean,
    /** Wall-clock end of the step countdown; null when paused or untimed. */
    val stepEndsAtMillis: Long?,
    val completedCount: Int,
  ) : RoutineRunState

  data class Finished(
    val routineId: String,
    val totalTimeSeconds: Int,
    val completedCount: Int,
    val totalCount: Int,
  ) : RoutineRunState
}
