package com.github.naz013.common.system

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

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

fun ProTrialController.observeIsActive(): Flow<Boolean> =
  observeRemainingMillis().map { it > 0L }.distinctUntilChanged()
