package com.github.naz013.logic.workflow

import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags

internal class WorkflowConfigImpl(
  private val featureFlags: FeatureFlags
) : WorkflowConfig {
  override val isEnabled: Boolean
    get() = featureFlags.isEnabled(FeatureFlag.WORKFLOW_ENABLED)
}
