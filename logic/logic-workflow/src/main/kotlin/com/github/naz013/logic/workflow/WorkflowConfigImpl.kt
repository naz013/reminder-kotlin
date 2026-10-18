package com.github.naz013.logic.workflow

import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags

/** Read at call time so workflows idle when the PRO trial ends and resume when PRO returns. */
fun interface WorkflowProAccess {
  fun isPro(): Boolean
}

internal class WorkflowConfigImpl(
  private val featureFlags: FeatureFlags,
  private val proAccess: WorkflowProAccess,
) : WorkflowConfig {
  override val isEnabled: Boolean
    get() = featureFlags.isEnabled(FeatureFlag.WORKFLOW_ENABLED)

  override val isRunnable: Boolean
    get() = isEnabled && proAccess.isPro()
}
