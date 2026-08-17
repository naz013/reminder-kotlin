package com.github.naz013.feature.settings

import androidx.lifecycle.ViewModelStore
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.feature.settings.security.SecuritySettingsPreferences
import com.github.naz013.platform.SystemInfo
import com.github.naz013.testing.BaseTest
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
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

  private lateinit var viewModel: SettingsHubViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { securityPreferences.hasPinCode } returns false
    every { doNotDisturbChecker.addChangeObserver(any()) } just Runs
    every { doNotDisturbChecker.removeChangeObserver(any()) } just Runs
    every { doNotDisturbChecker.isActive() } returns false
    every { buildInfo.isPro } returns false
    every { buildInfo.isDebug } returns false
    every { systemInfo.isProAppInstalled } returns false
    every { systemInfo.googlePlayServicesAvailable } returns true

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
      analyticsEventSender = analyticsEventSender,
    )

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
