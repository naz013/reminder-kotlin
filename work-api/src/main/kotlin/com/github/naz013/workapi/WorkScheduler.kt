package com.github.naz013.workapi

import kotlinx.coroutines.flow.Flow

/**
 * Convenient facade over the platform's background execution mechanism (WorkManager on
 * Android). Other modules depend only on this interface and on [BackgroundTask] to
 * schedule and observe background work, without pulling in any Android or WorkManager API.
 */
interface WorkScheduler {
  /** Enqueues [request] to run once, returning its tag. */
  fun enqueue(request: WorkRequest): String

  /** Enqueues [request] as unique work named [uniqueName], applying [policy] if work with that name already exists. */
  fun enqueueUnique(
    uniqueName: String,
    policy: ExistingWorkPolicy,
    request: WorkRequest,
  ): String

  /** Enqueues [request] to run repeatedly. */
  fun enqueuePeriodic(request: PeriodicWorkRequest): String

  /** Cancels all work carrying [tag]. */
  fun cancelByTag(tag: String)

  /** Cancels the unique work named [uniqueName]. */
  fun cancelUniqueWork(uniqueName: String)

  /** Observes the state of the unique work named [uniqueName]. */
  fun observeUniqueWork(uniqueName: String): Flow<WorkState>
}
