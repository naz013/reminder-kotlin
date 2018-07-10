package com.elementary.tasks.core.utils

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

internal open class PrefsConstants {
    companion object {

        protected val PREFS_NAME = "prefs_six"
        protected val EVENTS_CALENDAR = "events_cal"
        protected val AUTO_CHECK_FOR_EVENTS = "auto_events"
        protected val AUTO_CHECK_FOR_EVENTS_INTERVAL = "auto_events_interval"
        protected val APP_THEME = "theme_"
        protected val APP_THEME_COLOR = "theme_color"
        protected val DRIVE_USER = "ggl_user"
        protected val SCREEN = "screen"
        protected val MAP_TYPE = "new_map_type"
        protected val MAP_STYLE = "new_map_style"
        protected val LOCATION_RADIUS = "radius"
        protected val TRACKING_NOTIFICATION = "tracking_notification"
        val VIBRATION_STATUS = "vibration_status"
        val SILENT_SOUND = "sound_status"
        val WAKE_STATUS = "wake_status"
        val INFINITE_SOUND = "infinite_sound"
        val SILENT_SMS = "silent_sms"
        val CONTACT_BIRTHDAYS = "use_contacts"
        val BIRTHDAY_REMINDER = "birthdays_reminder"
        val START_DAY = "start_day"
        val DAYS_TO_BIRTHDAY = "days_to"
        val DELAY_TIME = "delay_time"
        val EVENT_DURATION = "event_duration"
        val EXPORT_TO_CALENDAR = "export_to_calendar"
        val CALENDAR_ID = "cal_id"
        val AUTO_CHECK_BIRTHDAYS = "birthdays_auto_scan"
        val LED_STATUS = "led_status"
        val LED_COLOR = "led_color"
        val MARKER_STYLE = "marker_style"
        val INFINITE_VIBRATION = "infinite_vibration"
        val AUTO_BACKUP = "auto_backup"
        val AUTO_BACKUP_INTERVAL = "auto_backup_interval"
        val SMART_FOLD = "smart_fold"
        val NOTIFICATION_REPEAT = "notification_repeat"
        val NOTIFICATION_REPEAT_INTERVAL = "notification_repeat_interval"
        val WEAR_NOTIFICATION = "wear_notification"
        val WIDGET_BIRTHDAYS = "widget_birthdays"
        val NOTIFICATION_REMOVE = "notification_remove"
        val BIRTH_COLOR = "birth_color"
        val TODAY_COLOR = "today_color"
        val STATUS_BAR_ICON = "status_icon"
        val STATUS_BAR_NOTIFICATION = "status_notification"
        val TRACK_TIME = "tracking_time"
        val TRACK_DISTANCE = "tracking_distance"
        val NOTE_TEXT_SIZE = "text_size"
        val QUICK_NOTE_REMINDER = "quick_note_reminder"
        val QUICK_NOTE_REMINDER_TIME = "quick_note_reminder_time"
        val VOICE_LOCALE = "voice_locale"
        val TIME_MORNING = "time_morning"
        val TIME_DAY = "time_day"
        val TIME_EVENING = "time_evening"
        val TIME_NIGHT = "time_night"
        val EXPORT_TO_STOCK = "export_to_stock"
        val APPLICATION_AUTO_LAUNCH = "application_auto_launch"
        val REMINDERS_IN_CALENDAR = "reminders_in_calendar"
        val REMINDER_COLOR = "reminder_color"
        val IS_24_TIME_FORMAT = "24_hour_format"
        val UNLOCK_DEVICE = "unlock_device"
        val CALENDAR_FEATURE_TASKS = "feature_tasks"
        val MISSED_CALL_REMINDER = "missed_call_reminder"
        val MISSED_CALL_TIME = "missed_call_time"
        val VOLUME = "reminder_volume"
        val QUICK_SMS = "quick_sms"
        val FOLLOW_REMINDER = "follow_reminder"
        val TTS = "tts_enabled"
        val TTS_LOCALE = "tts_locale"
        val ITEM_PREVIEW = "item_preview"
        val UI_CHANGED = "ui_changed"
        val CALENDAR_IMAGE = "calendar_image"
        val EXPORT_SETTINGS = "export_settings"
        val CUSTOM_SOUND = "custom_sound"
        val BIRTHDAY_PERMANENT = "birthday_permanent"
        val REMINDER_IMAGE = "reminder_image"
        val REMINDER_IMAGE_BLUR = "reminder_image_blur"
        val SYSTEM_VOLUME = "system_volume"
        val SOUND_STREAM = "sound_stream"
        val INCREASING_VOLUME = "increasing_volume"
        val MAIN_IMAGE_PATH = "main_image_path"
        val MAIN_IMAGE_ID = "main_image_id"
        val CALENDAR_IMAGES = "calendar_images"
        val AUTO_CALL = "auto_calls"
        val GCM_ENABLED = "gcm_enabled"
        val BETA_KEY = "beta_key"
        val RATE_COUNT = "rate_count"
        val USER_LOGGED = "user_logged"
        val LIVE_CONVERSATION = "live_conversation"
        val MIGRATION_COMPLETE = "migration_complete"
        val AUTO_SAVE = "auto_save"
        val REMEMBER_NOTE_COLOR = "remember_note_color"
        val LAST_NOTE_COLOR = "last_note_color"
        val NOTE_COLOR_OPACITY = "note_color_opacity"
        val REMINDER_TYPE = "notification_type"
        val DROPBOX_UID = "dropbox_uid"
        val DROPBOX_TOKEN = "dropbox_token"
        val TELL_ABOUT_EVENT = "tell_about_event"
        val APP_LANGUAGE = "app_language"
        val IGNORE_WINDOW_TYPE = "ignore_window_type"

        val REMINDER_CHANGED = "reminder_changed"
        val LAST_USED_REMINDER = "last_reminder"
        val LAST_LIST = "last_list"
        val NOTES_ORDER = "notes_ordering"
        val NOTES_LIST_STYLE = "notes_style"
        val TASKS_ORDER = "tasks_ordering"
        val RATE_SHOW = "show_rate"
        val APP_RUNS_COUNT = "app_runs"
        val NOTE_HINT_SHOWED = "note_hint_showed"

        // birthdays reminder notification constants
        val BIRTHDAY_USE_GLOBAL = "use_global"
        val BIRTHDAY_VIBRATION_STATUS = "birthday_vibration_status"
        val BIRTHDAY_SILENT_STATUS = "birthday_sound_status"
        val BIRTHDAY_WAKE_STATUS = "birthday_wake_status"
        val BIRTHDAY_INFINITE_SOUND = "birthday_infinite_sound"
        val BIRTHDAY_LED_STATUS = "birthday_led_status"
        val BIRTHDAY_LED_COLOR = "birthday_led_color"
        val BIRTHDAY_INFINITE_VIBRATION = "birthday_infinite_vibration"
        val BIRTHDAY_SOUND_FILE = "birthday_sound_file"
        val BIRTHDAY_REMINDER_TIME = "reminder_hour"
        val BIRTHDAY_TTS = "birthday_tts"
        val BIRTHDAY_TTS_LOCALE = "birthday_tts_locale"

        val IS_CREATE_SHOWN = "create_showcase"
        val IS_CALENDAR_SHOWN = "calendar_showcase"
        val IS_LIST_SHOWN = "list_showcase"
    }
}
