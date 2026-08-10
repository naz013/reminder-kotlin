package com.elementary.tasks.calendar.holidays

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HolidaySettingsGateImplTest {
  private val featureFlags = mockk<FeatureFlags>()
  private val prefs = mockk<Prefs>()
  private lateinit var gate: HolidaySettingsGateImpl

  @Before
  fun setUp() {
    gate = HolidaySettingsGateImpl(featureFlags, prefs)
  }

  @Test
  fun `isEnabled is true only when both the remote flag and the user toggle are on`() {
    every { featureFlags.isEnabled(FeatureFlag.PUBLIC_HOLIDAYS) } returns true
    every { prefs.publicHolidaysEnabled } returns true

    assertTrue(gate.isEnabled())
  }

  @Test
  fun `isEnabled is false when the remote flag is off even if the user toggle is on`() {
    every { featureFlags.isEnabled(FeatureFlag.PUBLIC_HOLIDAYS) } returns false
    every { prefs.publicHolidaysEnabled } returns true

    assertFalse(gate.isEnabled())
  }

  @Test
  fun `isEnabled is false when the user toggle is off even if the remote flag is on`() {
    every { featureFlags.isEnabled(FeatureFlag.PUBLIC_HOLIDAYS) } returns true
    every { prefs.publicHolidaysEnabled } returns false

    assertFalse(gate.isEnabled())
  }

  @Test
  fun `countryCode delegates to prefs`() {
    every { prefs.holidayCountryCode } returns "FR"

    assertEquals("FR", gate.countryCode())
  }
}
