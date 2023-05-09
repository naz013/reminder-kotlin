package com.elementary.tasks.core.analytics

import android.os.Bundle
import com.elementary.tasks.core.data.ui.reminder.UiReminderType

sealed class AnalyticEvent(val event: Event) {
  abstract fun getParams(): Bundle
  fun getName() = event.value
}

data class VoiceFeatureUsedEvent(
  val language: String,
  val status: String,
  val action: String
) : AnalyticEvent(Event.VOICE_CONTROL_USED) {

  override fun getParams(): Bundle {
    return Bundle().apply {
      putString(Parameter.VOICE_ACTION, action)
      putString(Parameter.VOICE_LANGUAGE, language)
      putString(Parameter.VOICE_STATUS, status)
    }
  }
}

data class ReminderFeatureUsedEvent(
  val type: UiReminderType,
  val timeSeconds: Long
) : AnalyticEvent(Event.REMINDER_USED) {
  override fun getParams(): Bundle {
    return Bundle().apply {
      putString(Parameter.REMINDER_TYPE, getReminderType(type))
      putLong(Parameter.DURATION, timeSeconds)
    }
  }

  private fun getReminderType(type: UiReminderType): String {
    return when {
      type.isEmail() -> "email"
      type.isLink() -> "web_link"
      type.isApp() -> "app"
      type.isCall() -> "call"
      type.isSms() -> "sms"
      type.isGpsType() -> "gps"
      type.isMonthly() -> "monthly"
      type.isByWeekday() -> "weekday"
      type.isTimer() -> "timer"
      type.isYearly() -> "yearly"
      type.isByDate() -> "by_date"
      else -> "other"
    }
  }
}

data class FeatureUsedEvent(
  val feature: Feature
) : AnalyticEvent(Event.FEATURE_USED) {

  override fun getParams(): Bundle {
    return Bundle().apply {
      putString(Parameter.TYPE, feature.value)
    }
  }
}

data class ScreenUsedEvent(
  val screen: Screen
) : AnalyticEvent(Event.SCREEN_OPENED) {

  override fun getParams(): Bundle {
    return Bundle().apply {
      putString(Parameter.SCREEN, screen.value)
    }
  }
}

enum class Feature(val value: String) {
  REMINDER("reminder"),
  CREATE_REMINDER("create_reminder"),

  CREATE_NOTE("create_note"),

  CREATE_GOOGLE_TASK("create_google_task"),
  CREATE_GOOGLE_TASK_LIST("create_google_task_list"),

  BIRTHDAY("birthday"),
  CREATE_BIRTHDAY("create_birthday"),
  BIRTHDAY_PREVIEW("birthday_preview"),

  CREATE_GROUP("create_group"),

  GOOGLE_TASK("login_google_task"),
  GOOGLE_DRIVE("login_google_drive"),
  DROPBOX("login_dropbox")
}

enum class Screen(val value: String) {
  CLOUD_DRIVES("cloud_drives"),
  REMINDERS_LIST("reminders_list"),
  NOTES_LIST("notes_list"),
  NOTE_PREVIEW("note_preview"),
  GOOGLE_TASKS_LIST("google_tasks_list"),
  CALENDAR("calendar"),
  BIRTHDAYS("birthdays_list"),
  GROUPS("groups_list"),
  VOICE_CONTROL("voice_control")
}

enum class Event(val value: String) {
  FEATURE_USED("feature_used"),
  REMINDER_USED("reminder_used"),
  SCREEN_OPENED("screen_opened"),
  VOICE_CONTROL_USED("voice_control_used")
}

object Parameter {
  const val SCREEN = "screen"

  const val TYPE = "type"

  const val REMINDER_TYPE = "reminder_type"
  const val DURATION = "duration"

  const val VOICE_LANGUAGE = "language"
  const val VOICE_ACTION = "action"
  const val VOICE_STATUS = "status"
}
