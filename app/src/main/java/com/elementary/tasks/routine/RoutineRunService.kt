package com.elementary.tasks.routine

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.Notifier
import com.github.naz013.feature.routine.run.RoutineRunEngine
import com.github.naz013.feature.routine.run.RoutineRunState
import com.github.naz013.logging.Logger
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject

/**
 * Keeps the process alive and shows an ongoing notification while a routine run is active, so the
 * step countdown survives the app being backgrounded. Owns no run state: [RoutineRunEngine] is a
 * Koin singleton and this service only reflects it and forwards notification actions to it.
 */
internal class RoutineRunService : LifecycleService() {
  private val notifier by inject<Notifier>()
  private val engine by inject<RoutineRunEngine>()
  private val notificationFactory = RoutineRunNotificationFactory()
  private var lastPosted: RoutineRunPostedSnapshot? = null

  override fun onCreate() {
    super.onCreate()
    engine.state.onEach { state ->
      if (state is RoutineRunState.Running) {
        showNotification(state)
      } else {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
      }
    }.launchIn(lifecycleScope)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    super.onStartCommand(intent, flags, startId)
    when (intent?.action) {
      ACTION_PAUSE -> engine.pause()
      ACTION_RESUME -> engine.resume()
      ACTION_SKIP -> engine.skip()
      ACTION_STOP -> engine.stop()
      else -> Unit
    }
    (engine.state.value as? RoutineRunState.Running)?.let(::startAsForeground)
    return START_NOT_STICKY
  }

  private fun showNotification(state: RoutineRunState.Running) {
    val snapshot = RoutineRunNotificationPlanner.snapshot(state, System.currentTimeMillis())
    if (RoutineRunNotificationPlanner.shouldRepost(lastPosted, snapshot)) {
      lastPosted = snapshot
      notifier.notify(NOTIFICATION_ID, buildNotification(state))
    }
  }

  private fun startAsForeground(state: RoutineRunState.Running) {
    lastPosted = RoutineRunNotificationPlanner.snapshot(state, System.currentTimeMillis())
    val notification = buildNotification(state)
    try {
      startForeground(NOTIFICATION_ID, notification)
    } catch (e: ForegroundServiceStartNotAllowedException) {
      Logger.i(TAG, "Start foreground: not allowed, ${e.message}")
      stopSelf()
    }
  }

  private fun buildNotification(state: RoutineRunState.Running): Notification {
    val plan = RoutineRunNotificationPlanner.plan(state, System.currentTimeMillis(), Build.VERSION.SDK_INT)
    val texts = RoutineRunNotificationTexts(
      stepOfLabel = getString(R.string.step_of_count, plan.stepNumber, plan.stepCount),
      pauseActionLabel = getString(R.string.pause),
      resumeActionLabel = getString(R.string.resume),
      skipActionLabel = getString(R.string.skip_step),
      stopActionLabel = getString(R.string.pomodoro_stop),
    )
    val intents = RoutineRunNotificationIntents(
      content = contentPendingIntent(),
      toggle = servicePendingIntent(if (state.isPaused) ACTION_RESUME else ACTION_PAUSE),
      skip = servicePendingIntent(ACTION_SKIP),
      stop = servicePendingIntent(ACTION_STOP),
    )
    return notificationFactory.build(
      builder = notifier.getNotificationBuilder(Notifier.CHANNEL_ROUTINE_RUN),
      plan = plan,
      texts = texts,
      intents = intents,
    )
  }

  private fun contentPendingIntent(): PendingIntent? {
    val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return null
    return PendingIntent.getActivity(
      this,
      0,
      launchIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  private fun servicePendingIntent(action: String): PendingIntent {
    val intent = Intent(this, RoutineRunService::class.java).setAction(action)
    return PendingIntent.getService(
      this,
      action.hashCode(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  companion object {
    private const val TAG = "RoutineRunService"
    private const val NOTIFICATION_ID = 1247
    const val ACTION_START = "com.elementary.tasks.routine.action.START"
    const val ACTION_PAUSE = "com.elementary.tasks.routine.action.PAUSE"
    const val ACTION_RESUME = "com.elementary.tasks.routine.action.RESUME"
    const val ACTION_SKIP = "com.elementary.tasks.routine.action.SKIP"
    const val ACTION_STOP = "com.elementary.tasks.routine.action.STOP"
  }
}
