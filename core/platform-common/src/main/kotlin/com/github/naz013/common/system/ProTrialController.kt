package com.github.naz013.common.system

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.Locale

enum class ProTrialResult {
  SUCCESS,
  ADS_UNAVAILABLE,
  CANCELLED,
}

/** Temporary PRO trial: observe the remaining time and earn it by watching rewarded ads. */
interface ProTrialController {
  val requiredAds: Int

  /** Rewarded ads watched in the current run; resets to 0 when the run ends without success. */
  val adsWatched: StateFlow<Int>

  /** Remaining trial time in millis, `0` when the trial is not active. Re-emits as time passes. */
  fun observeRemainingMillis(): Flow<Long>

  suspend fun watchAdsAndActivate(activity: Activity): ProTrialResult
}

/** Formats remaining trial time as `hh:mm`, rounding a partial minute up; empty when nothing remains. */
fun formatProTrialRemaining(remainingMillis: Long): String {
  if (remainingMillis <= 0L) return ""
  val totalMinutes = (remainingMillis + MILLIS_IN_MINUTE - 1) / MILLIS_IN_MINUTE
  return String.format(Locale.ROOT, "%02d:%02d", totalMinutes / MINUTES_IN_HOUR, totalMinutes % MINUTES_IN_HOUR)
}

private const val MILLIS_IN_MINUTE = 60_000L
private const val MINUTES_IN_HOUR = 60L

fun ProTrialController.observeIsActive(): Flow<Boolean> =
  observeRemainingMillis().map { it > 0L }.distinctUntilChanged()
