package com.github.naz013.workapi

/**
 * A unit of background work that can be scheduled through [WorkScheduler] without the
 * implementing module depending on any Android or WorkManager API.
 *
 * Implementations are looked up by [WorkRequest.taskKey] / [PeriodicWorkRequest.taskKey] at
 * execution time, so each implementation must be registered in DI under that same key.
 */
fun interface BackgroundTask {
  suspend fun run(
    input: TaskData,
    progress: TaskProgressReporter,
  ): TaskResult
}
