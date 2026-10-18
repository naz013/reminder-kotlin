package com.github.naz013.logic.workflow

import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkflowConfigImplTest {
  private val featureFlags = mockk<FeatureFlags>()
  private var isPro = true
  private val config = WorkflowConfigImpl(featureFlags) { isPro }

  @Test
  fun `isEnabled follows the feature flag regardless of pro`() {
    every { featureFlags.isEnabled(FeatureFlag.WORKFLOW_ENABLED) } returns true
    isPro = false

    assertTrue(config.isEnabled)
  }

  @Test
  fun `isRunnable is true when the flag is on and the user is pro`() {
    every { featureFlags.isEnabled(FeatureFlag.WORKFLOW_ENABLED) } returns true

    assertTrue(config.isRunnable)
  }

  @Test
  fun `isRunnable is false when the user is not pro`() {
    every { featureFlags.isEnabled(FeatureFlag.WORKFLOW_ENABLED) } returns true
    isPro = false

    assertFalse(config.isRunnable)
  }

  @Test
  fun `isRunnable is false when the flag is off even for pro`() {
    every { featureFlags.isEnabled(FeatureFlag.WORKFLOW_ENABLED) } returns false

    assertFalse(config.isRunnable)
  }

  @Test
  fun `isRunnable reacts at call time when the trial ends and starts again`() {
    every { featureFlags.isEnabled(FeatureFlag.WORKFLOW_ENABLED) } returns true

    assertTrue(config.isRunnable)
    isPro = false
    assertFalse(config.isRunnable)
    isPro = true
    assertTrue(config.isRunnable)
  }
}
