package com.github.naz013.feature.routine.run

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Single active routine run that outlives any screen: owns the ticker, deadline math and recording.
 * The focus screen and the foreground-service notification are both just observers/forwarders.
 */
interface RoutineRunEngine {
  val state: StateFlow<RoutineRunState>

  /** Emits once per step transition when haptic feedback is enabled. */
  val stepTransitions: Flow<Unit>

  /**
   * Starts a run; no-op when that routine is already running, replaces any other active run.
   * Returns an attach token for [release].
   */
  fun start(routineId: String): Long

  /** Stops the run only if [token] belongs to the latest [start] call, so a stale owner cannot end a newer attach. */
  fun release(token: Long)

  fun pause()

  fun resume()

  fun addMinute()

  fun skip()

  fun completeStep()

  fun previous()

  /** Ends the run: records silently when it is mid-way, then returns to [RoutineRunState.Idle]. */
  fun stop()
}
