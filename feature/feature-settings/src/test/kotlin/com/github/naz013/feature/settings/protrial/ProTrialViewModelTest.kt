package com.github.naz013.feature.settings.protrial

import android.app.Activity
import com.github.naz013.common.system.ProTrialController
import com.github.naz013.common.system.ProTrialResult
import com.github.naz013.testing.BaseTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProTrialViewModelTest : BaseTest() {
  private val proTrialController = mockk<ProTrialController>()
  private val activity = mockk<Activity>()
  private val remainingMillis = MutableStateFlow(0L)
  private val adsWatched = MutableStateFlow(0)

  @Before
  override fun setUp() {
    super.setUp()
    every { proTrialController.requiredAds } returns 3
    every { proTrialController.adsWatched } returns adsWatched
    every { proTrialController.observeRemainingMillis() } returns remainingMillis
    coEvery { proTrialController.watchAdsAndActivate(activity) } returns ProTrialResult.SUCCESS
  }

  private fun newViewModel() = ProTrialViewModel(proTrialController)

  @Test
  fun `initial state is idle with the required ads count`() {
    val state = newViewModel().state.value

    assertFalse(state.isTrialActive)
    assertFalse(state.isRunning)
    assertEquals("", state.remainingTime)
    assertEquals(3, state.requiredAds)
    assertNull(state.message)
  }

  @Test
  fun `shows remaining time as hours and minutes while the trial is active`() {
    remainingMillis.value = TimeUnit.HOURS.toMillis(5) + TimeUnit.MINUTES.toMillis(7)

    val state = newViewModel().state.value

    assertTrue(state.isTrialActive)
    assertEquals("05:07", state.remainingTime)
  }

  @Test
  fun `rounds a partial minute up so the last seconds still show time left`() {
    remainingMillis.value = TimeUnit.SECONDS.toMillis(20)

    assertEquals("00:01", newViewModel().state.value.remainingTime)
  }

  @Test
  fun `clears the remaining time when the trial expires`() {
    remainingMillis.value = TimeUnit.HOURS.toMillis(1)
    val viewModel = newViewModel()

    remainingMillis.value = 0L

    assertFalse(viewModel.state.value.isTrialActive)
    assertEquals("", viewModel.state.value.remainingTime)
  }

  @Test
  fun `reflects the number of ads watched`() {
    val viewModel = newViewModel()

    adsWatched.value = 2

    assertEquals(2, viewModel.state.value.adsWatched)
  }

  @Test
  fun `onActivateClick asks the screen for an activity`() {
    val viewModel = newViewModel()

    viewModel.onActivateClick()

    assertEquals(ProTrialEvent.RequestActivity, viewModel.event.value?.peekContent())
  }

  @Test
  fun `onActivityAvailable runs the ads flow and ends idle without a message on success`() {
    val viewModel = newViewModel()

    viewModel.onActivityAvailable(activity)

    coVerify(exactly = 1) { proTrialController.watchAdsAndActivate(activity) }
    assertFalse(viewModel.state.value.isRunning)
    assertNull(viewModel.state.value.message)
  }

  @Test
  fun `is running and ignores further clicks until the ads flow finishes`() {
    val result = CompletableDeferred<ProTrialResult>()
    coEvery { proTrialController.watchAdsAndActivate(activity) } coAnswers { result.await() }
    val viewModel = newViewModel()

    viewModel.onActivityAvailable(activity)
    viewModel.onActivateClick()
    viewModel.onActivityAvailable(activity)

    assertTrue(viewModel.state.value.isRunning)
    assertNull(viewModel.event.value)
    coVerify(exactly = 1) { proTrialController.watchAdsAndActivate(activity) }

    result.complete(ProTrialResult.SUCCESS)

    assertFalse(viewModel.state.value.isRunning)
  }

  @Test
  fun `shows the ads unavailable message when no ad can be shown`() {
    coEvery { proTrialController.watchAdsAndActivate(activity) } returns ProTrialResult.ADS_UNAVAILABLE
    val viewModel = newViewModel()

    viewModel.onActivityAvailable(activity)

    assertEquals(ProTrialMessage.ADS_UNAVAILABLE, viewModel.state.value.message)
    assertFalse(viewModel.state.value.isRunning)
  }

  @Test
  fun `shows the cancelled message when the user closes an ad early`() {
    coEvery { proTrialController.watchAdsAndActivate(activity) } returns ProTrialResult.CANCELLED
    val viewModel = newViewModel()

    viewModel.onActivityAvailable(activity)

    assertEquals(ProTrialMessage.CANCELLED, viewModel.state.value.message)
  }

  @Test
  fun `clears the previous message when a new run starts`() {
    coEvery { proTrialController.watchAdsAndActivate(activity) } returns ProTrialResult.CANCELLED
    val viewModel = newViewModel()
    viewModel.onActivityAvailable(activity)
    coEvery { proTrialController.watchAdsAndActivate(activity) } returns ProTrialResult.SUCCESS

    viewModel.onActivityAvailable(activity)

    assertNull(viewModel.state.value.message)
  }
}
