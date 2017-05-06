package com.elementary.tasks.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.elementary.tasks.navigation.settings.images.MainImageActivity;
import com.elementary.tasks.navigation.settings.images.MonthImage;
import com.google.gson.Gson;

import java.io.File;
import java.util.Locale;

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

public final class Prefs extends SharedPrefs {

    private static final String TAG = "Prefs";
    public static final String DRIVE_USER_NONE = "none";

    private static Prefs instance = null;

    private Prefs(Context context) {
        super(context);
    }

    public static Prefs getInstance() {
        if (instance != null) {
            return instance;
        }
        throw new IllegalArgumentException("Use Prefs(Context context) constructor!");
    }

    public static Prefs getInstance(Context context) {
        if (instance == null) {
            synchronized (Prefs.class) {
                if (instance == null) {
                    instance = new Prefs(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public boolean isMigrated() {
        return getBoolean(MIGRATION_COMPLETE);
    }

    public void setMigrated(boolean value) {
        putBoolean(MIGRATION_COMPLETE, value);
    }

    public int getLastUsedReminder() {
        return getInt(LAST_USED_REMINDER);
    }

    public void setLastUsedReminder(int value) {
        putInt(LAST_USED_REMINDER, value);
    }

    public int getMarkerStyle() {
        return getInt(MARKER_STYLE);
    }

    public void setMarkerStyle(int value) {
        putInt(MARKER_STYLE, value);
    }

    public int getScreenOrientation() {
        return getInt(SCREEN);
    }

    public void setScreenOrientation(int value) {
        putInt(SCREEN, value);
    }

    public int getTodayColor() {
        return getInt(TODAY_COLOR);
    }

    public void setTodayColor(int value) {
        putInt(TODAY_COLOR, value);
    }

    public int getReminderColor() {
        return getInt(REMINDER_COLOR);
    }

    public void setReminderColor(int value) {
        putInt(REMINDER_COLOR, value);
    }

    public int getBirthdayColor() {
        return getInt(BIRTH_COLOR);
    }

    public void setBirthdayColor(int value) {
        putInt(BIRTH_COLOR, value);
    }

    public int getAppTheme() {
        return getInt(APP_THEME);
    }

    public void setAppTheme(int value) {
        putInt(APP_THEME, value);
    }

    public int getAppThemeColor() {
        return getInt(APP_THEME_COLOR);
    }

    public void setAppThemeColor(int value) {
        putInt(APP_THEME_COLOR, value);
    }

    public String getNightTime() {
        return getString(TIME_NIGHT);
    }

    public void setNightTime(String value) {
        putString(TIME_NIGHT, value);
    }

    public String getEveningTime() {
        return getString(TIME_EVENING);
    }

    public void setEveningTime(String value) {
        putString(TIME_EVENING, value);
    }

    public String getNoonTime() {
        return getString(TIME_DAY);
    }

    public void setNoonTime(String value) {
        putString(TIME_DAY, value);
    }

    public String getMorningTime() {
        return getString(TIME_MORNING);
    }

    public void setMorningTime(String value) {
        putString(TIME_MORNING, value);
    }

    public int getVoiceLocale() {
        return getInt(VOICE_LOCALE);
    }

    public void setVoiceLocale(int value) {
        putInt(VOICE_LOCALE, value);
    }

    public String getTtsLocale() {
        return getString(TTS_LOCALE);
    }

    public void setTtsLocale(String value) {
        putString(TTS_LOCALE, value);
    }

    public String getBirthdayTtsLocale() {
        return getString(BIRTHDAY_TTS_LOCALE);
    }

    public void setBirthdayTtsLocale(String value) {
        putString(BIRTHDAY_TTS_LOCALE, value);
    }

    public boolean isSystemLoudnessEnabled() {
        return getBoolean(SYSTEM_VOLUME);
    }

    public void setSystemLoudnessEnabled(boolean value) {
        putBoolean(SYSTEM_VOLUME, value);
    }

    public int getSoundStream() {
        return getInt(SOUND_STREAM);
    }

    public void setSoundStream(int value) {
        putInt(SOUND_STREAM, value);
    }

    public boolean is24HourFormatEnabled() {
        return getBoolean(IS_24_TIME_FORMAT);
    }

    public void set24HourFormatEnabled(boolean value) {
        putBoolean(IS_24_TIME_FORMAT, value);
    }

    public boolean isAutoSaveEnabled() {
        return getBoolean(AUTO_SAVE);
    }

    public void setAutoSaveEnabled(boolean value) {
        putBoolean(AUTO_SAVE, value);
    }

    public boolean isCalendarEnabled() {
        return getBoolean(EXPORT_TO_CALENDAR);
    }

    public void setCalendarEnabled(boolean value) {
        putBoolean(EXPORT_TO_CALENDAR, value);
    }

    public boolean isStockCalendarEnabled() {
        return getBoolean(EXPORT_TO_STOCK);
    }

    public void setStockCalendarEnabled(boolean value) {
        putBoolean(EXPORT_TO_STOCK, value);
    }

    public boolean isFoldingEnabled() {
        return getBoolean(SMART_FOLD);
    }

    public void setFoldingEnabled(boolean value) {
        putBoolean(SMART_FOLD, value);
    }

    public boolean isWearEnabled() {
        return getBoolean(WEAR_NOTIFICATION);
    }

    public void setWearEnabled(boolean value) {
        putBoolean(WEAR_NOTIFICATION, value);
    }

    public boolean isUiChanged() {
        return getBoolean(UI_CHANGED);
    }

    public void setUiChanged(boolean value) {
        putBoolean(UI_CHANGED, value);
    }

    public boolean isSbNotificationEnabled() {
        return getBoolean(STATUS_BAR_NOTIFICATION);
    }

    public void setSbNotificationEnabled(boolean value) {
        putBoolean(STATUS_BAR_NOTIFICATION, value);
    }

    public int getImageId() {
        return getInt(MAIN_IMAGE_ID);
    }

    public void setImageId(int value) {
        putInt(MAIN_IMAGE_ID, value);
    }

    public String getImagePath() {
        return getString(MAIN_IMAGE_PATH);
    }

    public void setImagePath(String value) {
        putString(MAIN_IMAGE_PATH, value);
    }

    public MonthImage getCalendarImages() {
        return (MonthImage) getObject(CALENDAR_IMAGES, MonthImage.class);
    }

    public void setCalendarImages(MonthImage value) {
        putObject(MAIN_IMAGE_PATH, value);
    }

    public boolean isCalendarImagesEnabled() {
        return getBoolean(CALENDAR_IMAGE);
    }

    public void setCalendarImagesEnabled(boolean value) {
        putBoolean(CALENDAR_IMAGE, value);
    }

    public boolean isNoteReminderEnabled() {
        return getBoolean(QUICK_NOTE_REMINDER);
    }

    public void setNoteReminderEnabled(boolean value) {
        putBoolean(QUICK_NOTE_REMINDER, value);
    }

    public int getNoteReminderTime() {
        return getInt(QUICK_NOTE_REMINDER_TIME);
    }

    public void setNoteReminderTime(int value) {
        putInt(QUICK_NOTE_REMINDER_TIME, value);
    }

    public int getNoteTextSize() {
        return getInt(NOTE_TEXT_SIZE);
    }

    public void setNoteTextSize(int value) {
        putInt(NOTE_TEXT_SIZE, value);
    }

    public int getRadius() {
        return getInt(LOCATION_RADIUS);
    }

    public void setRadius(int value) {
        putInt(LOCATION_RADIUS, value);
    }

    public boolean isDistanceNotificationEnabled() {
        return getBoolean(TRACKING_NOTIFICATION);
    }

    public void setDistanceNotificationEnabled(boolean value) {
        putBoolean(TRACKING_NOTIFICATION, value);
    }

    public int getMapType() {
        return getInt(MAP_TYPE);
    }

    public void setMapType(int value) {
        putInt(MAP_TYPE, value);
    }

    public int getTrackDistance() {
        return getInt(TRACK_DISTANCE);
    }

    public void setTrackDistance(int value) {
        putInt(TRACK_DISTANCE, value);
    }

    public int getTrackTime() {
        return getInt(TRACK_TIME);
    }

    public void setTrackTime(int value) {
        putInt(TRACK_TIME, value);
    }

    public boolean isMissedReminderEnabled() {
        return getBoolean(MISSED_CALL_REMINDER);
    }

    public void setMissedReminderEnabled(boolean value) {
        putBoolean(MISSED_CALL_REMINDER, value);
    }

    public int getMissedReminderTime() {
        return getInt(MISSED_CALL_TIME);
    }

    public void setMissedReminderTime(int value) {
        putInt(MISSED_CALL_TIME, value);
    }

    public boolean isQuickSmsEnabled() {
        return getBoolean(QUICK_SMS);
    }

    public void setQuickSmsEnabled(boolean value) {
        putBoolean(QUICK_SMS, value);
    }

    public boolean isFollowReminderEnabled() {
        return getBoolean(FOLLOW_REMINDER);
    }

    public void setFollowReminderEnabled(boolean value) {
        putBoolean(FOLLOW_REMINDER, value);
    }

    public String getDriveUser() {
        return SuperUtil.decrypt(getString(DRIVE_USER));
    }

    public void setDriveUser(String value) {
        putString(DRIVE_USER, SuperUtil.encrypt(value));
    }

    public String getReminderImage() {
        return getString(REMINDER_IMAGE);
    }

    public void setReminderImage(String value) {
        putString(REMINDER_IMAGE, value);
    }

    public boolean isBlurEnabled() {
        return getBoolean(REMINDER_IMAGE_BLUR);
    }

    public void setBlurEnabled(boolean value) {
        putBoolean(REMINDER_IMAGE_BLUR, value);
    }

    public boolean isManualRemoveEnabled() {
        return getBoolean(NOTIFICATION_REMOVE);
    }

    public void setManualRemoveEnabled(boolean value) {
        putBoolean(NOTIFICATION_REMOVE, value);
    }

    public boolean isSbIconEnabled() {
        return getBoolean(STATUS_BAR_ICON);
    }

    public void setSbIconEnabled(boolean value) {
        putBoolean(STATUS_BAR_ICON, value);
    }

    public boolean isVibrateEnabled() {
        return getBoolean(VIBRATION_STATUS);
    }

    public void setVibrateEnabled(boolean value) {
        putBoolean(VIBRATION_STATUS, value);
    }

    public boolean isInfiniteVibrateEnabled() {
        return getBoolean(INFINITE_VIBRATION);
    }

    public void setInfiniteVibrateEnabled(boolean value) {
        putBoolean(INFINITE_VIBRATION, value);
    }

    public boolean isSoundInSilentModeEnabled() {
        return getBoolean(SILENT_SOUND);
    }

    public void setSoundInSilentModeEnabled(boolean value) {
        putBoolean(SILENT_SOUND, value);
    }

    public boolean isInfiniteSoundEnabled() {
        return getBoolean(INFINITE_SOUND);
    }

    public void setInfiniteSoundEnabled(boolean value) {
        putBoolean(INFINITE_SOUND, value);
    }

    public String getMelodyFile() {
        return getString(CUSTOM_SOUND);
    }

    public void setMelodyFile(String value) {
        putString(CUSTOM_SOUND, value);
    }

    public int getLoudness() {
        return getInt(VOLUME);
    }

    public void setLoudness(int value) {
        putInt(VOLUME, value);
    }

    public boolean isIncreasingLoudnessEnabled() {
        return getBoolean(INCREASING_VOLUME);
    }

    public void setIncreasingLoudnessEnabled(boolean value) {
        putBoolean(INCREASING_VOLUME, value);
    }

    public boolean isTtsEnabled() {
        return getBoolean(TTS);
    }

    public void setTtsEnabled(boolean value) {
        putBoolean(TTS, value);
    }

    public boolean isDeviceAwakeEnabled() {
        return getBoolean(WAKE_STATUS);
    }

    public void setDeviceAwakeEnabled(boolean value) {
        putBoolean(WAKE_STATUS, value);
    }

    public boolean isDeviceUnlockEnabled() {
        return getBoolean(UNLOCK_DEVICE);
    }

    public void setDeviceUnlockEnabled(boolean value) {
        putBoolean(UNLOCK_DEVICE, value);
    }

    public boolean isAutoSmsEnabled() {
        return getBoolean(SILENT_SMS);
    }

    public void setAutoSmsEnabled(boolean value) {
        putBoolean(SILENT_SMS, value);
    }

    public boolean isAutoLaunchEnabled() {
        return getBoolean(APPLICATION_AUTO_LAUNCH);
    }

    public void setAutoCallEnabled(boolean value) {
        putBoolean(AUTO_CALL, value);
    }

    public boolean isAutoCallEnabled() {
        return getBoolean(AUTO_CALL);
    }

    public void setAutoLaunchEnabled(boolean value) {
        putBoolean(APPLICATION_AUTO_LAUNCH, value);
    }

    public int getSnoozeTime() {
        return getInt(DELAY_TIME);
    }

    public void setSnoozeTime(int value) {
        putInt(DELAY_TIME, value);
    }

    public boolean isNotificationRepeatEnabled() {
        return getBoolean(NOTIFICATION_REPEAT);
    }

    public void setNotificationRepeatEnabled(boolean value) {
        putBoolean(NOTIFICATION_REPEAT, value);
    }

    public int getNotificationRepeatTime() {
        return getInt(NOTIFICATION_REPEAT_INTERVAL);
    }

    public void setNotificationRepeatTime(int value) {
        putInt(NOTIFICATION_REPEAT_INTERVAL, value);
    }

    public boolean isLedEnabled() {
        return getBoolean(LED_STATUS);
    }

    public void setLedEnabled(boolean value) {
        putBoolean(LED_STATUS, value);
    }

    public int getLedColor() {
        return getInt(LED_COLOR);
    }

    public void setLedColor(int value) {
        putInt(LED_COLOR, value);
    }

    public boolean isSettingsBackupEnabled() {
        return getBoolean(EXPORT_SETTINGS);
    }

    public void setSettingsBackupEnabled(boolean value) {
        putBoolean(EXPORT_SETTINGS, value);
    }

    public boolean isAutoBackupEnabled() {
        return getBoolean(AUTO_BACKUP);
    }

    public void setAutoBackupEnabled(boolean value) {
        putBoolean(AUTO_BACKUP, value);
    }

    public int getAutoBackupInterval() {
        return getInt(AUTO_BACKUP_INTERVAL);
    }

    public void setAutoBackupInterval(int value) {
        putInt(AUTO_BACKUP_INTERVAL, value);
    }

    public int getCalendarEventDuration() {
        return getInt(EVENT_DURATION);
    }

    public void setCalendarEventDuration(int value) {
        putInt(EVENT_DURATION, value);
    }

    public int getCalendarId() {
        return getInt(CALENDAR_ID);
    }

    public void setCalendarId(int value) {
        putInt(CALENDAR_ID, value);
    }

    public boolean isFutureEventEnabled() {
        return getBoolean(CALENDAR_FEATURE_TASKS);
    }

    public void setFutureEventEnabled(boolean value) {
        putBoolean(CALENDAR_FEATURE_TASKS, value);
    }

    public boolean isRemindersInCalendarEnabled() {
        return getBoolean(REMINDERS_IN_CALENDAR);
    }

    public void setRemindersInCalendarEnabled(boolean value) {
        putBoolean(REMINDERS_IN_CALENDAR, value);
    }

    public int getStartDay() {
        return getInt(START_DAY);
    }

    public void setStartDay(int value) {
        putInt(START_DAY, value);
    }

    public boolean isAutoEventsCheckEnabled() {
        return getBoolean(AUTO_CHECK_FOR_EVENTS);
    }

    public void setAutoEventsCheckEnabled(boolean value) {
        putBoolean(AUTO_CHECK_FOR_EVENTS, value);
    }

    public int getAutoCheckInterval() {
        return getInt(AUTO_CHECK_FOR_EVENTS_INTERVAL);
    }

    public void setAutoCheckInterval(int value) {
        putInt(AUTO_CHECK_FOR_EVENTS_INTERVAL, value);
    }

    public int getEventsCalendar() {
        return getInt(EVENTS_CALENDAR);
    }

    public void setEventsCalendar(int value) {
        putInt(EVENTS_CALENDAR, value);
    }

    public boolean isBirthdayReminderEnabled() {
        return getBoolean(BIRTHDAY_REMINDER);
    }

    public void setBirthdayReminderEnabled(boolean value) {
        putBoolean(BIRTHDAY_REMINDER, value);
    }

    public String getBirthdayTime() {
        return getString(BIRTHDAY_REMINDER_TIME);
    }

    public void setBirthdayTime(String value) {
        putString(BIRTHDAY_REMINDER_TIME, value);
    }

    public boolean isBirthdayInWidgetEnabled() {
        return getBoolean(WIDGET_BIRTHDAYS);
    }

    public void setBirthdayInWidgetEnabled(boolean value) {
        putBoolean(WIDGET_BIRTHDAYS, value);
    }

    public boolean isBirthdayPermanentEnabled() {
        return getBoolean(BIRTHDAY_PERMANENT);
    }

    public void setBirthdayPermanentEnabled(boolean value) {
        putBoolean(BIRTHDAY_PERMANENT, value);
    }

    public int getDaysToBirthday() {
        return getInt(DAYS_TO_BIRTHDAY);
    }

    public void setDaysToBirthday(int value) {
        putInt(DAYS_TO_BIRTHDAY, value);
    }

    public boolean isContactBirthdaysEnabled() {
        return getBoolean(CONTACT_BIRTHDAYS);
    }

    public void setContactBirthdaysEnabled(boolean value) {
        putBoolean(CONTACT_BIRTHDAYS, value);
    }

    public boolean isContactAutoCheckEnabled() {
        return getBoolean(AUTO_CHECK_BIRTHDAYS);
    }

    public void setContactAutoCheckEnabled(boolean value) {
        putBoolean(AUTO_CHECK_BIRTHDAYS, value);
    }

    public boolean isBirthdayGlobalEnabled() {
        return getBoolean(BIRTHDAY_USE_GLOBAL);
    }

    public void setBirthdayGlobalEnabled(boolean value) {
        putBoolean(BIRTHDAY_USE_GLOBAL, value);
    }

    public boolean isBirthdayVibrationEnabled() {
        return getBoolean(BIRTHDAY_VIBRATION_STATUS);
    }

    public void setBirthdayVibrationEnabled(boolean value) {
        putBoolean(BIRTHDAY_VIBRATION_STATUS, value);
    }

    public boolean isBirthdayInfiniteVibrationEnabled() {
        return getBoolean(BIRTHDAY_INFINITE_VIBRATION);
    }

    public void setBirthdayInfiniteVibrationEnabled(boolean value) {
        putBoolean(BIRTHDAY_INFINITE_VIBRATION, value);
    }

    public boolean isBirthdaySilentEnabled() {
        return getBoolean(BIRTHDAY_SILENT_STATUS);
    }

    public void setBirthdaySilentEnabled(boolean value) {
        putBoolean(BIRTHDAY_SILENT_STATUS, value);
    }

    public boolean isBirthdayInfiniteSoundEnabled() {
        return getBoolean(BIRTHDAY_INFINITE_SOUND);
    }

    public void setBirthdayInfiniteSoundEnabled(boolean value) {
        putBoolean(BIRTHDAY_INFINITE_SOUND, value);
    }

    public boolean isBirthdayWakeEnabled() {
        return getBoolean(BIRTHDAY_WAKE_STATUS);
    }

    public void setBirthdayWakeEnabled(boolean value) {
        putBoolean(BIRTHDAY_WAKE_STATUS, value);
    }

    public boolean isBirthdayLedEnabled() {
        return getBoolean(BIRTHDAY_LED_STATUS);
    }

    public void setBirthdayLedEnabled(boolean value) {
        putBoolean(BIRTHDAY_LED_STATUS, value);
    }

    public boolean isBirthdayTtsEnabled() {
        return getBoolean(BIRTHDAY_TTS);
    }

    public void setBirthdayTtsEnabled(boolean value) {
        putBoolean(BIRTHDAY_TTS, value);
    }

    public int getBirthdayLedColor() {
        return getInt(BIRTHDAY_LED_COLOR);
    }

    public void setBirthdayLedColor(int value) {
        putInt(BIRTHDAY_LED_COLOR, value);
    }

    public String getBirthdayMelody() {
        return getString(BIRTHDAY_SOUND_FILE);
    }

    public void setBirthdayMelody(String value) {
        putString(BIRTHDAY_SOUND_FILE, value);
    }

    public boolean isShowcase(String key) {
        return getBoolean(key);
    }

    public void setShowcase(String key, boolean value) {
        putBoolean(key, value);
    }

    public String getNoteOrder() {
        return getString(NOTES_ORDER);
    }

    public void setNoteOrder(String value) {
        putString(NOTES_ORDER, value);
    }

    public boolean isNotesGridEnabled() {
        return getBoolean(NOTES_LIST_STYLE);
    }

    public void setNotesGridEnabled(boolean value) {
        putBoolean(NOTES_LIST_STYLE, value);
    }

    public int getLastGoogleList() {
        return getInt(LAST_LIST);
    }

    public void setLastGoogleList(int value) {
        putInt(LAST_LIST, value);
    }

    public String getTasksOrder() {
        return getString(TASKS_ORDER);
    }

    public void setTasksOrder(String value) {
        putString(TASKS_ORDER, value);
    }

    public boolean isGcmEnabled() {
        return getBoolean(GCM_ENABLED);
    }

    public void setGcmEnabled(boolean value) {
        putBoolean(GCM_ENABLED, value);
    }

    public void initPrefs(Context context) {
        File settingsUI = new File("/data/data/" + context.getPackageName() + "/shared_prefs/" + PREFS_NAME + ".xml");
        if (!settingsUI.exists()) {
            SharedPreferences appUISettings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor uiEd = appUISettings.edit();
            uiEd.putInt(APP_THEME, ThemeUtil.THEME_AUTO);
            uiEd.putInt(APP_THEME_COLOR, 8);
            uiEd.putInt(TODAY_COLOR, 0);
            uiEd.putInt(BIRTH_COLOR, 2);
            uiEd.putInt(REMINDER_COLOR, 4);
            uiEd.putInt(MAP_TYPE, Constants.MAP_NORMAL);
            uiEd.putString(DRIVE_USER, DRIVE_USER_NONE);
            uiEd.putString(REMINDER_IMAGE, Constants.DEFAULT);
            uiEd.putInt(LED_COLOR, LED.BLUE);
            uiEd.putInt(BIRTHDAY_LED_COLOR, LED.BLUE);
            uiEd.putInt(LOCATION_RADIUS, 25);
            uiEd.putInt(MARKER_STYLE, 5);
            uiEd.putInt(TRACK_DISTANCE, 1);
            uiEd.putInt(TRACK_TIME, 1);
            uiEd.putInt(QUICK_NOTE_REMINDER_TIME, 10);
            uiEd.putInt(NOTE_TEXT_SIZE, 4);
            uiEd.putInt(VOLUME, 25);
            String localeCheck = Locale.getDefault().toString().toLowerCase();
            int locale;
            if (localeCheck.startsWith("uk")) {
                locale = 2;
            } else if (localeCheck.startsWith("ru")) {
                locale = 1;
            } else {
                locale = 0;
            }
            uiEd.putInt(VOICE_LOCALE, locale);
            uiEd.putString(TIME_MORNING, "7:0");
            uiEd.putString(TIME_DAY, "12:0");
            uiEd.putString(TIME_EVENING, "19:0");
            uiEd.putString(TIME_NIGHT, "23:0");
            uiEd.putString(TTS_LOCALE, Language.ENGLISH);
            uiEd.putString(MAIN_IMAGE_PATH, MainImageActivity.DEFAULT_PHOTO);
            uiEd.putString(CALENDAR_IMAGES, new Gson().toJson(new MonthImage()));
            uiEd.putString(CUSTOM_SOUND, Constants.DEFAULT);
            uiEd.putInt(START_DAY, 1);
            uiEd.putInt(DAYS_TO_BIRTHDAY, 0);
            uiEd.putInt(NOTIFICATION_REPEAT_INTERVAL, 15);
            uiEd.putInt(APP_RUNS_COUNT, 0);
            uiEd.putInt(DELAY_TIME, 5);
            uiEd.putInt(EVENT_DURATION, 30);
            uiEd.putInt(MISSED_CALL_TIME, 10);
            uiEd.putInt(AUTO_BACKUP_INTERVAL, 6);
            uiEd.putInt(AUTO_CHECK_FOR_EVENTS_INTERVAL, 6);
            uiEd.putInt(SOUND_STREAM, 5);
            uiEd.putInt(MAIN_IMAGE_ID, -1);
            uiEd.putInt(NOTE_COLOR_OPACITY, 100);
            uiEd.putBoolean(TRACKING_NOTIFICATION, true);
            uiEd.putBoolean(RATE_SHOW, false);
            uiEd.putBoolean(IS_CREATE_SHOWN, false);
            uiEd.putBoolean(IS_CALENDAR_SHOWN, false);
            uiEd.putBoolean(IS_LIST_SHOWN, false);
            uiEd.putBoolean(CONTACT_BIRTHDAYS, false);
            uiEd.putBoolean(BIRTHDAY_REMINDER, false);
            uiEd.putBoolean(CALENDAR_IMAGE, false);
            uiEd.putBoolean(EXPORT_TO_CALENDAR, false);
            uiEd.putBoolean(AUTO_CHECK_BIRTHDAYS, false);
            uiEd.putBoolean(INFINITE_VIBRATION, false);
            uiEd.putBoolean(NOTIFICATION_REPEAT, false);
            uiEd.putBoolean(WIDGET_BIRTHDAYS, false);
            uiEd.putBoolean(QUICK_NOTE_REMINDER, false);
            uiEd.putBoolean(EXPORT_TO_STOCK, false);
            uiEd.putBoolean(REMINDERS_IN_CALENDAR, true);
            uiEd.putBoolean(IS_24_TIME_FORMAT, true);
            uiEd.putBoolean(UNLOCK_DEVICE, false);
            uiEd.putBoolean(WAKE_STATUS, false);
            uiEd.putBoolean(CALENDAR_FEATURE_TASKS, true);
            uiEd.putBoolean(MISSED_CALL_REMINDER, false);
            uiEd.putBoolean(QUICK_SMS, false);
            uiEd.putBoolean(FOLLOW_REMINDER, false);
            uiEd.putBoolean(TTS, false);
            uiEd.putBoolean(BIRTHDAY_PERMANENT, false);
            uiEd.putBoolean(REMINDER_CHANGED, false);
            uiEd.putBoolean(REMINDER_IMAGE_BLUR, false);
            uiEd.putBoolean(SYSTEM_VOLUME, false);
            uiEd.putBoolean(INCREASING_VOLUME, false);
            uiEd.putBoolean(GCM_ENABLED, true);
            uiEd.putBoolean(LIVE_CONVERSATION, true);
            if (Module.isPro()) {
                uiEd.putBoolean(BIRTHDAY_LED_STATUS, false);
                uiEd.putBoolean(LED_STATUS, true);
                uiEd.putInt(BIRTHDAY_LED_COLOR, 6);
                uiEd.putInt(LED_COLOR, 11);
                uiEd.putBoolean(BIRTHDAY_USE_GLOBAL, true);
                uiEd.putBoolean(BIRTHDAY_INFINITE_VIBRATION, false);
                uiEd.putBoolean(BIRTHDAY_VIBRATION_STATUS, false);
                uiEd.putBoolean(BIRTHDAY_WAKE_STATUS, false);
            }
            uiEd.apply();
        }
    }

    public void checkPrefs() {
        if (!hasKey(TODAY_COLOR)) {
            putInt(TODAY_COLOR, 4);
        }
        if (!hasKey(BIRTH_COLOR)) {
            putInt(BIRTH_COLOR, 1);
        }
        if (!hasKey(REMINDER_COLOR)) {
            putInt(REMINDER_COLOR, 6);
        }
        if (!hasKey(APP_THEME)) {
            putInt(APP_THEME, ThemeUtil.THEME_AUTO);
        }
        if (!hasKey(APP_THEME_COLOR)) {
            putInt(APP_THEME_COLOR, 8);
        }
        if (!hasKey(DRIVE_USER)) {
            putString(DRIVE_USER, DRIVE_USER_NONE);
        }
        if (!hasKey(TTS_LOCALE)) {
            putString(TTS_LOCALE, Language.ENGLISH);
        }
        if (!hasKey(REMINDER_IMAGE)) {
            putString(REMINDER_IMAGE, Constants.DEFAULT);
        }

        if (!hasKey(VOICE_LOCALE)) {
            putInt(VOICE_LOCALE, 0);
        }
        if (!hasKey(TIME_MORNING)) {
            putString(TIME_MORNING, "7:0");
        }
        if (!hasKey(TIME_DAY)) {
            putString(TIME_DAY, "12:0");
        }
        if (!hasKey(TIME_EVENING)) {
            putString(TIME_EVENING, "19:0");
        }
        if (!hasKey(TIME_NIGHT)) {
            putString(TIME_NIGHT, "23:0");
        }
        if (!hasKey(DAYS_TO_BIRTHDAY)) {
            putInt(DAYS_TO_BIRTHDAY, 0);
        }
        if (!hasKey(QUICK_NOTE_REMINDER_TIME)) {
            putInt(QUICK_NOTE_REMINDER_TIME, 10);
        }
        if (!hasKey(NOTE_TEXT_SIZE)) {
            putInt(NOTE_TEXT_SIZE, 4);
        }
        if (!hasKey(START_DAY)) {
            putInt(START_DAY, 1);
        }
        if (!hasKey(BIRTHDAY_REMINDER_TIME)) {
            putString(BIRTHDAY_REMINDER_TIME, "12:00");
        }
        if (!hasKey(TRACK_DISTANCE)) {
            putInt(TRACK_DISTANCE, 1);
        }
        if (!hasKey(AUTO_BACKUP_INTERVAL)) {
            putInt(AUTO_BACKUP_INTERVAL, 6);
        }
        if (!hasKey(AUTO_CHECK_FOR_EVENTS_INTERVAL)) {
            putInt(AUTO_CHECK_FOR_EVENTS_INTERVAL, 6);
        }
        if (!hasKey(TRACK_TIME)) {
            putInt(TRACK_TIME, 1);
        }
        if (!hasKey(APP_RUNS_COUNT)) {
            putInt(APP_RUNS_COUNT, 0);
        }
        if (!hasKey(DELAY_TIME)) {
            putInt(DELAY_TIME, 5);
        }
        if (!hasKey(EVENT_DURATION)) {
            putInt(EVENT_DURATION, 30);
        }
        if (!hasKey(NOTIFICATION_REPEAT_INTERVAL)) {
            putInt(NOTIFICATION_REPEAT_INTERVAL, 15);
        }
        if (!hasKey(VOLUME)) {
            putInt(VOLUME, 25);
        }
        if (!hasKey(MAP_TYPE)) {
            putInt(MAP_TYPE, Constants.MAP_NORMAL);
        }
        if (!hasKey(MISSED_CALL_TIME)) {
            putInt(MISSED_CALL_TIME, 10);
        }
        if (!hasKey(SOUND_STREAM)) {
            putInt(SOUND_STREAM, 5);
        }
        if (!hasKey(RATE_SHOW)) {
            putBoolean(RATE_SHOW, false);
        }
        if (!hasKey(REMINDER_IMAGE_BLUR)) {
            putBoolean(REMINDER_IMAGE_BLUR, false);
        }
        if (!hasKey(QUICK_NOTE_REMINDER)) {
            putBoolean(QUICK_NOTE_REMINDER, false);
        }
        if (!hasKey(REMINDERS_IN_CALENDAR)) {
            putBoolean(REMINDERS_IN_CALENDAR, false);
        }
        if (!hasKey(TTS)) {
            putBoolean(TTS, false);
        }
        if (!hasKey(CONTACT_BIRTHDAYS)) {
            putBoolean(CONTACT_BIRTHDAYS, false);
        }
        if (!hasKey(BIRTHDAY_REMINDER)) {
            putBoolean(BIRTHDAY_REMINDER, true);
        }
        if (!hasKey(CALENDAR_IMAGE)) {
            putBoolean(CALENDAR_IMAGE, false);
        }
        if (!hasKey(SILENT_SMS)) {
            putBoolean(SILENT_SMS, false);
        }
        if (!hasKey(ITEM_PREVIEW)) {
            putBoolean(ITEM_PREVIEW, true);
        }
        if (!hasKey(WIDGET_BIRTHDAYS)) {
            putBoolean(WIDGET_BIRTHDAYS, false);
        }
        if (!hasKey(WEAR_NOTIFICATION)) {
            putBoolean(WEAR_NOTIFICATION, false);
        }
        if (!hasKey(EXPORT_TO_STOCK)) {
            putBoolean(EXPORT_TO_STOCK, false);
        }
        if (!hasKey(EXPORT_TO_CALENDAR)) {
            putBoolean(EXPORT_TO_CALENDAR, false);
        }
        if (!hasKey(AUTO_CHECK_BIRTHDAYS)) {
            putBoolean(AUTO_CHECK_BIRTHDAYS, false);
        }
        if (!hasKey(INFINITE_VIBRATION)) {
            putBoolean(INFINITE_VIBRATION, false);
        }
        if (!hasKey(AUTO_BACKUP)) {
            putBoolean(AUTO_BACKUP, false);
        }
        if (!hasKey(SMART_FOLD)) {
            putBoolean(SMART_FOLD, false);
        }
        if (!hasKey(NOTIFICATION_REPEAT)) {
            putBoolean(NOTIFICATION_REPEAT, false);
        }
        if (!hasKey(IS_24_TIME_FORMAT)) {
            putBoolean(IS_24_TIME_FORMAT, true);
        }
        if (!hasKey(UNLOCK_DEVICE)) {
            putBoolean(UNLOCK_DEVICE, false);
        }
        if (!hasKey(CALENDAR_FEATURE_TASKS)) {
            putBoolean(CALENDAR_FEATURE_TASKS, false);
        }
        if (!hasKey(MISSED_CALL_REMINDER)) {
            putBoolean(MISSED_CALL_REMINDER, false);
        }
        if (!hasKey(QUICK_SMS)) {
            putBoolean(QUICK_SMS, false);
        }
        if (!hasKey(FOLLOW_REMINDER)) {
            putBoolean(FOLLOW_REMINDER, false);
        }
        if (!hasKey(BIRTHDAY_PERMANENT)) {
            putBoolean(BIRTHDAY_PERMANENT, false);
        }
        if (!hasKey(REMINDER_CHANGED)) {
            putBoolean(REMINDER_CHANGED, false);
        }
        if (!hasKey(SYSTEM_VOLUME)) {
            putBoolean(SYSTEM_VOLUME, false);
        }
        if (!hasKey(INCREASING_VOLUME)) {
            putBoolean(INCREASING_VOLUME, false);
        }
        if (!hasKey(WAKE_STATUS)) {
            putBoolean(WAKE_STATUS, false);
        }
        if (!hasKey(GCM_ENABLED)) {
            putBoolean(GCM_ENABLED, true);
        }
        if (!hasKey(LIVE_CONVERSATION)) {
            putBoolean(LIVE_CONVERSATION, true);
        }
        if (!hasKey(MAIN_IMAGE_ID)) {
            putInt(MAIN_IMAGE_ID, -1);
        }
        if (!hasKey(NOTE_COLOR_OPACITY)) {
            putInt(NOTE_COLOR_OPACITY, 100);
        }
        if (!hasKey(MAIN_IMAGE_PATH)) {
            putString(MAIN_IMAGE_PATH, MainImageActivity.DEFAULT_PHOTO);
        }
        if (!hasKey(CUSTOM_SOUND)) {
            putString(CUSTOM_SOUND, Constants.DEFAULT);
        }
        if (!hasKey(CALENDAR_IMAGES)) {
            putObject(CALENDAR_IMAGES, new MonthImage());
        }
        if (Module.isPro()) {
            if (!hasKey(LED_STATUS)) {
                putBoolean(LED_STATUS, true);
            }
            if (!hasKey(LED_COLOR)) {
                putInt(LED_COLOR, 11);
            }
            if (!hasKey(BIRTHDAY_LED_STATUS)) {
                putBoolean(BIRTHDAY_LED_STATUS, false);
            }
            if (!hasKey(BIRTHDAY_LED_COLOR)) {
                putInt(BIRTHDAY_LED_COLOR, 6);
            }
            if (!hasKey(BIRTHDAY_VIBRATION_STATUS)) {
                putBoolean(BIRTHDAY_VIBRATION_STATUS, false);
            }
            if (!hasKey(BIRTHDAY_SILENT_STATUS)) {
                putBoolean(BIRTHDAY_SILENT_STATUS, false);
            }
            if (!hasKey(BIRTHDAY_WAKE_STATUS)) {
                putBoolean(BIRTHDAY_WAKE_STATUS, false);
            }
            if (!hasKey(BIRTHDAY_INFINITE_SOUND)) {
                putBoolean(BIRTHDAY_INFINITE_SOUND, false);
            }
            if (!hasKey(BIRTHDAY_INFINITE_VIBRATION)) {
                putBoolean(BIRTHDAY_INFINITE_VIBRATION, false);
            }
            if (!hasKey(BIRTHDAY_USE_GLOBAL)) {
                putBoolean(BIRTHDAY_USE_GLOBAL, true);
            }
        } else {
            putInt(MARKER_STYLE, 5);
        }
    }

    public boolean isBetaWarmingShowed() {
        return getBoolean(BETA_KEY);
    }

    public void setBetaWarmingShowed(boolean value) {
        putBoolean(BETA_KEY, value);
    }

    public int getRateCount() {
        return getInt(RATE_COUNT);
    }

    public void setRateCount(int count) {
        putInt(RATE_COUNT, count);
    }

    public boolean isUserLogged() {
        return getBoolean(USER_LOGGED);
    }

    public void setUserLogged(boolean value) {
        putBoolean(USER_LOGGED, value);
    }

    public void setLiveEnabled(boolean value) {
        putBoolean(LIVE_CONVERSATION, value);
    }

    public boolean isLiveEnabled() {
        return getBoolean(LIVE_CONVERSATION);
    }

    public void setNoteColorRememberingEnabled(boolean value) {
        putBoolean(REMEMBER_NOTE_COLOR, value);
    }

    public boolean isNoteColorRememberingEnabled() {
        return getBoolean(REMEMBER_NOTE_COLOR);
    }

    public int getLastNoteColor() {
        return getInt(LAST_NOTE_COLOR);
    }

    public void setLastNoteColor(int count) {
        putInt(LAST_NOTE_COLOR, count);
    }

    public int getNoteColorOpacity() {
        return getInt(NOTE_COLOR_OPACITY);
    }

    public void setNoteColorOpacity(int count) {
        putInt(NOTE_COLOR_OPACITY, count);
    }

    public void setNoteHintShowed(boolean value) {
        putBoolean(NOTE_HINT_SHOWED, value);
    }

    public boolean isNoteHintShowed() {
        return getBoolean(NOTE_HINT_SHOWED);
    }
}
