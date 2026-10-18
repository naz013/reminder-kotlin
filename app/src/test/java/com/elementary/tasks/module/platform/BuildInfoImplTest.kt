package com.elementary.tasks.module.platform

import com.elementary.tasks.BuildConfig
import com.elementary.tasks.core.utils.params.Prefs
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class BuildInfoImplTest {

  @Test
  fun `isPro reports build flag when trial was never activated`() {
    val prefs = mockk<Prefs>()
    every { prefs.proTrialExpiryMillis } returns 0L
    val buildInfo = BuildInfoImpl(prefs)

    assertEquals(BuildConfig.IS_PRO, buildInfo.isPro)
  }

  @Test
  fun `isPro is true while trial expiry is in the future`() {
    val prefs = mockk<Prefs>()
    every { prefs.proTrialExpiryMillis } returns Long.MAX_VALUE
    val buildInfo = BuildInfoImpl(prefs)

    assertEquals(true, buildInfo.isPro)
  }
}
