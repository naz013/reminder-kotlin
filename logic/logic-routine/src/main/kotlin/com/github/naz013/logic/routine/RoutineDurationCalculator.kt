package com.github.naz013.logic.routine

import com.github.naz013.domain.routine.RoutineStep

class RoutineDurationCalculator {

  fun calculateTotalDuration(steps: List<RoutineStep>): Int = steps.sumOf { it.durationSeconds }

  fun calculateRemainingDuration(steps: List<RoutineStep>): Int =
    steps.filterNot { it.isCompleted }.sumOf { it.durationSeconds }

  /** e.g. `3665` -> "1h 1m", `600` -> "10m", `45` -> "45s", `0` -> "0m". */
  fun formatDuration(seconds: Int): String {
    if (seconds <= 0) return "0m"
    val hours = seconds / SECONDS_PER_HOUR
    val minutes = (seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val secs = seconds % SECONDS_PER_MINUTE
    return when {
      hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
      hours > 0 -> "${hours}h"
      minutes > 0 -> "${minutes}m"
      else -> "${secs}s"
    }
  }

  companion object {
    private const val SECONDS_PER_MINUTE = 60
    private const val SECONDS_PER_HOUR = 3600
  }
}
