package com.github.naz013.logic.routine

import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.sync.IsProUserUseCase
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class HabitConfigImplTest {

  private val featureFlags = mockk<FeatureFlags>()
  private val isProUser = mockk<IsProUserUseCase>()

  private fun config(flagEnabled: Boolean, pro: Boolean): HabitConfigImpl {
    every { featureFlags.isEnabled(FeatureFlag.HABIT_ENABLED) } returns flagEnabled
    every { isProUser() } returns pro
    return HabitConfigImpl(featureFlags, isProUser)
  }

  @Test
  fun `is enabled when flag is on and user is pro`() {
    assertEquals(true, config(flagEnabled = true, pro = true).isEnabled)
  }

  @Test
  fun `is disabled when flag is on but user is not pro`() {
    assertEquals(false, config(flagEnabled = true, pro = false).isEnabled)
  }

  @Test
  fun `is disabled when flag is off and user is pro`() {
    assertEquals(false, config(flagEnabled = false, pro = true).isEnabled)
  }

  @Test
  fun `is disabled when flag is off and user is not pro`() {
    assertEquals(false, config(flagEnabled = false, pro = false).isEnabled)
  }
}
