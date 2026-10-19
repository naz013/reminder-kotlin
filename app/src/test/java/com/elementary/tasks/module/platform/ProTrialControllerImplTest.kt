package com.elementary.tasks.module.platform

import android.app.Activity
import com.github.naz013.common.system.ProTrialResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProTrialControllerImplTest {

  private val activity = mockk<Activity>()
  private val proTrialUseCase = mockk<ProTrialUseCase>()
  private val proTrialAdsUseCase = mockk<ProTrialAdsUseCase>()
  private val adsWatched = MutableStateFlow(0)

  private lateinit var controller: ProTrialControllerImpl

  @Before
  fun setUp() {
    every { proTrialAdsUseCase.adsWatched } returns adsWatched
    controller = ProTrialControllerImpl(proTrialUseCase, proTrialAdsUseCase)
  }

  @Test
  fun `exposes the required ads count from the ads use case`() {
    assertEquals(ProTrialAdsUseCase.REQUIRED_ADS, controller.requiredAds)
  }

  @Test
  fun `exposes the ads watched state from the ads use case`() {
    adsWatched.value = 2

    assertEquals(2, controller.adsWatched.value)
  }

  @Test
  fun `forwards remaining trial time from the trial use case`() = runTest {
    every { proTrialUseCase.observeRemainingMillis() } returns flowOf(1_500L, 0L)

    assertEquals(listOf(1_500L, 0L), controller.observeRemainingMillis().toList())
  }

  @Test
  fun `maps a successful ads run to success result`() = runTest {
    coEvery { proTrialAdsUseCase.run(activity) } returns ProTrialAdsResult.SUCCESS

    assertEquals(ProTrialResult.SUCCESS, controller.watchAdsAndActivate(activity))
  }

  @Test
  fun `maps unavailable ads to ads unavailable result`() = runTest {
    coEvery { proTrialAdsUseCase.run(activity) } returns ProTrialAdsResult.ADS_UNAVAILABLE

    assertEquals(ProTrialResult.ADS_UNAVAILABLE, controller.watchAdsAndActivate(activity))
  }

  @Test
  fun `maps a dismissed ads run to cancelled result`() = runTest {
    coEvery { proTrialAdsUseCase.run(activity) } returns ProTrialAdsResult.CANCELLED

    assertEquals(ProTrialResult.CANCELLED, controller.watchAdsAndActivate(activity))
  }
}
