package com.elementary.tasks.core.analytics

import android.os.Bundle

sealed class AnalyticEvent(val name: String) {

  open fun getParams(bundle: Bundle): Bundle {
    return bundle
  }
}

data class ReminderFeatureUsedEvent(
  override val feature: Feature,
  val reminderType: String
) : FeatureUsedEvent(feature) {
  override fun getParams(bundle: Bundle): Bundle {
    return bundle
  }
}

open class FeatureUsedEvent(
  open val feature: Feature
) : AnalyticEvent("feature_used") {

  override fun getParams(bundle: Bundle): Bundle {
    bundle.putString("type", feature.value)
    return bundle
  }
}

data class ScreenUsedEvent(
  val screen: Screen
) : AnalyticEvent("screen_opened") {

  override fun getParams(bundle: Bundle): Bundle {
    bundle.putString("screen", screen.value)
    return bundle
  }
}

enum class Feature(val value: String) {
  REMINDER("reminder"),
  NOTE("note"),
  GOOGLE_TASK("google_task"),
  BIRTHDAY("birthday"),
  SMS_TEMPLATE("sms_template"),
  MISSED_CALL("missed_call_reminder"),
  AFTER_CALL("after_call_reminder")
}

enum class Screen(val value: String) {
  REMINDERS_LIST("reminders"),
  NOTES_LIST("notes"),
  GOOGLE_TASKS_LIST("google_tasks"),
  CALENDAR("calendar"),
  BIRTHDAYS("birthdays"),
  GROUPS("groups")
}
