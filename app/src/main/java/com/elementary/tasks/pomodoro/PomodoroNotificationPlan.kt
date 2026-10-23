package com.elementary.tasks.pomodoro

import com.github.naz013.feature.pomodoro.engine.PomodoroEngineState
import com.github.naz013.feature.pomodoro.engine.PomodoroMode
import java.util.Locale

/** Pure description of what the Pomodoro notification should show; no framework types. */
internal data class PomodoroNotificationPlan(
  val mode: PomodoroMode,
  val isPaused: Boolean,
  val timeLabel: String,
  val useChronometer: Boolean,
  val endTimeMillis: Long,
  val showProgress: Boolean,
  val progressMax: Int,
  val progressElapsed: Int,
  val promoted: Boolean,
)

/** What was last posted, used to skip re-posts that the system chronometer already covers. */
internal data class PomodoroPostedSnapshot(
  val mode: PomodoroMode,
  val isPaused: Boolean,
  val totalSeconds: Int,
  val remainingSeconds: Int,
  val endTimeMillis: Long,
)

internal object PomodoroNotificationPlanner {
  const val END_TIME_TOLERANCE_MILLIS = 2_000L
  private const val SECONDS_PER_MINUTE = 60
  private const val MILLIS_PER_SECOND = 1_000L

  fun plan(
    state: PomodoroEngineState,
    nowMillis: Long,
    sdkInt: Int,
    canPostPromoted: Boolean,
  ): PomodoroNotificationPlan {
    val total = state.totalSeconds
    val remaining = state.remainingSeconds.coerceIn(0, if (total > 0) total else state.remainingSeconds)
    val supportsLiveUpdate = sdkInt >= LIVE_UPDATE_SDK && canPostPromoted
    return PomodoroNotificationPlan(
      mode = state.mode,
      isPaused = state.isPaused,
      timeLabel = formatTime(remaining),
      useChronometer = !state.isPaused,
      endTimeMillis = nowMillis + remaining * MILLIS_PER_SECOND,
      showProgress = supportsLiveUpdate && total > 0,
      progressMax = total,
      progressElapsed = (total - remaining).coerceIn(0, total.coerceAtLeast(0)),
      promoted = supportsLiveUpdate,
    )
  }

  fun snapshot(state: PomodoroEngineState, nowMillis: Long): PomodoroPostedSnapshot {
    return PomodoroPostedSnapshot(
      mode = state.mode,
      isPaused = state.isPaused,
      totalSeconds = state.totalSeconds,
      remainingSeconds = state.remainingSeconds,
      endTimeMillis = nowMillis + state.remainingSeconds * MILLIS_PER_SECOND,
    )
  }

  /** True only on a material change, so the per-second engine ticks do not re-post. */
  fun shouldRepost(last: PomodoroPostedSnapshot?, current: PomodoroPostedSnapshot): Boolean {
    if (last == null) return true
    if (last.mode != current.mode || last.isPaused != current.isPaused) return true
    if (last.totalSeconds != current.totalSeconds) return true
    return if (current.isPaused) {
      last.remainingSeconds != current.remainingSeconds
    } else {
      kotlin.math.abs(last.endTimeMillis - current.endTimeMillis) > END_TIME_TOLERANCE_MILLIS
    }
  }

  fun formatTime(remainingSeconds: Int): String {
    val minutes = remainingSeconds / SECONDS_PER_MINUTE
    val seconds = remainingSeconds % SECONDS_PER_MINUTE
    return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
  }

  const val LIVE_UPDATE_SDK = 36
}
