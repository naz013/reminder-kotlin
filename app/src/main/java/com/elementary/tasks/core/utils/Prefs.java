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

    public void initPrefs(Context context) {
        File settingsUI = new File("/data/data/" + context.getPackageName() + "/shared_prefs/" + PREFERENCES_NAME + ".xml");
        if (!settingsUI.exists()) {
            SharedPreferences appUISettings = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor uiEd = appUISettings.edit();
            uiEd.putInt(Prefs.APP_THEME, 8);
            uiEd.putInt(Prefs.TODAY_COLOR, 0);
            uiEd.putInt(Prefs.BIRTH_COLOR, 2);
            uiEd.putInt(Prefs.REMINDER_COLOR, 4);
            uiEd.putInt(Prefs.MAP_TYPE, Constants.MAP_NORMAL);
            uiEd.putString(Prefs.DRIVE_USER, DRIVE_USER_NONE);
            uiEd.putString(Prefs.REMINDER_IMAGE, Constants.DEFAULT);
            uiEd.putInt(Prefs.LED_COLOR, LED.BLUE);
            uiEd.putInt(Prefs.BIRTHDAY_LED_COLOR, LED.BLUE);
            uiEd.putInt(Prefs.LOCATION_RADIUS, 25);
            uiEd.putInt(Prefs.MARKER_STYLE, 5);
            uiEd.putInt(Prefs.TRACK_DISTANCE, 1);
            uiEd.putInt(Prefs.TRACK_TIME, 1);
            uiEd.putInt(Prefs.QUICK_NOTE_REMINDER_TIME, 10);
            uiEd.putInt(Prefs.TEXT_SIZE, 4);
            uiEd.putInt(Prefs.VOLUME, 25);
            uiEd.putInt(Prefs.LAST_CALENDAR_VIEW, 1);
            String localeCheck = Locale.getDefault().toString().toLowerCase();
            int locale;
            if (localeCheck.startsWith("uk")) {
                locale = 2;
            } else if (localeCheck.startsWith("ru")) {
                locale = 1;
            } else locale = 0;
            uiEd.putInt(Prefs.VOICE_LOCALE, locale);
            uiEd.putString(Prefs.TIME_MORNING, "7:0");
            uiEd.putString(Prefs.TIME_DAY, "12:0");
            uiEd.putString(Prefs.TIME_EVENING, "19:0");
            uiEd.putString(Prefs.TIME_NIGHT, "23:0");
            uiEd.putString(Prefs.TTS_LOCALE, Language.ENGLISH);
            uiEd.putString(Prefs.MAIN_IMAGE_PATH, MainImageActivity.DEFAULT_PHOTO);
            uiEd.putString(Prefs.CALENDAR_IMAGES, new Gson().toJson(new MonthImage()));
            uiEd.putInt(Prefs.START_DAY, 1);
            uiEd.putInt(Prefs.DAYS_TO_BIRTHDAY, 0);
            uiEd.putInt(Prefs.NOTIFICATION_REPEAT_INTERVAL, 15);
            uiEd.putInt(Prefs.APP_RUNS_COUNT, 0);
            uiEd.putInt(Prefs.DELAY_TIME, 5);
            uiEd.putInt(Prefs.EVENT_DURATION, 30);
            uiEd.putInt(Prefs.MISSED_CALL_TIME, 10);
            uiEd.putInt(Prefs.AUTO_BACKUP_INTERVAL, 6);
            uiEd.putInt(Prefs.AUTO_CHECK_FOR_EVENTS_INTERVAL, 6);
            uiEd.putInt(Prefs.SOUND_STREAM, 5);
            uiEd.putInt(Prefs.MAIN_IMAGE_ID, -1);
            uiEd.putBoolean(Prefs.TRACKING_NOTIFICATION, true);
            uiEd.putBoolean(Prefs.RATE_SHOW, false);
            uiEd.putBoolean(Prefs.IS_CREATE_SHOWN, false);
            uiEd.putBoolean(Prefs.IS_CALENDAR_SHOWN, false);
            uiEd.putBoolean(Prefs.IS_LIST_SHOWN, false);
            uiEd.putBoolean(Prefs.CONTACT_BIRTHDAYS, false);
            uiEd.putBoolean(Prefs.BIRTHDAY_REMINDER, true);
            uiEd.putBoolean(Prefs.CALENDAR_IMAGE, false);
            uiEd.putBoolean(Prefs.USE_DARK_THEME, false);
            uiEd.putBoolean(Prefs.EXPORT_TO_CALENDAR, false);
            uiEd.putBoolean(Prefs.AUTO_CHECK_BIRTHDAYS, false);
            uiEd.putBoolean(Prefs.INFINITE_VIBRATION, false);
            uiEd.putBoolean(Prefs.NOTIFICATION_REPEAT, false);
            uiEd.putBoolean(Prefs.WIDGET_BIRTHDAYS, false);
            uiEd.putBoolean(Prefs.QUICK_NOTE_REMINDER, false);
            uiEd.putBoolean(Prefs.NOTE_ENCRYPT, true);
            uiEd.putBoolean(Prefs.SYNC_NOTES, true);
            uiEd.putBoolean(Prefs.EXPORT_TO_STOCK, false);
            uiEd.putBoolean(Prefs.REMINDERS_IN_CALENDAR, true);
            uiEd.putBoolean(Prefs.IS_24_TIME_FORMAT, true);
            uiEd.putBoolean(Prefs.UNLOCK_DEVICE, false);
            uiEd.putBoolean(Prefs.CALENDAR_FEATURE_TASKS, true);
            uiEd.putBoolean(Prefs.MISSED_CALL_REMINDER, false);
            uiEd.putBoolean(Prefs.QUICK_SMS, false);
            uiEd.putBoolean(Prefs.FOLLOW_REMINDER, false);
            uiEd.putBoolean(Prefs.TTS, false);
            uiEd.putBoolean(Prefs.ITEM_PREVIEW, true);
            uiEd.putBoolean(Prefs.SYNC_BIRTHDAYS, true);
            uiEd.putBoolean(Prefs.BIRTHDAY_PERMANENT, false);
            uiEd.putBoolean(Prefs.REMINDER_CHANGED, false);
            uiEd.putBoolean(Prefs.REMINDER_IMAGE_BLUR, false);
            uiEd.putBoolean(Prefs.SYSTEM_VOLUME, false);
            uiEd.putBoolean(Prefs.INCREASING_VOLUME, false);
            uiEd.putBoolean(Prefs.DAY_NIGHT, false);
            if (Module.isPro()) {
                uiEd.putBoolean(Prefs.BIRTHDAY_LED_STATUS, false);
                uiEd.putBoolean(Prefs.LED_STATUS, true);
                uiEd.putInt(Prefs.BIRTHDAY_LED_COLOR, 6);
                uiEd.putInt(Prefs.LED_COLOR, 11);
                uiEd.putBoolean(Prefs.BIRTHDAY_USE_GLOBAL, true);
                uiEd.putBoolean(Prefs.BIRTHDAY_INFINITE_VIBRATION, false);
                uiEd.putBoolean(Prefs.BIRTHDAY_VIBRATION_STATUS, false);
                uiEd.putBoolean(Prefs.BIRTHDAY_WAKE_STATUS, false);
            }
            uiEd.apply();
        }
    }

    public void checkPrefs(){
        if (!hasKey(Prefs.TODAY_COLOR)){
            putInt(Prefs.TODAY_COLOR, 4);
        }
        if (!hasKey(Prefs.BIRTH_COLOR)){
            putInt(Prefs.BIRTH_COLOR, 1);
        }
        if (!hasKey(Prefs.REMINDER_COLOR)){
            putInt(Prefs.REMINDER_COLOR, 6);
        }
        if (!hasKey(Prefs.APP_THEME)){
            putInt(Prefs.APP_THEME, 8);
        }
        if (!hasKey(Prefs.DRIVE_USER)){
            putString(Prefs.DRIVE_USER, DRIVE_USER_NONE);
        }
        if (!hasKey(Prefs.TTS_LOCALE)){
            putString(Prefs.TTS_LOCALE, Language.ENGLISH);
        }
        if (!hasKey(Prefs.REMINDER_IMAGE)){
            putString(Prefs.REMINDER_IMAGE, Constants.DEFAULT);
        }

        if (!hasKey(Prefs.VOICE_LOCALE)){
            putInt(Prefs.VOICE_LOCALE, 0);
        }
        if (!hasKey(Prefs.TIME_MORNING)){
            putString(Prefs.TIME_MORNING, "7:0");
        }
        if (!hasKey(Prefs.TIME_DAY)){
            putString(Prefs.TIME_DAY, "12:0");
        }
        if (!hasKey(Prefs.TIME_EVENING)){
            putString(Prefs.TIME_EVENING, "19:0");
        }
        if (!hasKey(Prefs.TIME_NIGHT)){
            putString(Prefs.TIME_NIGHT, "23:0");
        }
        if (!hasKey(Prefs.DAYS_TO_BIRTHDAY)){
            putInt(Prefs.DAYS_TO_BIRTHDAY, 0);
        }
        if (!hasKey(Prefs.QUICK_NOTE_REMINDER_TIME)){
            putInt(Prefs.QUICK_NOTE_REMINDER_TIME, 10);
        }
        if (!hasKey(Prefs.TEXT_SIZE)){
            putInt(Prefs.TEXT_SIZE, 4);
        }
        if (!hasKey(Prefs.START_DAY)){
            putInt(Prefs.START_DAY, 1);
        }
        if (!hasKey(Prefs.BIRTHDAY_REMINDER_HOUR)){
            putInt(Prefs.BIRTHDAY_REMINDER_HOUR, 12);
        }
        if (!hasKey(Prefs.BIRTHDAY_REMINDER_MINUTE)){
            putInt(Prefs.BIRTHDAY_REMINDER_MINUTE, 0);
        }
        if (!hasKey(Prefs.TRACK_DISTANCE)){
            putInt(Prefs.TRACK_DISTANCE, 1);
        }
        if (!hasKey(Prefs.AUTO_BACKUP_INTERVAL)){
            putInt(Prefs.AUTO_BACKUP_INTERVAL, 6);
        }
        if (!hasKey(Prefs.AUTO_CHECK_FOR_EVENTS_INTERVAL)){
            putInt(Prefs.AUTO_CHECK_FOR_EVENTS_INTERVAL, 6);
        }
        if (!hasKey(Prefs.TRACK_TIME)){
            putInt(Prefs.TRACK_TIME, 1);
        }
        if (!hasKey(Prefs.APP_RUNS_COUNT)){
            putInt(Prefs.APP_RUNS_COUNT, 0);
        }
        if (!hasKey(Prefs.LAST_CALENDAR_VIEW)){
            putInt(Prefs.LAST_CALENDAR_VIEW, 1);
        }
        if (!hasKey(Prefs.DELAY_TIME)){
            putInt(Prefs.DELAY_TIME, 5);
        }
        if (!hasKey(Prefs.EVENT_DURATION)){
            putInt(Prefs.EVENT_DURATION, 30);
        }
        if (!hasKey(Prefs.NOTIFICATION_REPEAT_INTERVAL)){
            putInt(Prefs.NOTIFICATION_REPEAT_INTERVAL, 15);
        }
        if (!hasKey(Prefs.VOLUME)){
            putInt(Prefs.VOLUME, 25);
        }
        if (!hasKey(Prefs.MAP_TYPE)){
            putInt(Prefs.MAP_TYPE, Constants.MAP_NORMAL);
        }
        if (!hasKey(Prefs.MISSED_CALL_TIME)){
            putInt(Prefs.MISSED_CALL_TIME, 10);
        }
        if (!hasKey(Prefs.SOUND_STREAM)){
            putInt(Prefs.SOUND_STREAM, 5);
        }

        if (!hasKey(Prefs.DAY_NIGHT)){
            putBoolean(Prefs.DAY_NIGHT, false);
        }
        if (!hasKey(Prefs.RATE_SHOW)){
            putBoolean(Prefs.RATE_SHOW, false);
        }
        if (!hasKey(Prefs.REMINDER_IMAGE_BLUR)){
            putBoolean(Prefs.REMINDER_IMAGE_BLUR, false);
        }
        if (!hasKey(Prefs.QUICK_NOTE_REMINDER)){
            putBoolean(Prefs.QUICK_NOTE_REMINDER, false);
        }
        if (!hasKey(Prefs.SYNC_NOTES)){
            putBoolean(Prefs.SYNC_NOTES, true);
        }
        if (!hasKey(Prefs.REMINDERS_IN_CALENDAR)){
            putBoolean(Prefs.REMINDERS_IN_CALENDAR, false);
        }
        if (!hasKey(Prefs.TTS)){
            putBoolean(Prefs.TTS, false);
        }
        if (!hasKey(Prefs.SYNC_BIRTHDAYS)){
            putBoolean(Prefs.SYNC_BIRTHDAYS, true);
        }
        if (!hasKey(Prefs.NOTE_ENCRYPT)){
            putBoolean(Prefs.NOTE_ENCRYPT, true);
        }
        if (!hasKey(Prefs.CONTACTS_IMPORT_DIALOG)){
            putBoolean(Prefs.CONTACTS_IMPORT_DIALOG, false);
        }
        if (!hasKey(Prefs.CONTACT_BIRTHDAYS)){
            putBoolean(Prefs.CONTACT_BIRTHDAYS, false);
        }
        if (!hasKey(Prefs.BIRTHDAY_REMINDER)){
            putBoolean(Prefs.BIRTHDAY_REMINDER, true);
        }
        if (!hasKey(Prefs.CALENDAR_IMAGE)){
            putBoolean(Prefs.CALENDAR_IMAGE, false);
        }
        if (!hasKey(Prefs.SILENT_SMS)){
            putBoolean(Prefs.SILENT_SMS, false);
        }
        if (!hasKey(Prefs.ITEM_PREVIEW)){
            putBoolean(Prefs.ITEM_PREVIEW, true);
        }
        if (!hasKey(Prefs.WIDGET_BIRTHDAYS)){
            putBoolean(Prefs.WIDGET_BIRTHDAYS, false);
        }
        if (!hasKey(Prefs.WEAR_NOTIFICATION)){
            putBoolean(Prefs.WEAR_NOTIFICATION, false);
        }
        if (!hasKey(Prefs.EXPORT_TO_STOCK)){
            putBoolean(Prefs.EXPORT_TO_STOCK, false);
        }
        if (!hasKey(Prefs.USE_DARK_THEME)){
            putBoolean(Prefs.USE_DARK_THEME, false);
        }
        if (!hasKey(Prefs.EXPORT_TO_CALENDAR)){
            putBoolean(Prefs.EXPORT_TO_CALENDAR, false);
        }
        if (!hasKey(Prefs.AUTO_CHECK_BIRTHDAYS)){
            putBoolean(Prefs.AUTO_CHECK_BIRTHDAYS, false);
        }
        if (!hasKey(Prefs.INFINITE_VIBRATION)){
            putBoolean(Prefs.INFINITE_VIBRATION, false);
        }
        if (!hasKey(Prefs.AUTO_BACKUP)){
            putBoolean(Prefs.AUTO_BACKUP, false);
        }
        if (!hasKey(Prefs.SMART_FOLD)){
            putBoolean(Prefs.SMART_FOLD, false);
        }
        if (!hasKey(Prefs.NOTIFICATION_REPEAT)){
            putBoolean(Prefs.NOTIFICATION_REPEAT, false);
        }
        if (!hasKey(Prefs.IS_24_TIME_FORMAT)){
            putBoolean(Prefs.IS_24_TIME_FORMAT, true);
        }
        if (!hasKey(Prefs.UNLOCK_DEVICE)){
            putBoolean(Prefs.UNLOCK_DEVICE, false);
        }
        if (!hasKey(Prefs.CALENDAR_FEATURE_TASKS)){
            putBoolean(Prefs.CALENDAR_FEATURE_TASKS, false);
        }
        if (!hasKey(Prefs.MISSED_CALL_REMINDER)){
            putBoolean(Prefs.MISSED_CALL_REMINDER, false);
        }
        if (!hasKey(Prefs.QUICK_SMS)){
            putBoolean(Prefs.QUICK_SMS, false);
        }
        if (!hasKey(Prefs.FOLLOW_REMINDER)){
            putBoolean(Prefs.FOLLOW_REMINDER, false);
        }
        if (!hasKey(Prefs.BIRTHDAY_PERMANENT)){
            putBoolean(Prefs.BIRTHDAY_PERMANENT, false);
        }
        if (!hasKey(Prefs.REMINDER_CHANGED)){
            putBoolean(Prefs.REMINDER_CHANGED, false);
        }
        if (!hasKey(Prefs.SYSTEM_VOLUME)){
            putBoolean(Prefs.SYSTEM_VOLUME, false);
        }
        if (!hasKey(Prefs.INCREASING_VOLUME)){
            putBoolean(Prefs.INCREASING_VOLUME, false);
        }
        if (!hasKey(Prefs.MAIN_IMAGE_ID)){
            putInt(Prefs.MAIN_IMAGE_ID, -1);
        }
        if (!hasKey(Prefs.MAIN_IMAGE_PATH)){
            putString(Prefs.MAIN_IMAGE_PATH, MainImageActivity.DEFAULT_PHOTO);
        }
        if (!hasKey(Prefs.CALENDAR_IMAGES)) {
            putObject(Prefs.CALENDAR_IMAGES, new MonthImage());
        }
        if (Module.isPro()) {
            if (!hasKey(Prefs.LED_STATUS)) {
                putBoolean(Prefs.LED_STATUS, true);
            }
            if (!hasKey(Prefs.LED_COLOR)) {
                putInt(Prefs.LED_COLOR, 11);
            }
            if (!hasKey(Prefs.BIRTHDAY_LED_STATUS)) {
                putBoolean(Prefs.BIRTHDAY_LED_STATUS, false);
            }
            if (!hasKey(Prefs.BIRTHDAY_LED_COLOR)) {
                putInt(Prefs.BIRTHDAY_LED_COLOR, 6);
            }
            if (!hasKey(Prefs.BIRTHDAY_VIBRATION_STATUS)) {
                putBoolean(Prefs.BIRTHDAY_VIBRATION_STATUS, false);
            }
            if (!hasKey(Prefs.BIRTHDAY_SOUND_STATUS)) {
                putBoolean(Prefs.BIRTHDAY_SOUND_STATUS, false);
            }
            if (!hasKey(Prefs.BIRTHDAY_WAKE_STATUS)) {
                putBoolean(Prefs.BIRTHDAY_WAKE_STATUS, false);
            }
            if (!hasKey(Prefs.BIRTHDAY_INFINITE_SOUND)) {
                putBoolean(Prefs.BIRTHDAY_INFINITE_SOUND, false);
            }
            if (!hasKey(Prefs.BIRTHDAY_INFINITE_VIBRATION)) {
                putBoolean(Prefs.BIRTHDAY_INFINITE_VIBRATION, false);
            }
            if (!hasKey(Prefs.BIRTHDAY_USE_GLOBAL)) {
                putBoolean(Prefs.BIRTHDAY_USE_GLOBAL, true);
            }
        } else {
            putInt(Prefs.MARKER_STYLE, 5);
        }
    }
}
