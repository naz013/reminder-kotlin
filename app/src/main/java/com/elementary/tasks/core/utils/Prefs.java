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

    public void setMarkerStyle(int markerStyle) {
        putInt(MARKER_STYLE, markerStyle);
    }

    public boolean hasScreenOrientation() {
        return hasKey(SCREEN);
    }

    public int getScreenOrientation() {
        return getInt(SCREEN);
    }

    public void setScreenOrientation(int orientation) {
        putInt(SCREEN, orientation);
    }

    public int getTodayColor() {
        return getInt(TODAY_COLOR);
    }

    public void setTodayColor(int color) {
        putInt(TODAY_COLOR, color);
    }

    public int getReminderColor() {
        return getInt(REMINDER_COLOR);
    }

    public void setReminderColor(int color) {
        putInt(REMINDER_COLOR, color);
    }

    public int getBirthdayColor() {
        return getInt(BIRTH_COLOR);
    }

    public void setBirthdayColor(int color) {
        putInt(BIRTH_COLOR, color);
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

    public void setAppTheme(int theme) {
        putInt(APP_THEME, theme);
    }

    public String getNightTime() {
        return getString(TIME_NIGHT);
    }

    public void setNightTime(String time) {
        putString(TIME_NIGHT, time);
    }

    public String getEveningTime() {
        return getString(TIME_EVENING);
    }

    public void setEveningTime(String time) {
        putString(TIME_EVENING, time);
    }

    public String getNoonTime() {
        return getString(TIME_DAY);
    }

    public void setNoonTime(String time) {
        putString(TIME_DAY, time);
    }

    public String getMorningTime() {
        return getString(TIME_MORNING);
    }

    public void setMorningTime(String time) {
        putString(TIME_MORNING, time);
    }

    public int getVoiceLocale() {
        return getInt(VOICE_LOCALE);
    }

    public void setVoiceLocale(int locale) {
        putInt(VOICE_LOCALE, locale);
    }

    public String getTtsLocale() {
        return getString(TTS_LOCALE);
    }

    public void setTtsLocale(String locale) {
        putString(TTS_LOCALE, locale);
    }

    public String getBirthdayTtsLocale() {
        return getString(BIRTHDAY_TTS_LOCALE);
    }

    public void setBirthdayTtsLocale(String locale) {
        putString(BIRTHDAY_TTS_LOCALE, locale);
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
}
