package com.github.naz013.feature.pomodoro.engine

import kotlinx.coroutines.flow.StateFlow

/**
 * Public (not `internal`): both this feature's own [com.github.naz013.feature.pomodoro.compose.PomodoroTimerViewModel]
 * and `app`'s foreground `Service` (which keeps the process alive and shows the ongoing
 * notification while a session is running) need to start/observe/control it, and `app` is always
 * allowed to depend on a `feature-*` module. A Koin `single`, so its [state] and ticking coroutine
 * outlive any one Compose screen or ViewModel - stopping only when a session actually ends.
 */
interface PomodoroTimerEngine {
  val state: StateFlow<PomodoroEngineState>

  fun start(linkedReminderId: String?)

  fun pause()

  fun resume()

  fun skipToBreak()

  fun stop(discard: Boolean)
}
