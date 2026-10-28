package com.elementary.tasks.module.platform

import java.util.concurrent.TimeUnit

internal fun interface TimeSource {
  fun nowMillis(): Long
}

internal object SystemTimeSource : TimeSource {
  override fun nowMillis(): Long = System.currentTimeMillis()
}

internal class ProTrial(
  private val timeSource: TimeSource = SystemTimeSource,
) {
  fun activationExpiry(): Long = timeSource.nowMillis() + TRIAL_DURATION_MILLIS

  fun remainingMillis(expiryMillis: Long): Long =
    (expiryMillis - timeSource.nowMillis()).coerceAtLeast(0L)

  fun isExpired(expiryMillis: Long): Boolean = timeSource.nowMillis() >= expiryMillis

  fun extendedExpiry(expiryMillis: Long): Long =
    maxOf(expiryMillis, timeSource.nowMillis()) + EXTENSION_MILLIS

  fun canExtend(remainingMillis: Long): Boolean =
    remainingMillis > 0L && remainingMillis + EXTENSION_MILLIS <= MAX_REMAINING_MILLIS

  companion object {
    val TRIAL_DURATION_MILLIS: Long = TimeUnit.HOURS.toMillis(24)
    val EXTENSION_MILLIS: Long = TimeUnit.HOURS.toMillis(24)
    val MAX_REMAINING_MILLIS: Long = TimeUnit.DAYS.toMillis(5)
  }
}
