package com.github.naz013.feature.pomodoro

import com.github.naz013.feature.pomodoro.engine.PomodoroMode

/** Seam implemented in `app` (mirrors `ReminderNotifier`): starts/updates/stops the persistent
 * foreground-service notification that keeps a running session alive in the background. The
 * engine calls this; it never touches `Service`/`NotificationManager` itself. */
interface PomodoroForegroundNotifier {
  fun start()

  fun update(mode: PomodoroMode, remainingSeconds: Int, isPaused: Boolean)

  fun stop()
}
