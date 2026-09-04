package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.koin.compose.koinInject

/**
 * The [SettingsItem.itemKey][SettingsItem] currently being highlighted after a settings-search
 * jump, or `null` when nothing should be highlighted - provided once per destination screen by
 * [SettingsHighlightScope], read by every [SettingsItem] to decide whether it is the target.
 */
val LocalSettingsHighlightKey = compositionLocalOf<String?> { null }

/**
 * Bridges a settings-search result's target item id to the destination screen once it's pushed
 * onto the backstack - that screen is a separate Nav3 entry (its own composition), so it can't
 * receive the id as a plain function argument the way a same-screen callback would. Mirrors
 * `HolidayCountryPickerResultHolder`'s approach to the same constraint.
 */
class SettingsHighlightController {
  var pendingHighlightKey: String? = null

  fun consumePendingHighlightKey(): String? {
    val key = pendingHighlightKey
    pendingHighlightKey = null
    return key
  }
}

@Composable
fun rememberSettingsHighlightController(): SettingsHighlightController = koinInject()

/**
 * Wrap a settings destination screen's content in this once, at its Nav3 entry, so any
 * [SettingsItem] inside it can be targeted by [SettingsHighlightController.pendingHighlightKey].
 * Consumes the pending key exactly once per screen visit, so navigating away and back doesn't
 * replay the highlight.
 */
@Composable
fun SettingsHighlightScope(content: @Composable () -> Unit) {
  val controller = rememberSettingsHighlightController()
  var activeHighlightKey by remember { mutableStateOf<String?>(null) }
  LaunchedEffect(Unit) { activeHighlightKey = controller.consumePendingHighlightKey() }

  CompositionLocalProvider(LocalSettingsHighlightKey provides activeHighlightKey, content = content)
}

/**
 * Stable [SettingsItem.itemKey] values for the individual settings the search index
 * (`SettingsSearchIndex` in `feature-settings`) can jump straight to and highlight. Shared as
 * plain constants - rather than each destination screen inventing its own string - since the row
 * living in that setting's screen (possibly in a different feature module) and the index entry
 * pointing at it need to agree on the exact same key.
 */
object SettingsSearchItemKeys {
  const val GENERAL_DARK_MODE = "general_dark_mode"
  const val GENERAL_LANGUAGE = "general_language"
  const val GENERAL_TIME_FORMAT = "general_time_format"
  const val GENERAL_METRIC_UNITS = "general_metric_units"
  const val GENERAL_ANALYTICS = "general_analytics"
  const val GENERAL_HAPTIC_FEEDBACK = "general_haptic_feedback"

  const val CALENDAR_PUBLIC_HOLIDAYS = "calendar_public_holidays"
  const val CALENDAR_FIRST_DAY = "calendar_first_day"
  const val CALENDAR_TODAY_COLOR = "calendar_today_color"
  const val CALENDAR_REMINDER_COLOR = "calendar_reminder_color"
  const val CALENDAR_BIRTHDAY_COLOR = "calendar_birthday_color"
  const val CALENDAR_EVENT_COLOR = "calendar_event_color"
  const val CALENDAR_CHOOSE_CALENDAR = "calendar_choose_calendar"
  const val CALENDAR_EXPORT_TOGGLE = "calendar_export_toggle"
  const val CALENDAR_SCAN_TOGGLE = "calendar_scan_toggle"

  const val REMINDERS_DO_NOT_DISTURB = "reminders_do_not_disturb"
  const val REMINDERS_SNOOZE_TIME = "reminders_snooze_time"
  const val REMINDERS_LED_COLOR = "reminders_led_color"
  const val REMINDERS_VIBRATION_PATTERN = "reminders_vibration_pattern"
  const val REMINDERS_INSIGHTS = "reminders_insights"
  const val REMINDERS_PRIORITY = "reminders_priority"
  const val REMINDERS_COMPLETED = "reminders_completed"
  const val REMINDERS_WEAR = "reminders_wear"
  const val REMINDERS_REPEAT_INTERVAL = "reminders_repeat_interval"
  const val REMINDERS_MAX_REPEAT_COUNT = "reminders_max_repeat_count"
  const val REMINDERS_ESCALATE = "reminders_escalate"
  const val REMINDERS_PERMANENT_NOTIFICATION = "reminders_permanent_notification"
  const val REMINDERS_DEFAULT_CATEGORY = "reminders_default_category"
  const val REMINDERS_LOCK_SCREEN_VISIBILITY = "reminders_lock_screen_visibility"
  const val REMINDERS_SWIPE_TO_DISMISS = "reminders_swipe_to_dismiss"
  const val REMINDERS_IN_APP_BANNER = "reminders_in_app_banner"

  const val BIRTHDAY_REMINDER_TOGGLE = "birthday_reminder_toggle"
  const val BIRTHDAY_DAYS_BEFORE = "birthday_days_before"
  const val BIRTHDAY_PRIORITY = "birthday_priority"
  const val BIRTHDAY_REMIND_TIME = "birthday_remind_time"
  const val BIRTHDAY_WIDGET = "birthday_widget"
  const val BIRTHDAY_USE_CONTACTS = "birthday_use_contacts"
  const val BIRTHDAY_AUTO_SCAN = "birthday_auto_scan"

  const val NOTES_LAST_COLOR = "notes_last_color"
  const val NOTES_TEXT_SIZE = "notes_text_size"
  const val NOTES_FONT_STYLE = "notes_font_style"
  const val NOTES_TEXT_COLOR = "notes_text_color"
  const val NOTES_OPACITY = "notes_opacity"

  const val SECURITY_PIN_PROTECTION = "security_pin_protection"
  const val SECURITY_FINGERPRINT = "security_fingerprint"
  const val SECURITY_CHANGE_PIN = "security_change_pin"
  const val SECURITY_SHUFFLE_DIGITS = "security_shuffle_digits"
  const val SECURITY_PHONE_SMS = "security_phone_sms"

  const val LOCATION_NOTIFICATION_TOGGLE = "location_notification_toggle"
  const val LOCATION_RADIUS = "location_radius"
  const val LOCATION_MAP_TYPE = "location_map_type"
  const val LOCATION_MARKER_STYLE = "location_marker_style"
  const val LOCATION_TRACKING = "location_tracking"

  const val BACKUP_AUTO_INTERVAL = "backup_auto_interval"
  const val BACKUP_NETWORK_TYPE = "backup_network_type"
  const val BACKUP_ERASE_CLOUD = "backup_erase_cloud"
}
