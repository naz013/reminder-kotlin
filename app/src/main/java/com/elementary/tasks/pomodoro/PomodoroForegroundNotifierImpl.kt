package com.elementary.tasks.pomodoro

import android.content.Context
import android.content.Intent
import com.github.naz013.feature.pomodoro.PomodoroForegroundNotifier
import com.github.naz013.feature.pomodoro.engine.PomodoroMode

/** Starts/stops [PomodoroTimerService]. Doesn't handle per-tick [update] itself - the service
 * observes [com.github.naz013.feature.pomodoro.engine.PomodoroTimerEngine.state] directly and
 * refreshes its own notification, so this only needs to kick the service into existence/away. */
class PomodoroForegroundNotifierImpl(
  private val context: Context,
) : PomodoroForegroundNotifier {

  override fun start() {
    val intent = Intent(context, PomodoroTimerService::class.java).setAction(PomodoroTimerService.ACTION_START)
    context.startForegroundService(intent)
  }

  override fun update(mode: PomodoroMode, remainingSeconds: Int, isPaused: Boolean) {
    // No-op: PomodoroTimerService collects the engine's StateFlow itself while running.
  }

  override fun stop() {
    context.stopService(Intent(context, PomodoroTimerService::class.java))
  }
}
