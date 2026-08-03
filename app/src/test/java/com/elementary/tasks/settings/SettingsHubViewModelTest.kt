package com.elementary.tasks.settings

import androidx.lifecycle.ViewModelStore
import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.utils.datetime.DoNotDisturbManager
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.params.PrefsConstants
import com.elementary.tasks.core.utils.params.RemotePrefs
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.common.TextProvider
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.common.system.SystemInfo
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
  private val remotePrefs = mockk<RemotePrefs>(relaxed = true)
  private val prefs = mockk<Prefs>()
  private val doNotDisturbManager = mockk<DoNotDisturbManager>()
  private val textProvider = mockk<TextProvider>(relaxed = true)
  private val buildInfo = mockk<BuildInfo>()
  private val systemInfo = mockk<SystemInfo>()
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)

  private lateinit var viewModel: SettingsHubViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { prefs.hasPinCode } returns false
    every { prefs.addObserver(any(), any()) } just Runs
    every { prefs.removeObserver(any(), any()) } just Runs
    every { doNotDisturbManager.applyDoNotDisturb(0, any()) } returns false
    every { buildInfo.isPro } returns false
    every { systemInfo.isProAppInstalled } returns false
    every { systemInfo.googlePlayServicesAvailable } returns true

    viewModel = newViewModel()
  }

  private fun newViewModel() =
    SettingsHubViewModel(
      remotePrefs = remotePrefs,
      prefs = prefs,
      doNotDisturbManager = doNotDisturbManager,
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
      assertFalse(state.isInsightsVisible)
    }

  @Test
  fun `shows insights entry only for pro users`() =
    runTest {
      every { buildInfo.isPro } returns true

      val state = newViewModel().state.first()

      assertTrue(state.isInsightsVisible)
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
      every { prefs.hasPinCode } returns true

      val state = newViewModel().state.first()

      assertTrue(state.hasPinCode)
    }

  @Test
  fun `collecting state registers prefs observers and checks do not disturb`() =
    runTest {
      viewModel.state.first()

      verify { prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_ENABLED, any()) }
      verify { prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_FROM, any()) }
      verify { prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_TO, any()) }
      verify { prefs.addObserver(PrefsConstants.DO_NOT_DISTURB_IGNORE, any()) }
      verify { doNotDisturbManager.applyDoNotDisturb(0, any()) }
    }

  @Test
  fun `isDoNotDisturbActive reflects the do not disturb manager`() =
    runTest {
      every { doNotDisturbManager.applyDoNotDisturb(0, any()) } returns true

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
  fun `onCleared removes prefs and remote prefs observers`() {
    val store = ViewModelStore()
    store.put("settingsHub", viewModel)

    store.clear()

    verify { prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_ENABLED, any()) }
    verify { prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_FROM, any()) }
    verify { prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_TO, any()) }
    verify { prefs.removeObserver(PrefsConstants.DO_NOT_DISTURB_IGNORE, any()) }
    verify { remotePrefs.removeUpdateObserver(viewModel) }
    verify { remotePrefs.removeMessageObserver(viewModel) }
  }
}
