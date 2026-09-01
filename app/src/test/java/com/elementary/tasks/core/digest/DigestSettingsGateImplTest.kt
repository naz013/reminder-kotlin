package com.elementary.tasks.core.digest

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

class DigestSettingsGateImplTest {
  private val featureFlags = mockk<FeatureFlags>()
  private val prefs = mockk<Prefs>()
  private lateinit var gate: DigestSettingsGateImpl

  @Before
  fun setUp() {
    gate = DigestSettingsGateImpl(featureFlags, prefs)
  }

  @Test
  fun `isDailyEnabled is true only when both the remote flag and the user toggle are on`() {
    every { featureFlags.isEnabled(FeatureFlag.AI_DIGEST) } returns true
    every { prefs.aiDigestDailyEnabled } returns true

    assertTrue(gate.isDailyEnabled())
  }

  @Test
  fun `isDailyEnabled is false when the remote flag is off even if the user toggle is on`() {
    every { featureFlags.isEnabled(FeatureFlag.AI_DIGEST) } returns false
    every { prefs.aiDigestDailyEnabled } returns true

    assertFalse(gate.isDailyEnabled())
  }

  @Test
  fun `isDailyEnabled is false when the user toggle is off even if the remote flag is on`() {
    every { featureFlags.isEnabled(FeatureFlag.AI_DIGEST) } returns true
    every { prefs.aiDigestDailyEnabled } returns false

    assertFalse(gate.isDailyEnabled())
  }

  @Test
  fun `preferredHour delegates to prefs`() {
    every { prefs.aiDigestHour } returns 7

    assertEquals(7, gate.preferredHour())
  }
}
