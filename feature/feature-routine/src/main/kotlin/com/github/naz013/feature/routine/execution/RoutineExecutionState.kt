package com.github.naz013.feature.routine.execution

import androidx.compose.ui.graphics.Color

internal sealed interface RoutineExecutionState {
  data object Loading : RoutineExecutionState

  data class Running(
    val routineTitle: String,
    val backgroundColor: Color,
    val contentColor: Color,
    val stepIndex: Int,
    val stepCount: Int,
    val stepTitle: String,
    val scheduledTimeLabel: String?,
    val isTimed: Boolean,
    val remainingSeconds: Int,
    val timeLabel: String,
    val progress: Float,
    val isPaused: Boolean,
    val isFirstStep: Boolean,
  ) : RoutineExecutionState

  data class Finished(
    val totalTimeLabel: String,
    val completedCount: Int,
    val totalCount: Int,
  ) : RoutineExecutionState
}
