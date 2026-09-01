package com.github.naz013.analytics

import android.os.Bundle

sealed class AnalyticEvent(val event: Event) {
  abstract fun getParams(): Bundle
  fun getName() = event.value
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

/** Send once per user, the first time they complete a feature's core action - not on every use
 *  like [FeatureUsedEvent]. Lets adoption be tracked as a distinct GA4 event/metric from overall
 *  usage volume, so dashboards can show new adopters over time instead of only total event counts. */
data class FeatureAdoptedEvent(
  val feature: Feature
) : AnalyticEvent(Event.FEATURE_ADOPTED) {

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

/** Send on every real tap on a widget already placed on the home screen (item/button clicks),
 *  as opposed to [WidgetUsedEvent] which fires when the widget is added/reconfigured. Lets GA
 *  distinguish "most added" from "most actively used" widget types. */
data class WidgetInteractedEvent(
  val widget: Widget
) : AnalyticEvent(Event.WIDGET_INTERACTED) {

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

/** Fired when a free user taps a locked Pro-only row (Insights, Local Backup, Gemini
 *  AppFunctions) instead of the feature itself, so we know which gate is driving Pro-screen
 *  visits. */
data class FeatureGateTappedEvent(
  val feature: Feature
) : AnalyticEvent(Event.FEATURE_GATE_TAPPED) {
  override fun getParams(): Bundle {
    return Bundle().apply {
      putString(Parameter.TYPE, feature.value)
    }
  }
}

data object ProScreenViewedEvent : AnalyticEvent(Event.PRO_SCREEN_VIEWED) {
  override fun getParams(): Bundle = Bundle()
}

data object ProBuyClickedEvent : AnalyticEvent(Event.PRO_BUY_CLICKED) {
  override fun getParams(): Bundle = Bundle()
}

/** Fired once, from the Pro app's first launch, when an install-referrer value set by the free
 *  app's "Buy PRO" deep link (see GooglePlayMarketLauncher) is read back - the only way to
 *  attribute a Pro install to the free app, since Pro is a separate paid listing rather than an
 *  in-app purchase. */
data class ProInstallAttributedEvent(
  val source: String
) : AnalyticEvent(Event.PRO_INSTALL_ATTRIBUTED) {
  override fun getParams(): Bundle {
    return Bundle().apply {
      putString(Parameter.SOURCE, source)
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

  APP_FUNCTION_CREATE_REMINDER("app_function_create_reminder"),
  APP_FUNCTION_LIST_REMINDERS("app_function_list_reminders"),
  APP_FUNCTION_COMPLETE_REMINDER("app_function_complete_reminder"),
  APP_FUNCTION_DELETE_REMINDER("app_function_delete_reminder"),
  APP_FUNCTION_UPDATE_REMINDER("app_function_update_reminder"),
  APP_FUNCTION_SEARCH_REMINDERS("app_function_search_reminders"),

  APP_FUNCTION_CREATE_NOTE("app_function_create_note"),
  APP_FUNCTION_SEARCH_NOTES("app_function_search_notes"),
  APP_FUNCTION_UPDATE_NOTE("app_function_update_note"),
  APP_FUNCTION_DELETE_NOTE("app_function_delete_note"),

  APP_FUNCTION_CREATE_BIRTHDAY("app_function_create_birthday"),
  APP_FUNCTION_LIST_BIRTHDAYS("app_function_list_birthdays"),
  APP_FUNCTION_UPDATE_BIRTHDAY("app_function_update_birthday"),
  APP_FUNCTION_DELETE_BIRTHDAY("app_function_delete_birthday"),
  APP_FUNCTION_SEARCH_BIRTHDAYS("app_function_search_birthdays"),

  APP_FUNCTION_CREATE_GOOGLE_TASK("app_function_create_google_task"),
  APP_FUNCTION_LIST_GOOGLE_TASKS("app_function_list_google_tasks"),
  APP_FUNCTION_COMPLETE_GOOGLE_TASK("app_function_complete_google_task"),
  APP_FUNCTION_UPDATE_GOOGLE_TASK("app_function_update_google_task"),
  APP_FUNCTION_DELETE_GOOGLE_TASK("app_function_delete_google_task"),
  APP_FUNCTION_SEARCH_GOOGLE_TASKS("app_function_search_google_tasks"),

  INSIGHTS("insights"),
  LOCAL_BACKUP("local_backup"),
  GEMINI_FUNCTIONS("gemini_functions"),
  PUBLIC_HOLIDAYS("public_holidays"),
  BUY_ME_A_COFFEE("buy_me_a_coffee"),
  AI_DIGEST("ai_digest")
}

enum class Screen(val value: String) {
  CLOUD_DRIVES("cloud_drives"),
  REMINDERS_LIST("reminders_list"),
  TODO_REMINDERS_LIST("todo_reminders_list"),
  NOTES_LIST("notes_list"),
  NOTE_PREVIEW("note_preview"),
  GOOGLE_TASKS_LIST("google_tasks_list"),
  CALENDAR("calendar"),
  BIRTHDAYS("birthdays_list"),
  GROUPS("groups_list"),
  TROUBLESHOOTING("troubleshooting"),
  WHATS_NEW("whats_new"),

  SETTINGS("settings"),
  GENERAL_SETTINGS("general_settings"),
  BIRTHDAY_SETTINGS("birthday_settings"),
  CALENDAR_SETTINGS("calendar_settings"),
  REMINDERS_SETTINGS("reminders_settings"),
  SECURITY_SETTINGS("security_settings"),
  NOTE_SETTINGS("note_settings"),
  LOCATION_SETTINGS("location_settings"),
  OTHER_SETTINGS("other_settings"),
  AI_DIGEST_SETTINGS("ai_digest_settings")
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
  FEATURE_ADOPTED("feature_adopted"),
  REMINDER_USED("reminder_used"),
  SCREEN_OPENED("screen_opened"),
  PRESET_USED("preset_used"),
  WIDGET_USED("widget_used"),
  WIDGET_INTERACTED("widget_interacted"),
  PRO_SCREEN_VIEWED("pro_screen_viewed"),
  PRO_BUY_CLICKED("pro_buy_clicked"),
  FEATURE_GATE_TAPPED("feature_gate_tapped"),
  PRO_INSTALL_ATTRIBUTED("pro_install_attributed")
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
  const val SOURCE = "source"
}
