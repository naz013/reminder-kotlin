/**
 * Copyright 2016 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elementary.tasks.core.utils;

import android.content.Context;

public class Prefs extends SharedPrefs {
    private static final String TECH_ONE = "RanGuide";

    private static final String EVENTS_CALENDAR = "events_cal";
    private static final String AUTO_CHECK_FOR_EVENTS = "auto_events";
    private static final String AUTO_CHECK_FOR_EVENTS_INTERVAL = "auto_events_interval";
    private static final String APP_THEME = "theme_";
    private static final String DRIVE_USER = "ggl_user";
    private static final String SCREEN = "screen";
    private static final String USE_DARK_THEME = "dark_theme";
    private static final String DAY_NIGHT = "day_night";
    private static final String MAP_TYPE = "new_map_type";
    private static final String LOCATION_RADIUS = "radius";
    private static final String TRACKING_NOTIFICATION = "tracking_notification";
    private static final String VIBRATION_STATUS = "vibration_status";
    private static final String SILENT_SOUND = "sound_status";
    private static final String WAKE_STATUS = "wake_status";
    private static final String INFINITE_SOUND = "infinite_sound";
    private static final String SILENT_SMS = "silent_sms";
    private static final String CONTACT_BIRTHDAYS = "use_contacts";
    private static final String BIRTHDAY_REMINDER = "birthdays_reminder";
    private static final String START_DAY = "start_day";
    private static final String DAYS_TO_BIRTHDAY = "days_to";
    private static final String DELAY_TIME = "delay_time";
    private static final String EVENT_DURATION = "event_duration";
    private static final String EXPORT_TO_CALENDAR = "export_to_calendar";
    private static final String CALENDAR_NAME = "cal_name";
    private static final String CALENDAR_ID = "cal_id";
    private static final String AUTO_CHECK_BIRTHDAYS = "birthdays_auto_scan";
    private static final String LED_STATUS = "led_status";
    private static final String LED_COLOR = "led_color";
    private static final String MARKER_STYLE = "marker_style";
    private static final String INFINITE_VIBRATION = "infinite_vibration";
    private static final String AUTO_BACKUP = "auto_backup";
    private static final String AUTO_BACKUP_INTERVAL = "auto_backup_interval";
    private static final String SMART_FOLD = "smart_fold";
    private static final String NOTIFICATION_REPEAT = "notification_repeat";
    private static final String NOTIFICATION_REPEAT_INTERVAL = "notification_repeat_interval";
    private static final String WEAR_NOTIFICATION = "wear_notification";
    private static final String WIDGET_BIRTHDAYS = "widget_birthdays";
    private static final String NOTIFICATION_REMOVE = "notification_remove";
    private static final String BIRTH_COLOR = "birth_color";
    private static final String TODAY_COLOR = "today_color";
    private static final String STATUS_BAR_ICON = "status_icon";
    private static final String STATUS_BAR_NOTIFICATION = "status_notification";
    private static final String TRACK_TIME = "tracking_time";
    private static final String TRACK_DISTANCE = "tracking_distance";
    private static final String NOTE_ENCRYPT = "note_encrypt";
    private static final String TEXT_SIZE = "text_size";
    private static final String QUICK_NOTE_REMINDER = "quick_note_reminder";
    private static final String QUICK_NOTE_REMINDER_TIME = "quick_note_reminder_time";
    private static final String SYNC_NOTES = "sync_notes";
    private static final String SYNC_BIRTHDAYS = "sync_birthdays";
    private static final String DELETE_NOTE_FILE = "delete_note_file";
    private static final String VOICE_LOCALE = "voice_locale";
    private static final String TIME_MORNING = "time_morning";
    private static final String TIME_DAY = "time_day";
    private static final String TIME_EVENING = "time_evening";
    private static final String TIME_NIGHT = "time_night";
    private static final String EXPORT_TO_STOCK = "export_to_stock";
    private static final String APPLICATION_AUTO_LAUNCH = "application_auto_launch";
    private static final String NOTES_ORDER = "notes_ordering";
    private static final String TASKS_ORDER = "tasks_ordering";
    private static final String REMINDERS_IN_CALENDAR = "reminders_in_calendar";
    private static final String REMINDER_COLOR = "reminder_color";
    private static final String IS_24_TIME_FORMAT = "24_hour_format";
    private static final String UNLOCK_DEVICE = "unlock_device";
    private static final String CALENDAR_FEATURE_TASKS = "feature_tasks";
    private static final String MISSED_CALL_REMINDER = "missed_call_reminder";
    private static final String MISSED_CALL_TIME = "missed_call_time";
    private static final String VOLUME = "reminder_volume";
    private static final String QUICK_SMS = "quick_sms";
    private static final String FOLLOW_REMINDER = "follow_reminder";
    private static final String LAST_LIST = "last_list";
    private static final String TTS = "tts_enabled";
    private static final String TTS_LOCALE = "tts_locale";
    private static final String LAST_USED_REMINDER = "last_reminder";
    private static final String ITEM_PREVIEW = "item_preview";
    private static final String THANKS_SHOWN = "thanks_shown";
    private static final String LAST_CALENDAR_VIEW = "last_calendar_view";
    private static final String UI_CHANGED = "ui_changed";
    private static final String TASK_CHANGED = "task_changed";
    private static final String LAST_FRAGMENT = "last_fragment";
    private static final String CALENDAR_IMAGE = "calendar_image";
    private static final String EXPORT_SETTINGS = "export_settings";
    private final static String CUSTOM_SOUND = "custom_sound";
    private final static String CUSTOM_SOUND_FILE = "sound_file";
    private final static String BIRTHDAY_PERMANENT = "birthday_permanent";
    private final static String REMINDER_IMAGE = "reminder_image";
    private final static String REMINDER_IMAGE_BLUR = "reminder_image_blur";
    private final static String SYSTEM_VOLUME = "system_volume";
    private final static String SOUND_STREAM = "sound_stream";
    private final static String INCREASING_VOLUME = "increasing_volume";
    private final static String MAIN_IMAGE_PATH = "main_image_path";
    private final static String MAIN_IMAGE_ID = "main_image_id";
    private final static String CALENDAR_IMAGES = "calendar_images";

    private final static String REMINDER_CHANGED = "reminder_changed";
    private final static String NOTE_CHANGED = "note_changed";
    private final static String GROUP_CHANGED = "group_changed";
    private final static String PLACE_CHANGED = "place_changed";
    private final static String TEMPLATE_CHANGED = "template_changed";

    // birthdays reminder notification constants
    private static final String BIRTHDAY_USE_GLOBAL = "use_global";
    private static final String BIRTHDAY_VIBRATION_STATUS = "birthday_vibration_status";
    private static final String BIRTHDAY_SOUND_STATUS = "birthday_sound_status";
    private static final String BIRTHDAY_WAKE_STATUS = "birthday_wake_status";
    private static final String BIRTHDAY_INFINITE_SOUND = "birthday_infinite_sound";
    private static final String BIRTHDAY_LED_STATUS = "birthday_led_status";
    private static final String BIRTHDAY_LED_COLOR = "birthday_led_color";
    private static final String BIRTHDAY_INFINITE_VIBRATION = "birthday_infinite_vibration";
    private final static String BIRTHDAY_CUSTOM_SOUND = "birthday_custom_sound";
    private final static String BIRTHDAY_CUSTOM_SOUND_FILE = "birthday_sound_file";
    private static final String CONTACTS_IMPORT_DIALOG = "contacts_imported";
    private static final String RATE_SHOW = "show_rate";
    private static final String APP_RUNS_COUNT = "app_runs";
    private static final String BIRTHDAY_REMINDER_HOUR = "reminder_hour";
    private static final String BIRTHDAY_REMINDER_MINUTE = "reminder_minute";
    private static final String BIRTHDAY_TTS = "birthday_tts";
    private static final String BIRTHDAY_TTS_LOCALE = "birthday_tts_locale";

    private static final String IS_CREATE_SHOWN = "create_showcase";
    private static final String IS_CALENDAR_SHOWN = "calendar_showcase";
    private static final String IS_LIST_SHOWN = "list_showcase";
    private static final String IS_MIGRATION = "is_migrate";

    private static Prefs instance;

    public static Prefs getInstance(Context context) {
        if (instance == null) {
            instance = new Prefs(context);
        }
        return null;
    }

    private Prefs(Context context) {
        super(context);
    }
}
