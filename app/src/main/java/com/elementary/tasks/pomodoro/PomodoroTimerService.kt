package com.elementary.tasks.pomodoro

import android.app.ForegroundServiceStartNotAllowedException
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.feature.pomodoro.engine.PomodoroMode
import com.github.naz013.feature.pomodoro.engine.PomodoroTimerEngine
import com.github.naz013.logging.Logger
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject

/**
 * Keeps the process alive (elevated priority) and shows an ongoing notification while a Pomodoro
 * session is running, so a work/break countdown survives the app being backgrounded - mirrors
 * [com.elementary.tasks.core.services.GeolocationService]'s `startForeground` pattern. Owns no
 * timer state itself: [PomodoroTimerEngine] is a Koin singleton that keeps ticking regardless of
 * whether this service or any screen is alive: this service only reflects its state.
 */
class PomodoroTimerService : LifecycleService() {
  private val notifier by inject<Notifier>()
  private val pomodoroTimerEngine by inject<PomodoroTimerEngine>()

  override fun onCreate() {
    super.onCreate()
    pomodoroTimerEngine.state.onEach { state ->
      if (!state.isRunning) {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
      } else {
        showNotification(state.mode, state.remainingSeconds, state.isPaused)
      }
    }.launchIn(lifecycleScope)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    super.onStartCommand(intent, flags, startId)
    when (intent?.action) {
      ACTION_PAUSE -> pomodoroTimerEngine.pause()
      ACTION_RESUME -> pomodoroTimerEngine.resume()
      ACTION_STOP -> pomodoroTimerEngine.stop(discard = false)
      else -> Unit
    }
    val state = pomodoroTimerEngine.state.value
    if (state.isRunning) {
      startAsForeground(state.mode, state.remainingSeconds, state.isPaused)
    }
    return START_NOT_STICKY
  }

  private fun showNotification(mode: PomodoroMode, remainingSeconds: Int, isPaused: Boolean) {
    notifier.notify(NOTIFICATION_ID, buildNotification(mode, remainingSeconds, isPaused))
  }

  private fun startAsForeground(mode: PomodoroMode, remainingSeconds: Int, isPaused: Boolean) {
    val notification = buildNotification(mode, remainingSeconds, isPaused)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      try {
        startForeground(NOTIFICATION_ID, notification)
      } catch (e: ForegroundServiceStartNotAllowedException) {
        Logger.i(TAG, "Start foreground: not allowed, ${e.message}")
        stopSelf()
      }
    } else {
      startForeground(NOTIFICATION_ID, notification)
    }
  }

  private fun buildNotification(
    mode: PomodoroMode,
    remainingSeconds: Int,
    isPaused: Boolean,
  ): android.app.Notification {
    val minutes = remainingSeconds / SECONDS_PER_MINUTE
    val seconds = remainingSeconds % SECONDS_PER_MINUTE
    val timeLabel = "%02d:%02d".format(minutes, seconds)
    val modeLabelRes = when (mode) {
      PomodoroMode.Work -> R.string.pomodoro_work
      PomodoroMode.Break -> R.string.pomodoro_break
      PomodoroMode.LongBreak -> R.string.pomodoro_long_break
      PomodoroMode.Idle -> R.string.pomodoro_focus_timer
    }
    val builder = notifier.getNotificationBuilder(Notifier.CHANNEL_FOCUS_TIMER)
    builder.setContentTitle(getString(modeLabelRes))
    builder.setContentText(timeLabel)
    builder.setSmallIcon(R.drawable.ic_fluent_clock_alarm)
    builder.setOngoing(true)
    builder.setOnlyAlertOnce(true)
    builder.priority = NotificationCompat.PRIORITY_LOW
    builder.addAction(
      0,
      getString(if (isPaused) R.string.pomodoro_resume else R.string.pomodoro_pause),
      servicePendingIntent(if (isPaused) ACTION_RESUME else ACTION_PAUSE),
    )
    builder.addAction(0, getString(R.string.pomodoro_stop), servicePendingIntent(ACTION_STOP))
    return builder.build()
  }

  private fun servicePendingIntent(action: String): PendingIntent {
    val intent = Intent(this, PomodoroTimerService::class.java).setAction(action)
    return PendingIntent.getService(
      this,
      action.hashCode(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  companion object {
    private const val TAG = "PomodoroTimerService"
    private const val NOTIFICATION_ID = 1246
    private const val SECONDS_PER_MINUTE = 60
    const val ACTION_START = "com.elementary.tasks.pomodoro.action.START"
    const val ACTION_PAUSE = "com.elementary.tasks.pomodoro.action.PAUSE"
    const val ACTION_RESUME = "com.elementary.tasks.pomodoro.action.RESUME"
    const val ACTION_STOP = "com.elementary.tasks.pomodoro.action.STOP"
  }
}
