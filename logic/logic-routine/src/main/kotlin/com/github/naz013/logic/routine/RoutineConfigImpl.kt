package com.github.naz013.logic.routine

import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags

internal class RoutineConfigImpl(
  private val featureFlags: FeatureFlags
) : RoutineConfig {
  override val isEnabled: Boolean
    get() = featureFlags.isEnabled(FeatureFlag.ROUTINE_ENABLED)
}
