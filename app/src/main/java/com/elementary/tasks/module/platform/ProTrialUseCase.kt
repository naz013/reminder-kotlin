package com.elementary.tasks.module.platform

import com.elementary.tasks.core.utils.params.Prefs
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

internal class ProTrialUseCase(
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
      delay(POLL_MILLIS)
    }
  }.distinctUntilChanged()

  private companion object {
    const val POLL_MILLIS = 1000L
  }
}
