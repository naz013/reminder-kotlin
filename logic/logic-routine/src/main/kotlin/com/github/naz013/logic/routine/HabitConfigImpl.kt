package com.github.naz013.logic.routine

import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.platform.BuildInfo

internal class HabitConfigImpl(
  private val featureFlags: FeatureFlags,
  private val buildInfo: BuildInfo
) : HabitConfig {
  override val isEnabled: Boolean
    get() = featureFlags.isEnabled(FeatureFlag.HABIT_ENABLED) && buildInfo.isPro
}
