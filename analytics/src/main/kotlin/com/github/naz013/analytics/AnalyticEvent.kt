package com.github.naz013.analytics

import android.os.Bundle

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
  val type: AnalyticsReminderType,
  val timeSeconds: Long
) : AnalyticEvent(Event.REMINDER_USED) {
  override fun getParams(): Bundle {
    return Bundle().apply {
      putString(Parameter.REMINDER_TYPE, type.value)
      putLong(Parameter.DURATION, timeSeconds)
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

data class WidgetUsedEvent(
  val widget: Widget
) : AnalyticEvent(Event.WIDGET_USED) {

  override fun getParams(): Bundle {
    return Bundle().apply {
      putString(Parameter.TYPE, widget.value)
    }
  }
}

data class PresetUsed(
  val presetAction: PresetAction
) : AnalyticEvent(Event.PRESET_USED) {

  override fun getParams(): Bundle {
    return Bundle().apply {
      putString(Parameter.TYPE, presetAction.value)
    }
  }
}

enum class Feature(val value: String) {
  REMINDER("reminder"),
  CREATE_REMINDER("create_reminder"),

  CREATE_NOTE("create_note"),

  CREATE_GOOGLE_TASK("create_google_task"),
  CREATE_GOOGLE_TASK_LIST("create_google_task_list"),
  GOOGLE_TASK_PREVIEW("google_task_preview"),

  BIRTHDAY("birthday"),
  CREATE_BIRTHDAY("create_birthday"),
  BIRTHDAY_PREVIEW("birthday_preview"),

  CREATE_GROUP("create_group"),

  GOOGLE_TASK("login_google_task"),
  GOOGLE_DRIVE("login_google_drive"),
  DROPBOX("login_dropbox"),

  RECUR_EVENT_CREATED("recur_created")
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
  VOICE_CONTROL("voice_control"),
  TROUBLESHOOTING("troubleshooting"),
  WHATS_NEW("whats_new")
}

enum class Widget(val value: String) {
  EVENTS("events"),
  BIRTHDAYS("birthdays"),
  NOTES("notes"),
  CALENDAR("calendar"),
  COMBINED("combined"),
  GOOGLE_TASKS("google_tasks"),
  SINGLE_NOTE("single_note")
}

enum class Event(val value: String) {
  FEATURE_USED("feature_used"),
  REMINDER_USED("reminder_used"),
  SCREEN_OPENED("screen_opened"),
  VOICE_CONTROL_USED("voice_control_used"),
  PRESET_USED("preset_used"),
  WIDGET_USED("widget_used")
}

enum class PresetAction(val value: String) {
  CREATE("create"),
  USE("use"),
  DELETE("delete"),
  USE_BUILDER("use_builder")
}

enum class AnalyticsReminderType(val value: String) {
  Recur("recur"),
  Email("email"),
  WebLink("web_link"),
  App("app"),
  Call("call"),
  Sms("sms"),
  Gps("gps"),
  Monthly("monthly"),
  Weekday("weekday"),
  Timer("timer"),
  Yearly("yearly"),
  ByDate("by_date"),
  Other("other")
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
