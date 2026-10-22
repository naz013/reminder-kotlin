package com.github.naz013.feature.routine.run

/** Seam implemented in `app`: starts/stops the foreground service that keeps a run alive in the background. */
interface RoutineRunForegroundNotifier {
  fun start()

  fun stop()
}
