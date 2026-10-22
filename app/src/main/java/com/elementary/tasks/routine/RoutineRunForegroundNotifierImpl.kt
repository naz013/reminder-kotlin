package com.elementary.tasks.routine

import android.content.Context
import android.content.Intent
import com.github.naz013.feature.routine.run.RoutineRunForegroundNotifier

/** Kicks [RoutineRunService] into or out of existence; the service observes the engine state itself. */
internal class RoutineRunForegroundNotifierImpl(
  private val context: Context,
) : RoutineRunForegroundNotifier {

  override fun start() {
    val intent = Intent(context, RoutineRunService::class.java).setAction(RoutineRunService.ACTION_START)
    context.startForegroundService(intent)
  }

  override fun stop() {
    context.stopService(Intent(context, RoutineRunService::class.java))
  }
}
