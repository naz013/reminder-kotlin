package com.elementary.tasks.settings.security

import com.elementary.tasks.BaseTest
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.platform.SystemInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SecuritySettingsViewModelTest : BaseTest() {
  private val prefs = mockk<Prefs>(relaxed = true)
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)
  private val systemInfo = mockk<SystemInfo>()

  private lateinit var viewModel: SecuritySettingsViewModel

  @Before
  override fun setUp() {
    super.setUp()
    every { prefs.hasPinCode } returns false
    every { prefs.useFingerprint } returns false
    every { prefs.shufflePinView } returns false
    every { prefs.isTelephonyEnabled } returns true
    every { systemInfo.hasBiometricHardware } returns true
    every { systemInfo.hasTelephony } returns true

    viewModel =
      SecuritySettingsViewModel(
        prefs = prefs,
        analyticsEventSender = analyticsEventSender,
        systemInfo = systemInfo,
      )
  }

  @Test
  fun `init sends the security settings screen analytics event`() {
    verify { analyticsEventSender.send(ScreenUsedEvent(Screen.SECURITY_SETTINGS)) }
  }

  @Test
  fun `state reflects prefs and system info on first collection`() =
    runTest {
      every { prefs.hasPinCode } returns true
      every { prefs.useFingerprint } returns true
      every { prefs.shufflePinView } returns true

      val state = viewModel.state.first()

      assertEquals(true, state.isPinChecked)
      assertEquals(true, state.isFingerprintChecked)
      assertEquals(true, state.isShuffleChecked)
      assertEquals(true, state.hasBiometricHardware)
      assertEquals(true, state.hasTelephony)
    }

  @Test
  fun `loading state forces telephony pref off when the device has no telephony hardware`() =
    runTest {
      every { systemInfo.hasTelephony } returns false
      every { prefs.isTelephonyEnabled } returns false

      viewModel.state.first()

      verify { prefs.isTelephonyEnabled = false }
    }

  @Test
  fun `loading state leaves telephony pref untouched when the device has telephony hardware`() =
    runTest {
      every { systemInfo.hasTelephony } returns true

      viewModel.state.first()

      verify(exactly = 0) { prefs.isTelephonyEnabled = any() }
    }

  @Test
  fun `onPinRowClick opens AddPin when no pin is currently set`() =
    runTest {
      every { prefs.hasPinCode } returns false
      viewModel.state.first()

      viewModel.onPinRowClick()

      val event = viewModel.navigationEvent.value?.peekContent()
      assertEquals(SecuritySettingsEvent.OpenAddPin, event)
    }

  @Test
  fun `onPinRowClick opens DisablePin when a pin is currently set`() =
    runTest {
      every { prefs.hasPinCode } returns true
      viewModel.state.first()

      viewModel.onPinRowClick()

      val event = viewModel.navigationEvent.value?.peekContent()
      assertEquals(SecuritySettingsEvent.OpenDisablePin, event)
    }

  @Test
  fun `onChangePinClick opens the change-pin screen`() {
    viewModel.onChangePinClick()

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(SecuritySettingsEvent.OpenChangePin, event)
  }

  @Test
  fun `onBiometricAuthClicked requests a biometric login attempt`() {
    viewModel.onBiometricAuthClicked()

    val event = viewModel.navigationEvent.value?.peekContent()
    assertEquals(SecuritySettingsEvent.TryBiometricLogin, event)
  }

  @Test
  fun `onBiometricAuthSuccess toggles the fingerprint pref`() {
    every { prefs.useFingerprint } returns false

    viewModel.onBiometricAuthSuccess()

    verify { prefs.useFingerprint = true }
  }

  @Test
  fun `onShuffleToggle toggles the shuffle pref`() {
    every { prefs.shufflePinView } returns true

    viewModel.onShuffleToggle()

    verify { prefs.shufflePinView = false }
  }

  @Test
  fun `onTelephonyToggle toggles the telephony pref`() {
    every { prefs.isTelephonyEnabled } returns false
    every { systemInfo.hasTelephony } returns true

    viewModel.onTelephonyToggle()

    verify { prefs.isTelephonyEnabled = true }
  }
}
