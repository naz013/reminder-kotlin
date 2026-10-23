package com.elementary.tasks.pomodoro

import android.app.ForegroundServiceStartNotAllowedException
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.feature.pomodoro.engine.PomodoroEngineState
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
  private val notificationFactory = PomodoroNotificationFactory()
  private var lastPosted: PomodoroPostedSnapshot? = null

  override fun onCreate() {
    super.onCreate()
    pomodoroTimerEngine.state.onEach { state ->
      if (!state.isRunning) {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
      } else {
        showNotification(state)
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
      startAsForeground(state)
    } else {
      startPlaceholderAndStop()
    }
    return START_NOT_STICKY
  }

  private fun startPlaceholderAndStop() {
    val placeholder = notifier.getNotificationBuilder(Notifier.CHANNEL_FOCUS_TIMER)
      .setSmallIcon(R.drawable.ic_fluent_clock_alarm)
      .setContentTitle(getString(R.string.pomodoro_focus_timer))
      .build()
    try {
      startForeground(NOTIFICATION_ID, placeholder)
    } catch (e: IllegalStateException) {
      Logger.i(TAG, "Start foreground: not allowed, ${e.message}")
    }
    stopForeground(STOP_FOREGROUND_REMOVE)
    stopSelf()
  }

  private fun showNotification(state: PomodoroEngineState) {
    val snapshot = PomodoroNotificationPlanner.snapshot(state, System.currentTimeMillis())
    if (PomodoroNotificationPlanner.shouldRepost(lastPosted, snapshot)) {
      lastPosted = snapshot
      notifier.notify(NOTIFICATION_ID, buildNotification(state))
    }
  }

  private fun startAsForeground(state: PomodoroEngineState) {
    lastPosted = PomodoroNotificationPlanner.snapshot(state, System.currentTimeMillis())
    val notification = buildNotification(state)
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

  private fun buildNotification(state: PomodoroEngineState): android.app.Notification {
    val plan = PomodoroNotificationPlanner.plan(
      state,
      System.currentTimeMillis(),
      Build.VERSION.SDK_INT,
      notifier.canPostPromotedNotifications(),
    )
    val texts = PomodoroNotificationTexts(
      title = getString(plan.mode.labelRes()),
      toggleActionLabel = getString(if (state.isPaused) R.string.pomodoro_resume else R.string.pomodoro_pause),
      stopActionLabel = getString(R.string.pomodoro_stop),
    )
    return notificationFactory.build(
      builder = notifier.getNotificationBuilder(Notifier.CHANNEL_FOCUS_TIMER),
      plan = plan,
      texts = texts,
      toggleIntent = servicePendingIntent(if (state.isPaused) ACTION_RESUME else ACTION_PAUSE),
      stopIntent = servicePendingIntent(ACTION_STOP),
    )
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
    const val ACTION_START = "com.elementary.tasks.pomodoro.action.START"
    const val ACTION_PAUSE = "com.elementary.tasks.pomodoro.action.PAUSE"
    const val ACTION_RESUME = "com.elementary.tasks.pomodoro.action.RESUME"
    const val ACTION_STOP = "com.elementary.tasks.pomodoro.action.STOP"
  }
}
