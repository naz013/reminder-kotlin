package com.elementary.tasks.core.utils

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags

class FeatureManager(
  private val prefs: Prefs,
) : FeatureFlags {
  override fun isEnabled(feature: FeatureFlag): Boolean = prefs.getBoolean(feature.key, feature.defaultValue)
}
