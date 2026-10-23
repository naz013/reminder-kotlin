package com.elementary.tasks.routine

import com.github.naz013.feature.routine.run.RoutineRunState
import java.util.Locale
import kotlin.math.abs

/** Pure description of what the routine run notification should show; no framework types. */
internal data class RoutineRunNotificationPlan(
  val routineTitle: String,
  val stepTitle: String,
  val stepNumber: Int,
  val stepCount: Int,
  val isPaused: Boolean,
  val showToggleAction: Boolean,
  val useChronometer: Boolean,
  val endTimeMillis: Long,
  val pausedTimeLabel: String?,
  val showProgress: Boolean,
  val completedCount: Int,
  val promoted: Boolean,
)

/** What was last posted, used to skip re-posts that the system chronometer already covers. */
internal data class RoutineRunPostedSnapshot(
  val routineId: String,
  val stepIndex: Int,
  val stepCount: Int,
  val stepTitle: String,
  val isTimed: Boolean,
  val isPaused: Boolean,
  val remainingSeconds: Int,
  val completedCount: Int,
  val endTimeMillis: Long,
)

internal object RoutineRunNotificationPlanner {
  const val END_TIME_TOLERANCE_MILLIS = 2_000L
  const val LIVE_UPDATE_SDK = 36
  private const val SECONDS_PER_MINUTE = 60
  private const val MILLIS_PER_SECOND = 1_000L

  fun plan(
    state: RoutineRunState.Running,
    nowMillis: Long,
    sdkInt: Int,
    canPostPromoted: Boolean = true,
  ): RoutineRunNotificationPlan {
    val supportsLiveUpdate = sdkInt >= LIVE_UPDATE_SDK && canPostPromoted
    val remaining = state.remainingSeconds.coerceAtLeast(0)
    return RoutineRunNotificationPlan(
      routineTitle = state.routineTitle,
      stepTitle = state.stepTitle,
      stepNumber = state.stepIndex + 1,
      stepCount = state.stepCount,
      isPaused = state.isPaused,
      showToggleAction = state.isTimed,
      useChronometer = state.isTimed && !state.isPaused,
      endTimeMillis = state.stepEndsAtMillis ?: (nowMillis + remaining * MILLIS_PER_SECOND),
      pausedTimeLabel = if (state.isTimed && state.isPaused) formatTime(remaining) else null,
      showProgress = supportsLiveUpdate && state.stepCount > 0,
      completedCount = state.completedCount.coerceIn(0, state.stepCount),
      promoted = supportsLiveUpdate,
    )
  }

  fun snapshot(state: RoutineRunState.Running, nowMillis: Long): RoutineRunPostedSnapshot {
    return RoutineRunPostedSnapshot(
      routineId = state.routineId,
      stepIndex = state.stepIndex,
      stepCount = state.stepCount,
      stepTitle = state.stepTitle,
      isTimed = state.isTimed,
      isPaused = state.isPaused,
      remainingSeconds = state.remainingSeconds,
      completedCount = state.completedCount,
      endTimeMillis = state.stepEndsAtMillis ?: (nowMillis + state.remainingSeconds * MILLIS_PER_SECOND),
    )
  }

  /** True only on a material change, so the per-second engine ticks do not re-post. */
  fun shouldRepost(last: RoutineRunPostedSnapshot?, current: RoutineRunPostedSnapshot): Boolean {
    if (last == null) return true
    if (last.routineId != current.routineId || last.stepIndex != current.stepIndex) return true
    if (last.stepCount != current.stepCount || last.stepTitle != current.stepTitle) return true
    if (last.isTimed != current.isTimed || last.isPaused != current.isPaused) return true
    if (last.completedCount != current.completedCount) return true
    if (!current.isTimed) return false
    return if (current.isPaused) {
      last.remainingSeconds != current.remainingSeconds
    } else {
      abs(last.endTimeMillis - current.endTimeMillis) > END_TIME_TOLERANCE_MILLIS
    }
  }

  fun formatTime(remainingSeconds: Int): String {
    return String.format(Locale.ROOT, "%02d:%02d", remainingSeconds / SECONDS_PER_MINUTE, remainingSeconds % SECONDS_PER_MINUTE)
  }
}
