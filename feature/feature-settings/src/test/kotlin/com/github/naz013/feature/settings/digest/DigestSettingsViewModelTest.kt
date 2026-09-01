package com.github.naz013.feature.settings.digest

import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.digestapi.DigestScheduler
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DigestSettingsViewModelTest {
  private val prefs = mockk<DigestSettingsPreferences>(relaxed = true)
  private val digestScheduler = mockk<DigestScheduler>(relaxed = true)
  private val analyticsEventSender = mockk<AnalyticsEventSender>(relaxed = true)

  private lateinit var viewModel: DigestSettingsViewModel

  @Before
  fun setUp() {
    every { prefs.aiDigestDailyEnabled } returns false
    every { prefs.aiDigestHour } returns 8

    viewModel = DigestSettingsViewModel(prefs, digestScheduler, analyticsEventSender)
  }

  @Test
  fun `initial state reflects the current prefs`() {
    assertFalse(viewModel.state.value.isDailyEnabled)
    assertEquals(8, viewModel.state.value.hour)
  }

  @Test
  fun `onDailyToggle flips the pref and enables the scheduler when turning on`() {
    every { prefs.aiDigestDailyEnabled } returns false andThen true

    viewModel.onDailyToggle()

    verify { prefs.aiDigestDailyEnabled = true }
    verify { digestScheduler.enable() }
    assertTrue(viewModel.state.value.isDailyEnabled)
  }

  @Test
  fun `onDailyToggle flips the pref and disables the scheduler when turning off`() {
    every { prefs.aiDigestDailyEnabled } returns true andThen false

    viewModel.onDailyToggle()

    verify { prefs.aiDigestDailyEnabled = false }
    verify { digestScheduler.disable() }
  }

  @Test
  fun `onHourSelected updates the hour pref`() {
    every { prefs.aiDigestHour } returns 20

    viewModel.onHourSelected(20)

    verify { prefs.aiDigestHour = 20 }
    assertEquals(20, viewModel.state.value.hour)
  }
}
