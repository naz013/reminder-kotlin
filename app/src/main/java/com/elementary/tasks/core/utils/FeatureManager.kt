package com.elementary.tasks.core.utils

import com.elementary.tasks.core.utils.params.Prefs

class FeatureManager(
  private val prefs: Prefs
) {

  fun isFeatureEnabled(feature: Feature): Boolean {
    return prefs.getBoolean(feature.value, feature.defaultValue)
  }

  enum class Feature(
    val value: String,
    val defaultValue: Boolean = true
  ) {
    DROPBOX("feature_dropbox"),
    GOOGLE_DRIVE("feature_google_drive"),
    GOOGLE_TASKS("feature_google_tasks"),
    ALLOW_LOGS("allow_log_send", false),
    REMINDER_BUILDER_V1("feature_builder_v1"),
    REMINDER_BUILDER_V2("feature_builder_v2"),
    GEOCODING("feature_geocoding")
  }
}
