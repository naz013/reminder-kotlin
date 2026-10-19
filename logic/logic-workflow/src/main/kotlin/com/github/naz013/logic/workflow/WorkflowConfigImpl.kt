package com.github.naz013.logic.workflow

import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.platform.BuildInfo

internal class WorkflowConfigImpl(
  private val featureFlags: FeatureFlags,
  private val buildInfo: BuildInfo,
) : WorkflowConfig {
  override val isEnabled: Boolean
    get() = featureFlags.isEnabled(FeatureFlag.WORKFLOW_ENABLED)

  override val isRunnable: Boolean
    get() = isEnabled && buildInfo.isPro
}
