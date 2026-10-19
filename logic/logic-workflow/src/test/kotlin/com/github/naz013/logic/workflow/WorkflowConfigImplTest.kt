package com.github.naz013.logic.workflow

import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.platform.BuildInfo
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkflowConfigImplTest {
  private val featureFlags = mockk<FeatureFlags>()
  private val buildInfo = mockk<BuildInfo>()
  private val config = WorkflowConfigImpl(featureFlags, buildInfo)

  @Before
  fun before() {
    every { buildInfo.isPro } returns true
  }

  @Test
  fun `isEnabled follows the feature flag regardless of pro`() {
    every { featureFlags.isEnabled(FeatureFlag.WORKFLOW_ENABLED) } returns true
    every { buildInfo.isPro } returns false

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
    every { buildInfo.isPro } returns false

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
    every { buildInfo.isPro } returns false
    assertFalse(config.isRunnable)
    every { buildInfo.isPro } returns true
    assertTrue(config.isRunnable)
  }
}
