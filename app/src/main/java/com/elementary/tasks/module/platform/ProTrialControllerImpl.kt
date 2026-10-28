package com.elementary.tasks.module.platform

import android.app.Activity
import com.github.naz013.common.system.ProTrialController
import com.github.naz013.common.system.ProTrialResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

internal class ProTrialControllerImpl(
  private val proTrialUseCase: ProTrialUseCase,
  private val proTrialAdsUseCase: ProTrialAdsUseCase,
) : ProTrialController {
  override val requiredAds: Int = ProTrialAdsUseCase.REQUIRED_ADS
  override val adsWatched: StateFlow<Int> = proTrialAdsUseCase.adsWatched

  override fun observeRemainingMillis(): Flow<Long> = proTrialUseCase.observeRemainingMillis()

  override fun observeCanExtend(): Flow<Boolean> = proTrialUseCase.observeCanExtend()

  override suspend fun watchAdsAndActivate(activity: Activity): ProTrialResult {
    return proTrialAdsUseCase.run(activity).toProTrialResult()
  }

  override suspend fun watchAdsAndExtend(activity: Activity): ProTrialResult {
    return proTrialAdsUseCase.runExtend(activity).toProTrialResult()
  }

  private fun ProTrialAdsResult.toProTrialResult(): ProTrialResult {
    return when (this) {
      ProTrialAdsResult.SUCCESS -> ProTrialResult.SUCCESS
      ProTrialAdsResult.ADS_UNAVAILABLE -> ProTrialResult.ADS_UNAVAILABLE
      ProTrialAdsResult.CANCELLED -> ProTrialResult.CANCELLED
      ProTrialAdsResult.LIMIT_REACHED -> ProTrialResult.LIMIT_REACHED
    }
  }
}
