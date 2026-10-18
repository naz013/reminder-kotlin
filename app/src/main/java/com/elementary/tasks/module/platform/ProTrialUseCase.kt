package com.elementary.tasks.module.platform

import com.elementary.tasks.core.utils.params.Prefs
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProTrialUseCase internal constructor(
  private val prefs: Prefs,
  private val proTrial: ProTrial,
) {
  constructor(prefs: Prefs) : this(prefs, ProTrial())

  fun activate() {
    prefs.proTrialExpiryMillis = proTrial.activationExpiry()
  }

  fun observeRemainingMillis(): Flow<Long> = flow {
    while (true) {
      val remaining = proTrial.remainingMillis(prefs.proTrialExpiryMillis)
      emit(remaining)
      if (remaining == 0L) {
        delay(IDLE_POLL_MILLIS)
      } else {
        delay(remaining.coerceAtMost(TICK_MILLIS))
      }
    }
  }

  private companion object {
    const val TICK_MILLIS = 1000L
    const val IDLE_POLL_MILLIS = 1000L
  }
}
