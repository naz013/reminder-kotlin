package com.elementary.tasks.routine

import android.content.Context
import android.content.Intent
import com.github.naz013.feature.routine.run.RoutineRunForegroundNotifier
import com.github.naz013.logging.Logger

/** Kicks [RoutineRunService] into or out of existence; the service observes the engine state itself. */
internal class RoutineRunForegroundNotifierImpl(
  private val context: Context,
) : RoutineRunForegroundNotifier {

  override fun start() {
    val intent = Intent(context, RoutineRunService::class.java).setAction(RoutineRunService.ACTION_START)
    runCatching { context.startForegroundService(intent) }
      .onFailure { Logger.w(TAG, "Start service failed: ${it.message}") }
  }

  override fun stop() {
    runCatching { context.stopService(Intent(context, RoutineRunService::class.java)) }
      .onFailure { Logger.w(TAG, "Stop service failed: ${it.message}") }
  }

  private companion object {
    private const val TAG = "RoutineRunForegroundNotifier"
  }
}
