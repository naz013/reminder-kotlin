package com.github.naz013.feature.settings.search

import androidx.navigation3.runtime.NavKey
import com.github.naz013.feature.settings.SettingsNavKey
import com.github.naz013.feature.settings.export.ExportNavKey
import com.github.naz013.feature.settings.location.LocationNavKey
import com.github.naz013.feature.settings.other.OtherNavKey
import com.github.naz013.feature.settings.security.SecurityNavKey
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.foundation.component.SettingsSearchItemKeys

/**
 * Static, hand-maintained search index for the Settings Hub - every screen reachable from the
 * Hub, plus a curated set of individual settings ([SettingsSearchEntry.highlightItemId] set) that
 * are easy to miss inside a longer screen. Update this list whenever a settings screen or a
 * highlighted setting is added, renamed, or removed.
 */
internal object SettingsSearchIndex {

  private val remindersPath: List<NavKey> = listOf(SettingsNavKey.Reminders())
  private val calendarPath: List<NavKey> = listOf(SettingsNavKey.Calendar())
  private val securityPath: List<NavKey> = listOf(SecurityNavKey.Security)
  private val notePath: List<NavKey> = listOf(SettingsNavKey.Note())
  private val birthdayPath: List<NavKey> = listOf(SettingsNavKey.Birthday())
  private val locationPath: List<NavKey> = listOf(SettingsNavKey.Reminders(), LocationNavKey.Location)
  private val cloudBackupPath: List<NavKey> = listOf(SettingsNavKey.Backup, ExportNavKey.CloudBackup)

  val entries: List<SettingsSearchEntry> = buildList {
    // General
    add(SettingsSearchEntry(titleRes = R.string.general, path = listOf(SettingsNavKey.General)))
    add(
      SettingsSearchEntry(
        titleRes = R.string.dark_mode,
        path = listOf(SettingsNavKey.General),
        keywordRes = listOf(R.string.theme, R.string.settings_search_keyword_appearance),
        highlightItemId = SettingsSearchItemKeys.GENERAL_DARK_MODE,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.application_language,
        path = listOf(SettingsNavKey.General),
        keywordRes = listOf(R.string.settings_search_keyword_language),
        highlightItemId = SettingsSearchItemKeys.GENERAL_LANGUAGE,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string._24_hour_format,
        path = listOf(SettingsNavKey.General),
        keywordRes = listOf(R.string.settings_search_keyword_time_format),
        highlightItemId = SettingsSearchItemKeys.GENERAL_TIME_FORMAT,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.header_items,
        path = listOf(SettingsNavKey.HeaderItems),
        keywordRes = listOf(R.string.settings_search_keyword_home_screen),
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.metric_units,
        path = listOf(SettingsNavKey.General),
        keywordRes = listOf(R.string.settings_search_keyword_measurement_units),
        highlightItemId = SettingsSearchItemKeys.GENERAL_METRIC_UNITS,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.analytics,
        path = listOf(SettingsNavKey.General),
        keywordRes = listOf(R.string.settings_search_keyword_telemetry),
        highlightItemId = SettingsSearchItemKeys.GENERAL_ANALYTICS,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.haptic_feedback,
        path = listOf(SettingsNavKey.General),
        keywordRes = listOf(R.string.settings_search_keyword_touch_feedback),
        highlightItemId = SettingsSearchItemKeys.GENERAL_HAPTIC_FEEDBACK,
      ),
    )

    // Backup
    add(SettingsSearchEntry(titleRes = R.string.backup, path = listOf(SettingsNavKey.Backup)))
    add(
      SettingsSearchEntry(
        titleRes = R.string.cloud_backup,
        path = cloudBackupPath,
        keywordRes = listOf(R.string.settings_search_keyword_cloud_backup),
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.automatically_backup,
        path = cloudBackupPath,
        keywordRes = listOf(R.string.settings_search_keyword_auto_backup_interval),
        highlightItemId = SettingsSearchItemKeys.BACKUP_AUTO_INTERVAL,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.which_network_to_use_for_sync,
        path = cloudBackupPath,
        keywordRes = listOf(R.string.settings_search_keyword_wifi_only_sync),
        highlightItemId = SettingsSearchItemKeys.BACKUP_NETWORK_TYPE,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.erase_cloud_data,
        path = cloudBackupPath,
        keywordRes = listOf(R.string.settings_search_keyword_delete_cloud_backup),
        highlightItemId = SettingsSearchItemKeys.BACKUP_ERASE_CLOUD,
      ),
    )

    // Calendar
    add(SettingsSearchEntry(titleRes = R.string.calendar, path = calendarPath))
    add(
      SettingsSearchEntry(
        titleRes = R.string.first_day_of_the_week,
        path = calendarPath,
        keywordRes = listOf(R.string.settings_search_keyword_week_start),
        highlightItemId = SettingsSearchItemKeys.CALENDAR_FIRST_DAY,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.today_color,
        path = calendarPath,
        highlightItemId = SettingsSearchItemKeys.CALENDAR_TODAY_COLOR,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.reminders_color,
        path = calendarPath,
        highlightItemId = SettingsSearchItemKeys.CALENDAR_REMINDER_COLOR,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.birthdays_color,
        path = calendarPath,
        highlightItemId = SettingsSearchItemKeys.CALENDAR_BIRTHDAY_COLOR,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.google_calendar_events_color,
        path = calendarPath,
        highlightItemId = SettingsSearchItemKeys.CALENDAR_EVENT_COLOR,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.choose_calendar,
        path = calendarPath,
        keywordRes = listOf(R.string.settings_search_keyword_select_google_calendar),
        highlightItemId = SettingsSearchItemKeys.CALENDAR_CHOOSE_CALENDAR,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.add_reminders_to_google_calendar,
        path = calendarPath,
        keywordRes = listOf(R.string.settings_search_keyword_export_reminders),
        highlightItemId = SettingsSearchItemKeys.CALENDAR_EXPORT_TOGGLE,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.scan_google_calendar_for_the_new_events,
        path = calendarPath,
        keywordRes = listOf(R.string.settings_search_keyword_import_calendar_events),
        highlightItemId = SettingsSearchItemKeys.CALENDAR_SCAN_TOGGLE,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.show_public_holidays,
        path = calendarPath,
        keywordRes = listOf(R.string.settings_search_keyword_holiday_calendar),
        highlightItemId = SettingsSearchItemKeys.CALENDAR_PUBLIC_HOLIDAYS,
        isProOnly = true,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.public_holidays_country,
        path = listOf(SettingsNavKey.Calendar(), SettingsNavKey.SelectHolidayCountry),
        keywordRes = listOf(R.string.settings_search_keyword_holiday_calendar),
        isProOnly = true,
      ),
    )

    // Reminders
    add(SettingsSearchEntry(titleRes = R.string.reminders_, path = remindersPath))
    add(
      SettingsSearchEntry(
        titleRes = R.string.insights,
        path = remindersPath,
        keywordRes = listOf(R.string.settings_search_keyword_streaks),
        highlightItemId = SettingsSearchItemKeys.REMINDERS_INSIGHTS,
        isProOnly = true,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.reminder_default_priority,
        path = remindersPath,
        highlightItemId = SettingsSearchItemKeys.REMINDERS_PRIORITY,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.completed_reminders,
        path = remindersPath,
        keywordRes = listOf(R.string.settings_search_keyword_archive_completed),
        highlightItemId = SettingsSearchItemKeys.REMINDERS_COMPLETED,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.android_wear_notification,
        path = remindersPath,
        keywordRes = listOf(R.string.settings_search_keyword_smartwatch),
        highlightItemId = SettingsSearchItemKeys.REMINDERS_WEAR,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.do_not_disturb,
        path = remindersPath,
        keywordRes = listOf(R.string.settings_search_keyword_quiet_hours),
        highlightItemId = SettingsSearchItemKeys.REMINDERS_DO_NOT_DISTURB,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.default_reminder_snooze_time,
        path = remindersPath,
        keywordRes = listOf(R.string.settings_search_keyword_postpone),
        highlightItemId = SettingsSearchItemKeys.REMINDERS_SNOOZE_TIME,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.reminder_notification_repeat_interval,
        path = remindersPath,
        highlightItemId = SettingsSearchItemKeys.REMINDERS_REPEAT_INTERVAL,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.reminder_notification_max_repeat_count,
        path = remindersPath,
        highlightItemId = SettingsSearchItemKeys.REMINDERS_MAX_REPEAT_COUNT,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.reminder_notification_escalate_after_repeats,
        path = remindersPath,
        keywordRes = listOf(R.string.settings_search_keyword_escalation),
        highlightItemId = SettingsSearchItemKeys.REMINDERS_ESCALATE,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.led_indication_color,
        path = remindersPath,
        keywordRes = listOf(R.string.settings_search_keyword_notification_light),
        highlightItemId = SettingsSearchItemKeys.REMINDERS_LED_COLOR,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.allow_swipe_to_dismiss,
        path = remindersPath,
        keywordRes = listOf(R.string.settings_search_keyword_swipe_away),
        highlightItemId = SettingsSearchItemKeys.REMINDERS_SWIPE_TO_DISMISS,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.in_app_notification_banner,
        path = remindersPath,
        keywordRes = listOf(R.string.settings_search_keyword_banner),
        highlightItemId = SettingsSearchItemKeys.REMINDERS_IN_APP_BANNER,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.permanent_notification,
        path = remindersPath,
        keywordRes = listOf(R.string.settings_search_keyword_sticky_notification),
        highlightItemId = SettingsSearchItemKeys.REMINDERS_PERMANENT_NOTIFICATION,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.notification_category,
        path = remindersPath,
        highlightItemId = SettingsSearchItemKeys.REMINDERS_DEFAULT_CATEGORY,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.vibration_pattern,
        path = remindersPath,
        keywordRes = listOf(R.string.settings_search_keyword_vibrate),
        highlightItemId = SettingsSearchItemKeys.REMINDERS_VIBRATION_PATTERN,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.lock_screen_visibility,
        path = remindersPath,
        highlightItemId = SettingsSearchItemKeys.REMINDERS_LOCK_SCREEN_VISIBILITY,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.recur_presets,
        path = listOf(SettingsNavKey.Reminders(), SettingsNavKey.ManagePresets),
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.notification_customization,
        path = listOf(SettingsNavKey.Reminders(), SettingsNavKey.NotificationCustomizationHelp),
      ),
    )

    // Location (nested under Reminders)
    add(
      SettingsSearchEntry(
        titleRes = R.string.location,
        path = locationPath,
        keywordRes = listOf(R.string.settings_search_keyword_geofence),
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.distance_notification,
        path = locationPath,
        keywordRes = listOf(R.string.settings_search_keyword_geofence_alert),
        highlightItemId = SettingsSearchItemKeys.LOCATION_NOTIFICATION_TOGGLE,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.radius,
        path = locationPath,
        keywordRes = listOf(R.string.settings_search_keyword_geofence_radius),
        highlightItemId = SettingsSearchItemKeys.LOCATION_RADIUS,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.map_type,
        path = locationPath,
        keywordRes = listOf(R.string.settings_search_keyword_map_terrain),
        highlightItemId = SettingsSearchItemKeys.LOCATION_MAP_TYPE,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.style_of_marker,
        path = locationPath,
        highlightItemId = SettingsSearchItemKeys.LOCATION_MARKER_STYLE,
        isProOnly = true,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.tracking_settings,
        path = locationPath,
        highlightItemId = SettingsSearchItemKeys.LOCATION_TRACKING,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.map_style,
        path = listOf(SettingsNavKey.Reminders(), LocationNavKey.Location, LocationNavKey.MapStyle),
      ),
    )

    // Birthdays
    add(SettingsSearchEntry(titleRes = R.string.birthdays, path = birthdayPath))
    add(
      SettingsSearchEntry(
        titleRes = R.string.birthday_reminder,
        path = birthdayPath,
        highlightItemId = SettingsSearchItemKeys.BIRTHDAY_REMINDER_TOGGLE,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.days_to_birthday,
        path = birthdayPath,
        highlightItemId = SettingsSearchItemKeys.BIRTHDAY_DAYS_BEFORE,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.birthday_notification_priority,
        path = birthdayPath,
        highlightItemId = SettingsSearchItemKeys.BIRTHDAY_PRIORITY,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.remind_at,
        path = birthdayPath,
        highlightItemId = SettingsSearchItemKeys.BIRTHDAY_REMIND_TIME,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.birthdays_in_home_screen_widget,
        path = birthdayPath,
        keywordRes = listOf(R.string.settings_search_keyword_home_screen),
        highlightItemId = SettingsSearchItemKeys.BIRTHDAY_WIDGET,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.birthdays_from_contacts,
        path = birthdayPath,
        keywordRes = listOf(R.string.settings_search_keyword_import_contacts),
        highlightItemId = SettingsSearchItemKeys.BIRTHDAY_USE_CONTACTS,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.scan_contacts_automatically,
        path = birthdayPath,
        highlightItemId = SettingsSearchItemKeys.BIRTHDAY_AUTO_SCAN,
      ),
    )

    // Notes
    add(SettingsSearchEntry(titleRes = R.string.notes, path = notePath))
    add(
      SettingsSearchEntry(
        titleRes = R.string.last_color,
        path = notePath,
        keywordRes = listOf(R.string.settings_search_keyword_remember_color),
        highlightItemId = SettingsSearchItemKeys.NOTES_LAST_COLOR,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.color_saturation,
        path = notePath,
        keywordRes = listOf(R.string.settings_search_keyword_transparency),
        highlightItemId = SettingsSearchItemKeys.NOTES_OPACITY,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.text_size,
        path = notePath,
        highlightItemId = SettingsSearchItemKeys.NOTES_TEXT_SIZE,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.font_style,
        path = notePath,
        highlightItemId = SettingsSearchItemKeys.NOTES_FONT_STYLE,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.text_color,
        path = notePath,
        highlightItemId = SettingsSearchItemKeys.NOTES_TEXT_COLOR,
      ),
    )

    // Security
    add(SettingsSearchEntry(titleRes = R.string.security, path = securityPath))
    add(
      SettingsSearchEntry(
        titleRes = R.string.pin_protection,
        path = securityPath,
        keywordRes = listOf(R.string.settings_search_keyword_app_lock),
        highlightItemId = SettingsSearchItemKeys.SECURITY_PIN_PROTECTION,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.change_pin,
        path = securityPath,
        highlightItemId = SettingsSearchItemKeys.SECURITY_CHANGE_PIN,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.fingerprint,
        path = securityPath,
        keywordRes = listOf(R.string.settings_search_keyword_biometric),
        highlightItemId = SettingsSearchItemKeys.SECURITY_FINGERPRINT,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.shuffle_digits,
        path = securityPath,
        keywordRes = listOf(R.string.settings_search_keyword_randomize_pin),
        highlightItemId = SettingsSearchItemKeys.SECURITY_SHUFFLE_DIGITS,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.phone_calls_and_sms,
        path = securityPath,
        keywordRes = listOf(R.string.settings_search_keyword_telephony),
        highlightItemId = SettingsSearchItemKeys.SECURITY_PHONE_SMS,
      ),
    )

    // Other
    add(SettingsSearchEntry(titleRes = R.string.other, path = listOf(OtherNavKey.Other)))
    add(
      SettingsSearchEntry(
        titleRes = R.string.ai_digest,
        path = listOf(OtherNavKey.Other, SettingsNavKey.AiDigest),
        isProOnly = true,
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.troubleshooting,
        path = listOf(OtherNavKey.Other, SettingsNavKey.Troubleshooting),
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.permissions,
        path = listOf(OtherNavKey.Other, OtherNavKey.Permissions),
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.open_source_licenses,
        path = listOf(OtherNavKey.Other, OtherNavKey.Oss),
        keywordRes = listOf(R.string.settings_search_keyword_license),
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.privacy_policy,
        path = listOf(OtherNavKey.Other, OtherNavKey.PrivacyPolicy),
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.terms_and_conditions,
        path = listOf(OtherNavKey.Other, OtherNavKey.Terms),
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.whats_new,
        path = listOf(OtherNavKey.Other, OtherNavKey.WhatsNew),
      ),
    )
    add(
      SettingsSearchEntry(
        titleRes = R.string.gemini_functions,
        path = listOf(OtherNavKey.Other, OtherNavKey.GeminiFunctions),
        isProOnly = true,
      ),
    )

    // Pro version
    add(SettingsSearchEntry(titleRes = R.string.pro_version, path = listOf(SettingsNavKey.ProVersion)))
  }
}
