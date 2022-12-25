package com.elementary.tasks.core.utils

import com.elementary.tasks.core.utils.params.Prefs

class FeatureManager(
  private val prefs: Prefs
) {

  fun isFeatureEnabled(feature: Feature): Boolean {
    return prefs.getBoolean(feature.value, true)
  }

  enum class Feature(val value: String) {
    DROPBOX("feature_dropbox"),
    GOOGLE_DRIVE("feature_google_drive"),
    GOOGLE_TASKS("feature_google_tasks")
  }
}