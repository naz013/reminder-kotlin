package com.elementary.tasks.module.platform

import android.app.Activity
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.ProTrialActivatedEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProTrialAdsUseCaseTest {

  private val activity = mockk<Activity>()
  private val rewardedTrialAds = mockk<RewardedTrialAds>()
  private val proTrialUseCase = mockk<ProTrialUseCase>(relaxed = true)
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val useCase = ProTrialAdsUseCase(rewardedTrialAds, proTrialUseCase, analyticsEventSender)

  @Test
  fun `run returns success after three rewarded ads`() = runTest {
    givenAdResults(RewardedAdResult.REWARDED, RewardedAdResult.REWARDED, RewardedAdResult.REWARDED)

    val result = useCase.run(activity)

    assertEquals(ProTrialAdsResult.SUCCESS, result)
  }

  @Test
  fun `run activates trial exactly once after three rewarded ads`() = runTest {
    givenAdResults(RewardedAdResult.REWARDED, RewardedAdResult.REWARDED, RewardedAdResult.REWARDED)

    useCase.run(activity)

    verify(exactly = 1) { proTrialUseCase.activate() }
  }

  @Test
  fun `run sends trial activated event after three rewarded ads`() = runTest {
    givenAdResults(RewardedAdResult.REWARDED, RewardedAdResult.REWARDED, RewardedAdResult.REWARDED)

    useCase.run(activity)

    verify(exactly = 1) { analyticsEventSender.send(ProTrialActivatedEvent) }
  }

  @Test
  fun `run shows exactly three ads when every ad is rewarded`() = runTest {
    givenAdResults(RewardedAdResult.REWARDED, RewardedAdResult.REWARDED, RewardedAdResult.REWARDED)

    useCase.run(activity)

    coVerify(exactly = 3) { rewardedTrialAds.show(activity) }
  }

  @Test
  fun `run does not activate trial when ads are unavailable mid-flow`() = runTest {
    givenAdResults(RewardedAdResult.REWARDED, RewardedAdResult.UNAVAILABLE)

    useCase.run(activity)

    verify(exactly = 0) { proTrialUseCase.activate() }
  }

  @Test
  fun `run returns ads unavailable when an ad is unavailable mid-flow`() = runTest {
    givenAdResults(RewardedAdResult.REWARDED, RewardedAdResult.UNAVAILABLE)

    val result = useCase.run(activity)

    assertEquals(ProTrialAdsResult.ADS_UNAVAILABLE, result)
  }

  @Test
  fun `run does not send trial activated event when ads are unavailable`() = runTest {
    givenAdResults(RewardedAdResult.UNAVAILABLE)

    useCase.run(activity)

    verify(exactly = 0) { analyticsEventSender.send(any()) }
  }

  @Test
  fun `run resets progress when ads are unavailable mid-flow`() = runTest {
    givenAdResults(RewardedAdResult.REWARDED, RewardedAdResult.REWARDED, RewardedAdResult.UNAVAILABLE)

    useCase.run(activity)

    assertEquals(0, useCase.adsWatched.value)
  }

  @Test
  fun `run does not activate trial when user dismisses ad mid-flow`() = runTest {
    givenAdResults(RewardedAdResult.REWARDED, RewardedAdResult.DISMISSED)

    useCase.run(activity)

    verify(exactly = 0) { proTrialUseCase.activate() }
  }

  @Test
  fun `run returns cancelled when user dismisses ad mid-flow`() = runTest {
    givenAdResults(RewardedAdResult.REWARDED, RewardedAdResult.REWARDED, RewardedAdResult.DISMISSED)

    val result = useCase.run(activity)

    assertEquals(ProTrialAdsResult.CANCELLED, result)
  }

  @Test
  fun `run does not send trial activated event when user dismisses ad`() = runTest {
    givenAdResults(RewardedAdResult.REWARDED, RewardedAdResult.DISMISSED)

    useCase.run(activity)

    verify(exactly = 0) { analyticsEventSender.send(any()) }
  }

  @Test
  fun `run resets progress when user dismisses ad mid-flow`() = runTest {
    givenAdResults(RewardedAdResult.REWARDED, RewardedAdResult.REWARDED, RewardedAdResult.DISMISSED)

    useCase.run(activity)

    assertEquals(0, useCase.adsWatched.value)
  }

  @Test
  fun `run stops showing ads after the first dismissal`() = runTest {
    givenAdResults(RewardedAdResult.DISMISSED)

    useCase.run(activity)

    coVerify(exactly = 1) { rewardedTrialAds.show(activity) }
  }

  @Test
  fun `run starts from zero progress on a second run after a cancelled first run`() = runTest {
    givenAdResults(RewardedAdResult.REWARDED, RewardedAdResult.REWARDED, RewardedAdResult.DISMISSED)
    useCase.run(activity)
    givenAdResults(RewardedAdResult.REWARDED, RewardedAdResult.DISMISSED)

    val result = useCase.run(activity)

    assertEquals(ProTrialAdsResult.CANCELLED, result)
    assertEquals(0, useCase.adsWatched.value)
    verify(exactly = 0) { proTrialUseCase.activate() }
  }

  @Test
  fun `run activates trial again when a second run completes all three ads`() = runTest {
    givenAdResults(RewardedAdResult.REWARDED, RewardedAdResult.REWARDED, RewardedAdResult.REWARDED)
    useCase.run(activity)
    givenAdResults(RewardedAdResult.REWARDED, RewardedAdResult.REWARDED, RewardedAdResult.REWARDED)

    val result = useCase.run(activity)

    assertEquals(ProTrialAdsResult.SUCCESS, result)
    verify(exactly = 2) { proTrialUseCase.activate() }
    verify(exactly = 2) { analyticsEventSender.send(ProTrialActivatedEvent) }
  }

  @Test
  fun `run does not carry rewards from a failed run into a successful one`() = runTest {
    givenAdResults(RewardedAdResult.REWARDED, RewardedAdResult.UNAVAILABLE)
    useCase.run(activity)
    givenAdResults(RewardedAdResult.REWARDED, RewardedAdResult.REWARDED, RewardedAdResult.REWARDED)

    val result = useCase.run(activity)

    assertEquals(ProTrialAdsResult.SUCCESS, result)
    verify(exactly = 1) { proTrialUseCase.activate() }
  }

  private fun givenAdResults(vararg results: RewardedAdResult) {
    coEvery { rewardedTrialAds.show(any()) } returnsMany results.toList()
  }
}
