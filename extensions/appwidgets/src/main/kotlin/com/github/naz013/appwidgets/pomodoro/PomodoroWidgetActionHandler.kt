package com.github.naz013.appwidgets.pomodoro

import com.github.naz013.feature.pomodoro.engine.PomodoroTimerEngine
import com.github.naz013.logging.Logger
import com.github.naz013.platform.BuildInfo

internal class PomodoroWidgetActionHandler(
  private val buildInfo: BuildInfo,
  private val pomodoroTimerEngine: PomodoroTimerEngine
) {

  fun handle(action: PomodoroWidgetAction): Boolean {
    if (!buildInfo.isPro) {
      Logger.w(TAG, "Pomodoro is not available, ignoring $action")
      return false
    }
    when (action) {
      PomodoroWidgetAction.START -> pomodoroTimerEngine.start(linkedReminderId = null)
      PomodoroWidgetAction.PAUSE -> pomodoroTimerEngine.pause()
      PomodoroWidgetAction.RESUME -> pomodoroTimerEngine.resume()
      PomodoroWidgetAction.STOP -> pomodoroTimerEngine.stop(discard = false)
    }
    return true
  }

  companion object {
    private const val TAG = "PomodoroWidgetActionHandler"
  }
}

internal enum class PomodoroWidgetAction {
  START,
  PAUSE,
  RESUME,
  STOP
}
