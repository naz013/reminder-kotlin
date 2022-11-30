package com.elementary.tasks.core.utils

class FeatureManager(
  private val prefs: Prefs
) {

  fun isFeatureEnabled(feature: Feature): Boolean {
    return prefs.getBoolean(feature.value, false)
  }

  enum class Feature(val value: String) {
    DROPBOX("feature_dropbox"),
    GOOGLE_DRIVE("feature_google_drive"),
    GOOGLE_TASKS("feature_google_tasks")
  }
}