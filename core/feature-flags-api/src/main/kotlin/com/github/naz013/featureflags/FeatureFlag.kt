package com.github.naz013.featureflags

enum class FeatureFlag(
  val key: String,
  val defaultValue: Boolean = true,
) {
  DROPBOX("feature_dropbox"),
  GOOGLE_DRIVE("feature_google_drive"),
  GOOGLE_TASKS("feature_google_tasks"),
  ALLOW_LOGS("allow_log_send", false),
  REMINDER_BUILDER_V1("feature_builder_v1"),
  REMINDER_BUILDER_V2("feature_builder_v2"),
  GEOCODING("feature_geocoding"),
  LOGS_IN_REVIEWS("feature_logs_in_reviews", false),
  PUBLIC_HOLIDAYS("feature_public_holidays", false),
  BUY_ME_A_COFFEE("feature_buy_me_a_coffee", false),
  WORKFLOW_ENABLED("feature_workflow_enabled", false),
  ROUTINE_ENABLED("feature_routine_enabled", false),
  AI_DIGEST("feature_ai_digest", false),
}
