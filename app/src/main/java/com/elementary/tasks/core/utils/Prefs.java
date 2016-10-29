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
import android.content.SharedPreferences;

import com.elementary.tasks.core.LED;
import com.elementary.tasks.core.Language;
import com.elementary.tasks.navigation.settings.images.MainImageActivity;
import com.elementary.tasks.navigation.settings.images.MonthImage;
import com.google.gson.Gson;

import java.io.File;
import java.util.Locale;

public class Prefs extends SharedPrefs {

    private static final String PREFERENCES_NAME = "ui_settings";
    public static final String DRIVE_USER_NONE = "none";

    private static Prefs instance;

    public static Prefs getInstance(Context context) {
        if (instance == null) {
            instance = new Prefs(context);
        }
        return instance;
    }

    private Prefs(Context context) {
        super(context);
    }

    public int getMarkerStyle() {
        return getInt(MARKER_STYLE);
    }

    public void setMarkerStyle(int value) {
        putInt(MARKER_STYLE, value);
    }

    public boolean hasScreenOrientation() {
        return hasKey(SCREEN);
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

    public boolean isNightModeEnabled() {
        return getBoolean(DAY_NIGHT);
    }

    public void setNightModeEnabled(boolean value) {
        putBoolean(DAY_NIGHT, value);
    }

    public boolean isDarkModeEnabled() {
        return getBoolean(USE_DARK_THEME);
    }

    public void setDarkModeEnabled(boolean value) {
        putBoolean(USE_DARK_THEME, value);
    }

    public boolean hasAppTheme() {
        return hasKey(APP_THEME);
    }

    public int getAppTheme() {
        return getInt(APP_THEME);
    }

    public void setAppTheme(int value) {
        putInt(APP_THEME, value);
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

    public boolean isSystemVolume() {
        return getBoolean(SYSTEM_VOLUME);
    }

    public void setSystemVolume(boolean value) {
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

    public boolean isSBNotificationEnabled() {
        return getBoolean(STATUS_BAR_NOTIFICATION);
    }

    public void setSBNotificationEnabled(boolean value) {
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

    public void initPrefs(Context context) {
        File settingsUI = new File("/data/data/" + context.getPackageName() + "/shared_prefs/" + PREFERENCES_NAME + ".xml");
        if (!settingsUI.exists()) {
            SharedPreferences appUISettings = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor uiEd = appUISettings.edit();
            uiEd.putInt(APP_THEME, 8);
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
            } else locale = 0;
            uiEd.putInt(VOICE_LOCALE, locale);
            uiEd.putString(TIME_MORNING, "7:0");
            uiEd.putString(TIME_DAY, "12:0");
            uiEd.putString(TIME_EVENING, "19:0");
            uiEd.putString(TIME_NIGHT, "23:0");
            uiEd.putString(TTS_LOCALE, Language.ENGLISH);
            uiEd.putString(MAIN_IMAGE_PATH, MainImageActivity.DEFAULT_PHOTO);
            uiEd.putString(CALENDAR_IMAGES, new Gson().toJson(new MonthImage()));
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
            uiEd.putBoolean(TRACKING_NOTIFICATION, true);
            uiEd.putBoolean(RATE_SHOW, false);
            uiEd.putBoolean(IS_CREATE_SHOWN, false);
            uiEd.putBoolean(IS_CALENDAR_SHOWN, false);
            uiEd.putBoolean(IS_LIST_SHOWN, false);
            uiEd.putBoolean(CONTACT_BIRTHDAYS, false);
            uiEd.putBoolean(BIRTHDAY_REMINDER, true);
            uiEd.putBoolean(CALENDAR_IMAGE, false);
            uiEd.putBoolean(USE_DARK_THEME, false);
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
            uiEd.putBoolean(DAY_NIGHT, false);
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

    public void checkPrefs(){
        if (!hasKey(TODAY_COLOR)){
            putInt(TODAY_COLOR, 4);
        }
        if (!hasKey(BIRTH_COLOR)){
            putInt(BIRTH_COLOR, 1);
        }
        if (!hasKey(REMINDER_COLOR)){
            putInt(REMINDER_COLOR, 6);
        }
        if (!hasKey(APP_THEME)){
            putInt(APP_THEME, 8);
        }
        if (!hasKey(DRIVE_USER)){
            putString(DRIVE_USER, DRIVE_USER_NONE);
        }
        if (!hasKey(TTS_LOCALE)){
            putString(TTS_LOCALE, Language.ENGLISH);
        }
        if (!hasKey(REMINDER_IMAGE)){
            putString(REMINDER_IMAGE, Constants.DEFAULT);
        }

        if (!hasKey(VOICE_LOCALE)){
            putInt(VOICE_LOCALE, 0);
        }
        if (!hasKey(TIME_MORNING)){
            putString(TIME_MORNING, "7:0");
        }
        if (!hasKey(TIME_DAY)){
            putString(TIME_DAY, "12:0");
        }
        if (!hasKey(TIME_EVENING)){
            putString(TIME_EVENING, "19:0");
        }
        if (!hasKey(TIME_NIGHT)){
            putString(TIME_NIGHT, "23:0");
        }
        if (!hasKey(DAYS_TO_BIRTHDAY)){
            putInt(DAYS_TO_BIRTHDAY, 0);
        }
        if (!hasKey(QUICK_NOTE_REMINDER_TIME)){
            putInt(QUICK_NOTE_REMINDER_TIME, 10);
        }
        if (!hasKey(NOTE_TEXT_SIZE)){
            putInt(NOTE_TEXT_SIZE, 4);
        }
        if (!hasKey(START_DAY)){
            putInt(START_DAY, 1);
        }
        if (!hasKey(BIRTHDAY_REMINDER_HOUR)){
            putInt(BIRTHDAY_REMINDER_HOUR, 12);
        }
        if (!hasKey(BIRTHDAY_REMINDER_MINUTE)){
            putInt(BIRTHDAY_REMINDER_MINUTE, 0);
        }
        if (!hasKey(TRACK_DISTANCE)){
            putInt(TRACK_DISTANCE, 1);
        }
        if (!hasKey(AUTO_BACKUP_INTERVAL)){
            putInt(AUTO_BACKUP_INTERVAL, 6);
        }
        if (!hasKey(AUTO_CHECK_FOR_EVENTS_INTERVAL)){
            putInt(AUTO_CHECK_FOR_EVENTS_INTERVAL, 6);
        }
        if (!hasKey(TRACK_TIME)){
            putInt(TRACK_TIME, 1);
        }
        if (!hasKey(APP_RUNS_COUNT)){
            putInt(APP_RUNS_COUNT, 0);
        }
        if (!hasKey(DELAY_TIME)){
            putInt(DELAY_TIME, 5);
        }
        if (!hasKey(EVENT_DURATION)){
            putInt(EVENT_DURATION, 30);
        }
        if (!hasKey(NOTIFICATION_REPEAT_INTERVAL)){
            putInt(NOTIFICATION_REPEAT_INTERVAL, 15);
        }
        if (!hasKey(VOLUME)){
            putInt(VOLUME, 25);
        }
        if (!hasKey(MAP_TYPE)){
            putInt(MAP_TYPE, Constants.MAP_NORMAL);
        }
        if (!hasKey(MISSED_CALL_TIME)){
            putInt(MISSED_CALL_TIME, 10);
        }
        if (!hasKey(SOUND_STREAM)){
            putInt(SOUND_STREAM, 5);
        }

        if (!hasKey(DAY_NIGHT)){
            putBoolean(DAY_NIGHT, false);
        }
        if (!hasKey(RATE_SHOW)){
            putBoolean(RATE_SHOW, false);
        }
        if (!hasKey(REMINDER_IMAGE_BLUR)){
            putBoolean(REMINDER_IMAGE_BLUR, false);
        }
        if (!hasKey(QUICK_NOTE_REMINDER)){
            putBoolean(QUICK_NOTE_REMINDER, false);
        }
        if (!hasKey(REMINDERS_IN_CALENDAR)){
            putBoolean(REMINDERS_IN_CALENDAR, false);
        }
        if (!hasKey(TTS)){
            putBoolean(TTS, false);
        }
        if (!hasKey(CONTACTS_IMPORT_DIALOG)){
            putBoolean(CONTACTS_IMPORT_DIALOG, false);
        }
        if (!hasKey(CONTACT_BIRTHDAYS)){
            putBoolean(CONTACT_BIRTHDAYS, false);
        }
        if (!hasKey(BIRTHDAY_REMINDER)){
            putBoolean(BIRTHDAY_REMINDER, true);
        }
        if (!hasKey(CALENDAR_IMAGE)){
            putBoolean(CALENDAR_IMAGE, false);
        }
        if (!hasKey(SILENT_SMS)){
            putBoolean(SILENT_SMS, false);
        }
        if (!hasKey(ITEM_PREVIEW)){
            putBoolean(ITEM_PREVIEW, true);
        }
        if (!hasKey(WIDGET_BIRTHDAYS)){
            putBoolean(WIDGET_BIRTHDAYS, false);
        }
        if (!hasKey(WEAR_NOTIFICATION)){
            putBoolean(WEAR_NOTIFICATION, false);
        }
        if (!hasKey(EXPORT_TO_STOCK)){
            putBoolean(EXPORT_TO_STOCK, false);
        }
        if (!hasKey(USE_DARK_THEME)){
            putBoolean(USE_DARK_THEME, false);
        }
        if (!hasKey(EXPORT_TO_CALENDAR)){
            putBoolean(EXPORT_TO_CALENDAR, false);
        }
        if (!hasKey(AUTO_CHECK_BIRTHDAYS)){
            putBoolean(AUTO_CHECK_BIRTHDAYS, false);
        }
        if (!hasKey(INFINITE_VIBRATION)){
            putBoolean(INFINITE_VIBRATION, false);
        }
        if (!hasKey(AUTO_BACKUP)){
            putBoolean(AUTO_BACKUP, false);
        }
        if (!hasKey(SMART_FOLD)){
            putBoolean(SMART_FOLD, false);
        }
        if (!hasKey(NOTIFICATION_REPEAT)){
            putBoolean(NOTIFICATION_REPEAT, false);
        }
        if (!hasKey(IS_24_TIME_FORMAT)){
            putBoolean(IS_24_TIME_FORMAT, true);
        }
        if (!hasKey(UNLOCK_DEVICE)){
            putBoolean(UNLOCK_DEVICE, false);
        }
        if (!hasKey(CALENDAR_FEATURE_TASKS)){
            putBoolean(CALENDAR_FEATURE_TASKS, false);
        }
        if (!hasKey(MISSED_CALL_REMINDER)){
            putBoolean(MISSED_CALL_REMINDER, false);
        }
        if (!hasKey(QUICK_SMS)){
            putBoolean(QUICK_SMS, false);
        }
        if (!hasKey(FOLLOW_REMINDER)){
            putBoolean(FOLLOW_REMINDER, false);
        }
        if (!hasKey(BIRTHDAY_PERMANENT)){
            putBoolean(BIRTHDAY_PERMANENT, false);
        }
        if (!hasKey(REMINDER_CHANGED)){
            putBoolean(REMINDER_CHANGED, false);
        }
        if (!hasKey(SYSTEM_VOLUME)){
            putBoolean(SYSTEM_VOLUME, false);
        }
        if (!hasKey(INCREASING_VOLUME)){
            putBoolean(INCREASING_VOLUME, false);
        }
        if (!hasKey(MAIN_IMAGE_ID)){
            putInt(MAIN_IMAGE_ID, -1);
        }
        if (!hasKey(MAIN_IMAGE_PATH)){
            putString(MAIN_IMAGE_PATH, MainImageActivity.DEFAULT_PHOTO);
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
            if (!hasKey(BIRTHDAY_SOUND_STATUS)) {
                putBoolean(BIRTHDAY_SOUND_STATUS, false);
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
}
