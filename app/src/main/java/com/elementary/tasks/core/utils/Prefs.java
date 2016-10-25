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

import com.elementary.tasks.navigation.settings.images.MonthImage;

public class Prefs extends SharedPrefs {

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
}
