package com.github.naz013.feature.settings

import androidx.lifecycle.ViewModelStore
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.ProTrialPromoClickedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.platform.BuildInfo
import com.github.naz013.common.system.ProTrialController
import com.github.naz013.feature.settings.security.SecuritySettingsPreferences
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.platform.SystemInfo
import com.github.naz013.testing.BaseTest
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsHubViewModelTest : BaseTest() {
  private val remoteMessages = mockk<SettingsHubRemoteMessages>(relaxed = true)
  private val securityPreferences = mockk<SecuritySettingsPreferences>()
  private val doNotDisturbChecker = mockk<SettingsHubDoNotDisturbChecker>()
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val buildInfo = mockk<BuildInfo>()
  private val systemInfo = mockk<SystemInfo>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val proTrialController = mockk<ProTrialController>()
  private val featureFlags = mockk<FeatureFlags>()
  private val remainingMillis = MutableStateFlow(0L)

  private lateinit var viewModel: SettingsHubViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { securityPreferences.hasPinCode } returns false
    every { doNotDisturbChecker.addChangeObserver(any()) } just Runs
    every { doNotDisturbChecker.removeChangeObserver(any()) } just Runs
    every { doNotDisturbChecker.isActive() } returns false
    every { buildInfo.isPro } returns false
    every { buildInfo.isBuildPro } returns false
    every { buildInfo.isDebug } returns false
    every { proTrialController.observeRemainingMillis() } returns remainingMillis
    every { systemInfo.isProAppInstalled } returns false
    every { systemInfo.googlePlayServicesAvailable } returns true
    every { featureFlags.isEnabled(FeatureFlag.PRO_TRIAL_BANNER) } returns true

    viewModel = newViewModel()
  }

  private fun newViewModel() =
    SettingsHubViewModel(
      remoteMessages = remoteMessages,
      securityPreferences = securityPreferences,
      doNotDisturbChecker = doNotDisturbChecker,
      textProvider = textProvider,
      buildInfo = buildInfo,
      systemInfo = systemInfo,
      proTrialController = proTrialController,
      analyticsEventSender = analyticsEventSender,
      featureFlags = featureFlags,
    )

  @Test
  fun `shows the trial promo when the build is not pro and the trial is inactive`() =
    runTest {
      val state = viewModel.state.first()

      assertTrue(state.isTrialPromoVisible)
    }

  @Test
  fun `hides the trial promo when the trial banner flag is disabled`() =
    runTest {
      every { featureFlags.isEnabled(FeatureFlag.PRO_TRIAL_BANNER) } returns false

      val state = newViewModel().state.first()

      assertFalse(state.isTrialPromoVisible)
    }

  @Test
  fun `hides the trial promo when the build is pro`() =
    runTest {
      every { buildInfo.isBuildPro } returns true

      val state = newViewModel().state.first()

      assertFalse(state.isTrialPromoVisible)
    }

  @Test
  fun `hides the trial promo and the buy pro badge while the trial is active`() =
    runTest {
      every { buildInfo.isPro } returns true
      remainingMillis.value = 1_000L

      val state = newViewModel().state.first()

      assertFalse(state.isTrialPromoVisible)
      assertFalse(state.isBuyProBadgeVisible)
    }

  @Test
  fun `shows the trial promo again when the trial expires`() =
    runTest {
      every { buildInfo.isPro } returns true
      remainingMillis.value = 1_000L
      val viewModel = newViewModel()
      val states = mutableListOf<SettingsHubState>()
      val job = launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.toList(states) }

      every { buildInfo.isPro } returns false
      remainingMillis.value = 0L

      assertTrue(states.last().isTrialPromoVisible)
      assertTrue(states.last().isBuyProBadgeVisible)
      job.cancel()
    }

  @Test
  fun `proTrialRemaining is null while the trial is inactive`() =
    runTest {
      assertNull(viewModel.state.first().proTrialRemaining)
    }

  @Test
  fun `proTrialRemaining shows hours and minutes while the trial is active`() =
    runTest {
      remainingMillis.value = 2 * 3_600_000L + 30 * 60_000L

      assertEquals("02:30", newViewModel().state.first().proTrialRemaining)
    }

  @Test
  fun `onTrialPromoClick sends the trial promo clicked event`() {
    viewModel.onTrialPromoClick()

    verify { analyticsEventSender.send(ProTrialPromoClickedEvent) }
  }

  @Test
  fun `sends screen used analytics event on creation`() {
    verify { analyticsEventSender.send(any()) }
  }

  @Test
  fun `builds initial state from build info, system info and prefs`() =
    runTest {
      val state = viewModel.state.first()

      assertTrue(state.isBuyProBadgeVisible)
      assertFalse(state.isPlayServicesWarningVisible)
      assertFalse(state.hasPinCode)
    }

  @Test
  fun `hides the buy pro badge when already pro`() =
    runTest {
      every { buildInfo.isPro } returns true

      val state = newViewModel().state.first()

      assertFalse(state.isBuyProBadgeVisible)
    }

  @Test
  fun `hides the buy pro badge when the pro app is already installed`() =
    runTest {
      every { systemInfo.isProAppInstalled } returns true

      val state = newViewModel().state.first()

      assertFalse(state.isBuyProBadgeVisible)
    }

  @Test
  fun `shows play services warning when unavailable`() =
    runTest {
      every { systemInfo.googlePlayServicesAvailable } returns false

      val state = newViewModel().state.first()

      assertTrue(state.isPlayServicesWarningVisible)
    }

  @Test
  fun `reflects pin code presence from prefs`() =
    runTest {
      every { securityPreferences.hasPinCode } returns true

      val state = newViewModel().state.first()

      assertTrue(state.hasPinCode)
    }

  @Test
  fun `shows developer option when build is debug`() =
    runTest {
      every { buildInfo.isDebug } returns true

      val state = newViewModel().state.first()

      assertTrue(state.isDeveloperOptionVisible)
    }

  @Test
  fun `collecting state registers do not disturb observer and checks do not disturb`() =
    runTest {
      viewModel.state.first()

      verify { doNotDisturbChecker.addChangeObserver(any()) }
      verify { doNotDisturbChecker.isActive() }
    }

  @Test
  fun `isDoNotDisturbActive reflects the do not disturb checker`() =
    runTest {
      every { doNotDisturbChecker.isActive() } returns true

      val state = viewModel.state.first()

      assertTrue(state.isDoNotDisturbActive)
    }

  @Test
  fun `onUpdateChanged sets the update message when there is an update`() =
    runTest {
      every { textProvider.getString(any(), "2.0") } returns "Update to 2.0"

      viewModel.onUpdateChanged(hasUpdate = true, version = "2.0")

      assertEquals("Update to 2.0", viewModel.state.first().updateMessage)
    }

  @Test
  fun `onUpdateChanged clears the update message when there is no update`() =
    runTest {
      every { textProvider.getString(any(), "2.0") } returns "Update to 2.0"
      viewModel.onUpdateChanged(hasUpdate = true, version = "2.0")

      viewModel.onUpdateChanged(hasUpdate = false, version = "")

      assertNull(viewModel.state.first().updateMessage)
    }

  @Test
  fun `onSaleChanged sets the sale message when a discount is active`() =
    runTest {
      every { textProvider.getString(any(), "20%", "tomorrow") } returns "20% off until tomorrow"

      viewModel.onSaleChanged(showDiscount = true, discount = "20%", until = "tomorrow")

      assertEquals("20% off until tomorrow", viewModel.state.first().saleMessage)
    }

  @Test
  fun `onSaleChanged clears the sale message when there is no discount`() =
    runTest {
      viewModel.onSaleChanged(showDiscount = false, discount = "", until = "")

      assertNull(viewModel.state.first().saleMessage)
    }

  @Test
  fun `onMessageChanged sets the internal message when shown`() =
    runTest {
      viewModel.onMessageChanged(showMessage = true, message = "Hello there")

      assertEquals("Hello there", viewModel.state.first().internalMessage)
    }

  @Test
  fun `onMessageChanged clears the internal message when hidden`() =
    runTest {
      viewModel.onMessageChanged(showMessage = true, message = "Hello there")

      viewModel.onMessageChanged(showMessage = false, message = "")

      assertNull(viewModel.state.first().internalMessage)
    }

  @Test
  fun `onCleared removes do not disturb and remote message observers`() {
    val store = ViewModelStore()
    store.put("settingsHub", viewModel)

    store.clear()

    verify { doNotDisturbChecker.removeChangeObserver(any()) }
    verify { remoteMessages.removeUpdateObserver(viewModel) }
    verify { remoteMessages.removeMessageObserver(viewModel) }
  }
}
