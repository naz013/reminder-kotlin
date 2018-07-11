package com.elementary.tasks.core.utils

import android.content.Context
import com.elementary.tasks.navigation.settings.images.MainImageActivity
import com.elementary.tasks.navigation.settings.images.MonthImage
import com.google.gson.Gson
import java.io.File
import java.util.*

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

class Prefs private constructor(context: Context) : SharedPrefs(context) {

    var appLanguage: Int
        get() = getInt(PrefsConstants.APP_LANGUAGE)
        set(value) = putInt(PrefsConstants.APP_LANGUAGE, value)

    var isTellAboutEvent: Boolean
        get() = getBoolean(PrefsConstants.TELL_ABOUT_EVENT)
        set(value) = putBoolean(PrefsConstants.TELL_ABOUT_EVENT, value)

    var isMigrated: Boolean
        get() = getBoolean(PrefsConstants.MIGRATION_COMPLETE)
        set(value) = putBoolean(PrefsConstants.MIGRATION_COMPLETE, value)

    var lastUsedReminder: Int
        get() = getInt(PrefsConstants.LAST_USED_REMINDER)
        set(value) = putInt(PrefsConstants.LAST_USED_REMINDER, value)

    var markerStyle: Int
        get() = getInt(PrefsConstants.MARKER_STYLE)
        set(value) = putInt(PrefsConstants.MARKER_STYLE, value)

    var screenOrientation: Int
        get() = getInt(PrefsConstants.SCREEN)
        set(value) = putInt(PrefsConstants.SCREEN, value)

    var todayColor: Int
        get() = getInt(PrefsConstants.TODAY_COLOR)
        set(value) = putInt(PrefsConstants.TODAY_COLOR, value)

    var reminderColor: Int
        get() = getInt(PrefsConstants.REMINDER_COLOR)
        set(value) = putInt(PrefsConstants.REMINDER_COLOR, value)

    var birthdayColor: Int
        get() = getInt(PrefsConstants.BIRTH_COLOR)
        set(value) = putInt(PrefsConstants.BIRTH_COLOR, value)

    var appTheme: Int
        get() = getInt(PrefsConstants.APP_THEME)
        set(value) = putInt(PrefsConstants.APP_THEME, value)

    var appThemeColor: Int
        get() = getInt(PrefsConstants.APP_THEME_COLOR)
        set(value) = putInt(PrefsConstants.APP_THEME_COLOR, value)

    var nightTime: String
        get() = getString(PrefsConstants.TIME_NIGHT)
        set(value) = putString(PrefsConstants.TIME_NIGHT, value)

    var eveningTime: String
        get() = getString(PrefsConstants.TIME_EVENING)
        set(value) = putString(PrefsConstants.TIME_EVENING, value)

    var noonTime: String
        get() = getString(PrefsConstants.TIME_DAY)
        set(value) = putString(PrefsConstants.TIME_DAY, value)

    var morningTime: String
        get() = getString(PrefsConstants.TIME_MORNING)
        set(value) = putString(PrefsConstants.TIME_MORNING, value)

    var voiceLocale: Int
        get() = getInt(PrefsConstants.VOICE_LOCALE)
        set(value) = putInt(PrefsConstants.VOICE_LOCALE, value)

    var ttsLocale: String
        get() = getString(PrefsConstants.TTS_LOCALE)
        set(value) = putString(PrefsConstants.TTS_LOCALE, value)

    var birthdayTtsLocale: String
        get() = getString(PrefsConstants.BIRTHDAY_TTS_LOCALE)
        set(value) = putString(PrefsConstants.BIRTHDAY_TTS_LOCALE, value)

    var isSystemLoudnessEnabled: Boolean
        get() = getBoolean(PrefsConstants.SYSTEM_VOLUME)
        set(value) = putBoolean(PrefsConstants.SYSTEM_VOLUME, value)

    var soundStream: Int
        get() = getInt(PrefsConstants.SOUND_STREAM)
        set(value) = putInt(PrefsConstants.SOUND_STREAM, value)

    var is24HourFormatEnabled: Boolean
        get() = getBoolean(PrefsConstants.IS_24_TIME_FORMAT)
        set(value) = putBoolean(PrefsConstants.IS_24_TIME_FORMAT, value)

    var isAutoSaveEnabled: Boolean
        get() = getBoolean(PrefsConstants.AUTO_SAVE)
        set(value) = putBoolean(PrefsConstants.AUTO_SAVE, value)

    var isCalendarEnabled: Boolean
        get() = getBoolean(PrefsConstants.EXPORT_TO_CALENDAR)
        set(value) = putBoolean(PrefsConstants.EXPORT_TO_CALENDAR, value)

    var isStockCalendarEnabled: Boolean
        get() = getBoolean(PrefsConstants.EXPORT_TO_STOCK)
        set(value) = putBoolean(PrefsConstants.EXPORT_TO_STOCK, value)

    var isFoldingEnabled: Boolean
        get() = getBoolean(PrefsConstants.SMART_FOLD)
        set(value) = putBoolean(PrefsConstants.SMART_FOLD, value)

    var isWearEnabled: Boolean
        get() = getBoolean(PrefsConstants.WEAR_NOTIFICATION)
        set(value) = putBoolean(PrefsConstants.WEAR_NOTIFICATION, value)

    var isUiChanged: Boolean
        get() = getBoolean(PrefsConstants.UI_CHANGED)
        set(value) = putBoolean(PrefsConstants.UI_CHANGED, value)

    var isSbNotificationEnabled: Boolean
        get() = getBoolean(PrefsConstants.STATUS_BAR_NOTIFICATION)
        set(value) = putBoolean(PrefsConstants.STATUS_BAR_NOTIFICATION, value)

    var imageId: Int
        get() = getInt(PrefsConstants.MAIN_IMAGE_ID)
        set(value) = putInt(PrefsConstants.MAIN_IMAGE_ID, value)

    var imagePath: String
        get() = getString(PrefsConstants.MAIN_IMAGE_PATH)
        set(value) = putString(PrefsConstants.MAIN_IMAGE_PATH, value)

    var calendarImages: MonthImage
        get() {
            return try {
                getObject(PrefsConstants.CALENDAR_IMAGES, MonthImage::class.java) as MonthImage
            } catch (e: ClassCastException) {
                MonthImage()
            }

        }
        set(value) = putObject(PrefsConstants.MAIN_IMAGE_PATH, value)

    var isCalendarImagesEnabled: Boolean
        get() = getBoolean(PrefsConstants.CALENDAR_IMAGE)
        set(value) = putBoolean(PrefsConstants.CALENDAR_IMAGE, value)

    var isNoteReminderEnabled: Boolean
        get() = getBoolean(PrefsConstants.QUICK_NOTE_REMINDER)
        set(value) = putBoolean(PrefsConstants.QUICK_NOTE_REMINDER, value)

    var noteReminderTime: Int
        get() = getInt(PrefsConstants.QUICK_NOTE_REMINDER_TIME)
        set(value) = putInt(PrefsConstants.QUICK_NOTE_REMINDER_TIME, value)

    var noteTextSize: Int
        get() = getInt(PrefsConstants.NOTE_TEXT_SIZE)
        set(value) = putInt(PrefsConstants.NOTE_TEXT_SIZE, value)

    var radius: Int
        get() = getInt(PrefsConstants.LOCATION_RADIUS)
        set(value) = putInt(PrefsConstants.LOCATION_RADIUS, value)

    var isDistanceNotificationEnabled: Boolean
        get() = getBoolean(PrefsConstants.TRACKING_NOTIFICATION)
        set(value) = putBoolean(PrefsConstants.TRACKING_NOTIFICATION, value)

    var mapType: Int
        get() = getInt(PrefsConstants.MAP_TYPE)
        set(value) = putInt(PrefsConstants.MAP_TYPE, value)

    var mapStyle: Int
        get() = getInt(PrefsConstants.MAP_STYLE)
        set(value) = putInt(PrefsConstants.MAP_STYLE, value)

    var trackDistance: Int
        get() = getInt(PrefsConstants.TRACK_DISTANCE)
        set(value) = putInt(PrefsConstants.TRACK_DISTANCE, value)

    var trackTime: Int
        get() = getInt(PrefsConstants.TRACK_TIME)
        set(value) = putInt(PrefsConstants.TRACK_TIME, value)

    var isMissedReminderEnabled: Boolean
        get() = getBoolean(PrefsConstants.MISSED_CALL_REMINDER)
        set(value) = putBoolean(PrefsConstants.MISSED_CALL_REMINDER, value)

    var missedReminderTime: Int
        get() = getInt(PrefsConstants.MISSED_CALL_TIME)
        set(value) = putInt(PrefsConstants.MISSED_CALL_TIME, value)

    var isQuickSmsEnabled: Boolean
        get() = getBoolean(PrefsConstants.QUICK_SMS)
        set(value) = putBoolean(PrefsConstants.QUICK_SMS, value)

    var isFollowReminderEnabled: Boolean
        get() = getBoolean(PrefsConstants.FOLLOW_REMINDER)
        set(value) = putBoolean(PrefsConstants.FOLLOW_REMINDER, value)

    var driveUser: String
        get() = SuperUtil.decrypt(getString(PrefsConstants.DRIVE_USER))
        set(value) = putString(PrefsConstants.DRIVE_USER, SuperUtil.encrypt(value))

    var reminderImage: String
        get() = getString(PrefsConstants.REMINDER_IMAGE)
        set(value) = putString(PrefsConstants.REMINDER_IMAGE, value)

    var isBlurEnabled: Boolean
        get() = getBoolean(PrefsConstants.REMINDER_IMAGE_BLUR)
        set(value) = putBoolean(PrefsConstants.REMINDER_IMAGE_BLUR, value)

    var isManualRemoveEnabled: Boolean
        get() = getBoolean(PrefsConstants.NOTIFICATION_REMOVE)
        set(value) = putBoolean(PrefsConstants.NOTIFICATION_REMOVE, value)

    var isSbIconEnabled: Boolean
        get() = getBoolean(PrefsConstants.STATUS_BAR_ICON)
        set(value) = putBoolean(PrefsConstants.STATUS_BAR_ICON, value)

    var isVibrateEnabled: Boolean
        get() = getBoolean(PrefsConstants.VIBRATION_STATUS)
        set(value) = putBoolean(PrefsConstants.VIBRATION_STATUS, value)

    var isInfiniteVibrateEnabled: Boolean
        get() = getBoolean(PrefsConstants.INFINITE_VIBRATION)
        set(value) = putBoolean(PrefsConstants.INFINITE_VIBRATION, value)

    var isSoundInSilentModeEnabled: Boolean
        get() = getBoolean(PrefsConstants.SILENT_SOUND)
        set(value) = putBoolean(PrefsConstants.SILENT_SOUND, value)

    var isInfiniteSoundEnabled: Boolean
        get() = getBoolean(PrefsConstants.INFINITE_SOUND)
        set(value) = putBoolean(PrefsConstants.INFINITE_SOUND, value)

    var melodyFile: String
        get() = getString(PrefsConstants.CUSTOM_SOUND)
        set(value) = putString(PrefsConstants.CUSTOM_SOUND, value)

    var loudness: Int
        get() = getInt(PrefsConstants.VOLUME)
        set(value) = putInt(PrefsConstants.VOLUME, value)

    var isIncreasingLoudnessEnabled: Boolean
        get() = getBoolean(PrefsConstants.INCREASING_VOLUME)
        set(value) = putBoolean(PrefsConstants.INCREASING_VOLUME, value)

    var isTtsEnabled: Boolean
        get() = getBoolean(PrefsConstants.TTS)
        set(value) = putBoolean(PrefsConstants.TTS, value)

    var isDeviceAwakeEnabled: Boolean
        get() = getBoolean(PrefsConstants.WAKE_STATUS)
        set(value) = putBoolean(PrefsConstants.WAKE_STATUS, value)

    var isDeviceUnlockEnabled: Boolean
        get() = getBoolean(PrefsConstants.UNLOCK_DEVICE)
        set(value) = putBoolean(PrefsConstants.UNLOCK_DEVICE, value)

    var isAutoSmsEnabled: Boolean
        get() = getBoolean(PrefsConstants.SILENT_SMS)
        set(value) = putBoolean(PrefsConstants.SILENT_SMS, value)

    var isAutoLaunchEnabled: Boolean
        get() = getBoolean(PrefsConstants.APPLICATION_AUTO_LAUNCH)
        set(value) = putBoolean(PrefsConstants.APPLICATION_AUTO_LAUNCH, value)

    var isAutoCallEnabled: Boolean
        get() = getBoolean(PrefsConstants.AUTO_CALL)
        set(value) = putBoolean(PrefsConstants.AUTO_CALL, value)

    var snoozeTime: Int
        get() = getInt(PrefsConstants.DELAY_TIME)
        set(value) = putInt(PrefsConstants.DELAY_TIME, value)

    var isNotificationRepeatEnabled: Boolean
        get() = getBoolean(PrefsConstants.NOTIFICATION_REPEAT)
        set(value) = putBoolean(PrefsConstants.NOTIFICATION_REPEAT, value)

    var notificationRepeatTime: Int
        get() = getInt(PrefsConstants.NOTIFICATION_REPEAT_INTERVAL)
        set(value) = putInt(PrefsConstants.NOTIFICATION_REPEAT_INTERVAL, value)

    var isLedEnabled: Boolean
        get() = getBoolean(PrefsConstants.LED_STATUS)
        set(value) = putBoolean(PrefsConstants.LED_STATUS, value)

    var ledColor: Int
        get() = getInt(PrefsConstants.LED_COLOR)
        set(value) = putInt(PrefsConstants.LED_COLOR, value)

    var isSettingsBackupEnabled: Boolean
        get() = getBoolean(PrefsConstants.EXPORT_SETTINGS)
        set(value) = putBoolean(PrefsConstants.EXPORT_SETTINGS, value)

    var isAutoBackupEnabled: Boolean
        get() = getBoolean(PrefsConstants.AUTO_BACKUP)
        set(value) = putBoolean(PrefsConstants.AUTO_BACKUP, value)

    var autoBackupInterval: Int
        get() = getInt(PrefsConstants.AUTO_BACKUP_INTERVAL)
        set(value) = putInt(PrefsConstants.AUTO_BACKUP_INTERVAL, value)

    var calendarEventDuration: Int
        get() = getInt(PrefsConstants.EVENT_DURATION)
        set(value) = putInt(PrefsConstants.EVENT_DURATION, value)

    var calendarId: Int
        get() = getInt(PrefsConstants.CALENDAR_ID)
        set(value) = putInt(PrefsConstants.CALENDAR_ID, value)

    var isFutureEventEnabled: Boolean
        get() = getBoolean(PrefsConstants.CALENDAR_FEATURE_TASKS)
        set(value) = putBoolean(PrefsConstants.CALENDAR_FEATURE_TASKS, value)

    var isRemindersInCalendarEnabled: Boolean
        get() = getBoolean(PrefsConstants.REMINDERS_IN_CALENDAR)
        set(value) = putBoolean(PrefsConstants.REMINDERS_IN_CALENDAR, value)

    var startDay: Int
        get() = getInt(PrefsConstants.START_DAY)
        set(value) = putInt(PrefsConstants.START_DAY, value)

    var isAutoEventsCheckEnabled: Boolean
        get() = getBoolean(PrefsConstants.AUTO_CHECK_FOR_EVENTS)
        set(value) = putBoolean(PrefsConstants.AUTO_CHECK_FOR_EVENTS, value)

    var autoCheckInterval: Int
        get() = getInt(PrefsConstants.AUTO_CHECK_FOR_EVENTS_INTERVAL)
        set(value) = putInt(PrefsConstants.AUTO_CHECK_FOR_EVENTS_INTERVAL, value)

    var eventsCalendar: Int
        get() = getInt(PrefsConstants.EVENTS_CALENDAR)
        set(value) = putInt(PrefsConstants.EVENTS_CALENDAR, value)

    var isBirthdayReminderEnabled: Boolean
        get() = getBoolean(PrefsConstants.BIRTHDAY_REMINDER)
        set(value) = putBoolean(PrefsConstants.BIRTHDAY_REMINDER, value)

    var birthdayTime: String
        get() = getString(PrefsConstants.BIRTHDAY_REMINDER_TIME)
        set(value) = putString(PrefsConstants.BIRTHDAY_REMINDER_TIME, value)

    var isBirthdayInWidgetEnabled: Boolean
        get() = getBoolean(PrefsConstants.WIDGET_BIRTHDAYS)
        set(value) = putBoolean(PrefsConstants.WIDGET_BIRTHDAYS, value)

    var isBirthdayPermanentEnabled: Boolean
        get() = getBoolean(PrefsConstants.BIRTHDAY_PERMANENT)
        set(value) = putBoolean(PrefsConstants.BIRTHDAY_PERMANENT, value)

    var daysToBirthday: Int
        get() = getInt(PrefsConstants.DAYS_TO_BIRTHDAY)
        set(value) = putInt(PrefsConstants.DAYS_TO_BIRTHDAY, value)

    var isContactBirthdaysEnabled: Boolean
        get() = getBoolean(PrefsConstants.CONTACT_BIRTHDAYS)
        set(value) = putBoolean(PrefsConstants.CONTACT_BIRTHDAYS, value)

    var isContactAutoCheckEnabled: Boolean
        get() = getBoolean(PrefsConstants.AUTO_CHECK_BIRTHDAYS)
        set(value) = putBoolean(PrefsConstants.AUTO_CHECK_BIRTHDAYS, value)

    var isBirthdayGlobalEnabled: Boolean
        get() = getBoolean(PrefsConstants.BIRTHDAY_USE_GLOBAL)
        set(value) = putBoolean(PrefsConstants.BIRTHDAY_USE_GLOBAL, value)

    var isBirthdayVibrationEnabled: Boolean
        get() = getBoolean(PrefsConstants.BIRTHDAY_VIBRATION_STATUS)
        set(value) = putBoolean(PrefsConstants.BIRTHDAY_VIBRATION_STATUS, value)

    var isBirthdayInfiniteVibrationEnabled: Boolean
        get() = getBoolean(PrefsConstants.BIRTHDAY_INFINITE_VIBRATION)
        set(value) = putBoolean(PrefsConstants.BIRTHDAY_INFINITE_VIBRATION, value)

    var isBirthdaySilentEnabled: Boolean
        get() = getBoolean(PrefsConstants.BIRTHDAY_SILENT_STATUS)
        set(value) = putBoolean(PrefsConstants.BIRTHDAY_SILENT_STATUS, value)

    var isBirthdayInfiniteSoundEnabled: Boolean
        get() = getBoolean(PrefsConstants.BIRTHDAY_INFINITE_SOUND)
        set(value) = putBoolean(PrefsConstants.BIRTHDAY_INFINITE_SOUND, value)

    var isBirthdayWakeEnabled: Boolean
        get() = getBoolean(PrefsConstants.BIRTHDAY_WAKE_STATUS)
        set(value) = putBoolean(PrefsConstants.BIRTHDAY_WAKE_STATUS, value)

    var isBirthdayLedEnabled: Boolean
        get() = getBoolean(PrefsConstants.BIRTHDAY_LED_STATUS)
        set(value) = putBoolean(PrefsConstants.BIRTHDAY_LED_STATUS, value)

    var isBirthdayTtsEnabled: Boolean
        get() = getBoolean(PrefsConstants.BIRTHDAY_TTS)
        set(value) = putBoolean(PrefsConstants.BIRTHDAY_TTS, value)

    var birthdayLedColor: Int
        get() = getInt(PrefsConstants.BIRTHDAY_LED_COLOR)
        set(value) = putInt(PrefsConstants.BIRTHDAY_LED_COLOR, value)

    var birthdayMelody: String
        get() = getString(PrefsConstants.BIRTHDAY_SOUND_FILE)
        set(value) = putString(PrefsConstants.BIRTHDAY_SOUND_FILE, value)

    var noteOrder: String
        get() = getString(PrefsConstants.NOTES_ORDER)
        set(value) = putString(PrefsConstants.NOTES_ORDER, value)

    var isNotesGridEnabled: Boolean
        get() = getBoolean(PrefsConstants.NOTES_LIST_STYLE)
        set(value) = putBoolean(PrefsConstants.NOTES_LIST_STYLE, value)

    var lastGoogleList: Int
        get() = getInt(PrefsConstants.LAST_LIST)
        set(value) = putInt(PrefsConstants.LAST_LIST, value)

    var tasksOrder: String
        get() = getString(PrefsConstants.TASKS_ORDER)
        set(value) = putString(PrefsConstants.TASKS_ORDER, value)

    var isBetaWarmingShowed: Boolean
        get() = getBoolean(PrefsConstants.BETA_KEY)
        set(value) = putBoolean(PrefsConstants.BETA_KEY, value)

    var rateCount: Int
        get() = getInt(PrefsConstants.RATE_COUNT)
        set(count) = putInt(PrefsConstants.RATE_COUNT, count)

    var isUserLogged: Boolean
        get() = getBoolean(PrefsConstants.USER_LOGGED)
        set(value) = putBoolean(PrefsConstants.USER_LOGGED, value)

    var isLiveEnabled: Boolean
        get() = getBoolean(PrefsConstants.LIVE_CONVERSATION)
        set(value) = putBoolean(PrefsConstants.LIVE_CONVERSATION, value)

    var isNoteColorRememberingEnabled: Boolean
        get() = getBoolean(PrefsConstants.REMEMBER_NOTE_COLOR)
        set(value) = putBoolean(PrefsConstants.REMEMBER_NOTE_COLOR, value)

    var lastNoteColor: Int
        get() = getInt(PrefsConstants.LAST_NOTE_COLOR)
        set(count) = putInt(PrefsConstants.LAST_NOTE_COLOR, count)

    var noteColorOpacity: Int
        get() = getInt(PrefsConstants.NOTE_COLOR_OPACITY)
        set(count) = putInt(PrefsConstants.NOTE_COLOR_OPACITY, count)

    var isNoteHintShowed: Boolean
        get() = getBoolean(PrefsConstants.NOTE_HINT_SHOWED)
        set(value) = putBoolean(PrefsConstants.NOTE_HINT_SHOWED, value)

    var reminderType: Int
        get() = getInt(PrefsConstants.REMINDER_TYPE)
        set(reminderType) = putInt(PrefsConstants.REMINDER_TYPE, reminderType)

    var dropboxUid: String
        get() = getString(PrefsConstants.DROPBOX_UID)
        set(uid) = putString(PrefsConstants.DROPBOX_UID, uid)

    var dropboxToken: String
        get() = getString(PrefsConstants.DROPBOX_TOKEN)
        set(token) = putString(PrefsConstants.DROPBOX_TOKEN, token)

    var isIgnoreWindowType: Boolean
        get() = getBoolean(PrefsConstants.IGNORE_WINDOW_TYPE)
        set(value) = putBoolean(PrefsConstants.IGNORE_WINDOW_TYPE, value)

    fun isShowcase(key: String): Boolean {
        return getBoolean(key)
    }

    fun setShowcase(key: String, value: Boolean) {
        putBoolean(key, value)
    }

    fun initPrefs(context: Context) {
        val settingsUI = File("/data/data/" + context.packageName + "/shared_prefs/" + PrefsConstants.PREFS_NAME + ".xml")
        if (!settingsUI.exists()) {
            val appUISettings = context.getSharedPreferences(PrefsConstants.PREFS_NAME, Context.MODE_PRIVATE)
            val uiEd = appUISettings.edit()
            uiEd.putInt(PrefsConstants.APP_THEME, ThemeUtil.THEME_AUTO)
            uiEd.putInt(PrefsConstants.APP_THEME_COLOR, 8)
            uiEd.putInt(PrefsConstants.TODAY_COLOR, 0)
            uiEd.putInt(PrefsConstants.BIRTH_COLOR, 2)
            uiEd.putInt(PrefsConstants.REMINDER_COLOR, 4)
            uiEd.putInt(PrefsConstants.MAP_TYPE, Constants.MAP_NORMAL)
            uiEd.putString(PrefsConstants.DRIVE_USER, DRIVE_USER_NONE)
            uiEd.putString(PrefsConstants.REMINDER_IMAGE, Constants.DEFAULT)
            uiEd.putInt(PrefsConstants.LED_COLOR, LED.BLUE)
            uiEd.putInt(PrefsConstants.BIRTHDAY_LED_COLOR, LED.BLUE)
            uiEd.putInt(PrefsConstants.LOCATION_RADIUS, 25)
            uiEd.putInt(PrefsConstants.MARKER_STYLE, 5)
            uiEd.putInt(PrefsConstants.TRACK_DISTANCE, 1)
            uiEd.putInt(PrefsConstants.TRACK_TIME, 1)
            uiEd.putInt(PrefsConstants.QUICK_NOTE_REMINDER_TIME, 10)
            uiEd.putInt(PrefsConstants.NOTE_TEXT_SIZE, 4)
            uiEd.putInt(PrefsConstants.VOLUME, 25)
            val localeCheck = Locale.getDefault().toString().toLowerCase()
            val locale: Int
            if (localeCheck.startsWith("uk")) {
                locale = 2
            } else if (localeCheck.startsWith("ru")) {
                locale = 1
            } else {
                locale = 0
            }
            uiEd.putInt(PrefsConstants.VOICE_LOCALE, locale)
            uiEd.putString(PrefsConstants.TIME_MORNING, "7:0")
            uiEd.putString(PrefsConstants.TIME_DAY, "12:0")
            uiEd.putString(PrefsConstants.TIME_EVENING, "19:0")
            uiEd.putString(PrefsConstants.TIME_NIGHT, "23:0")
            uiEd.putString(PrefsConstants.TTS_LOCALE, Language.ENGLISH)
            uiEd.putString(PrefsConstants.MAIN_IMAGE_PATH, MainImageActivity.DEFAULT_PHOTO)
            uiEd.putString(PrefsConstants.CALENDAR_IMAGES, Gson().toJson(MonthImage()))
            uiEd.putString(PrefsConstants.CUSTOM_SOUND, Constants.DEFAULT)
            uiEd.putInt(PrefsConstants.APP_LANGUAGE, 0)
            uiEd.putInt(PrefsConstants.START_DAY, 1)
            uiEd.putInt(PrefsConstants.DAYS_TO_BIRTHDAY, 0)
            uiEd.putInt(PrefsConstants.NOTIFICATION_REPEAT_INTERVAL, 15)
            uiEd.putInt(PrefsConstants.APP_RUNS_COUNT, 0)
            uiEd.putInt(PrefsConstants.DELAY_TIME, 5)
            uiEd.putInt(PrefsConstants.EVENT_DURATION, 30)
            uiEd.putInt(PrefsConstants.MISSED_CALL_TIME, 10)
            uiEd.putInt(PrefsConstants.AUTO_BACKUP_INTERVAL, 6)
            uiEd.putInt(PrefsConstants.AUTO_CHECK_FOR_EVENTS_INTERVAL, 6)
            uiEd.putInt(PrefsConstants.SOUND_STREAM, 5)
            uiEd.putInt(PrefsConstants.MAIN_IMAGE_ID, -1)
            uiEd.putInt(PrefsConstants.NOTE_COLOR_OPACITY, 100)
            uiEd.putInt(PrefsConstants.MAP_STYLE, 6)
            uiEd.putBoolean(PrefsConstants.TRACKING_NOTIFICATION, true)
            uiEd.putBoolean(PrefsConstants.RATE_SHOW, false)
            uiEd.putBoolean(PrefsConstants.IS_CREATE_SHOWN, false)
            uiEd.putBoolean(PrefsConstants.IS_CALENDAR_SHOWN, false)
            uiEd.putBoolean(PrefsConstants.IS_LIST_SHOWN, false)
            uiEd.putBoolean(PrefsConstants.CONTACT_BIRTHDAYS, false)
            uiEd.putBoolean(PrefsConstants.BIRTHDAY_REMINDER, false)
            uiEd.putBoolean(PrefsConstants.CALENDAR_IMAGE, false)
            uiEd.putBoolean(PrefsConstants.EXPORT_TO_CALENDAR, false)
            uiEd.putBoolean(PrefsConstants.AUTO_CHECK_BIRTHDAYS, false)
            uiEd.putBoolean(PrefsConstants.INFINITE_VIBRATION, false)
            uiEd.putBoolean(PrefsConstants.NOTIFICATION_REPEAT, false)
            uiEd.putBoolean(PrefsConstants.WIDGET_BIRTHDAYS, false)
            uiEd.putBoolean(PrefsConstants.QUICK_NOTE_REMINDER, false)
            uiEd.putBoolean(PrefsConstants.EXPORT_TO_STOCK, false)
            uiEd.putBoolean(PrefsConstants.REMINDERS_IN_CALENDAR, true)
            uiEd.putBoolean(PrefsConstants.IS_24_TIME_FORMAT, true)
            uiEd.putBoolean(PrefsConstants.UNLOCK_DEVICE, false)
            uiEd.putBoolean(PrefsConstants.WAKE_STATUS, false)
            uiEd.putBoolean(PrefsConstants.CALENDAR_FEATURE_TASKS, true)
            uiEd.putBoolean(PrefsConstants.MISSED_CALL_REMINDER, false)
            uiEd.putBoolean(PrefsConstants.QUICK_SMS, false)
            uiEd.putBoolean(PrefsConstants.FOLLOW_REMINDER, false)
            uiEd.putBoolean(PrefsConstants.TTS, false)
            uiEd.putBoolean(PrefsConstants.BIRTHDAY_PERMANENT, false)
            uiEd.putBoolean(PrefsConstants.REMINDER_CHANGED, false)
            uiEd.putBoolean(PrefsConstants.REMINDER_IMAGE_BLUR, false)
            uiEd.putBoolean(PrefsConstants.SYSTEM_VOLUME, false)
            uiEd.putBoolean(PrefsConstants.INCREASING_VOLUME, false)
            uiEd.putBoolean(PrefsConstants.GCM_ENABLED, true)
            uiEd.putBoolean(PrefsConstants.LIVE_CONVERSATION, true)
            uiEd.putBoolean(PrefsConstants.IGNORE_WINDOW_TYPE, true)
            if (Module.isPro) {
                uiEd.putBoolean(PrefsConstants.BIRTHDAY_LED_STATUS, false)
                uiEd.putBoolean(PrefsConstants.LED_STATUS, true)
                uiEd.putInt(PrefsConstants.BIRTHDAY_LED_COLOR, 6)
                uiEd.putInt(PrefsConstants.LED_COLOR, 11)
                uiEd.putBoolean(PrefsConstants.BIRTHDAY_USE_GLOBAL, true)
                uiEd.putBoolean(PrefsConstants.BIRTHDAY_INFINITE_VIBRATION, false)
                uiEd.putBoolean(PrefsConstants.BIRTHDAY_VIBRATION_STATUS, false)
                uiEd.putBoolean(PrefsConstants.BIRTHDAY_WAKE_STATUS, false)
            }
            uiEd.apply()
        }
    }

    fun checkPrefs() {
        if (!hasKey(PrefsConstants.TODAY_COLOR)) {
            putInt(PrefsConstants.TODAY_COLOR, 4)
        }
        if (!hasKey(PrefsConstants.BIRTH_COLOR)) {
            putInt(PrefsConstants.BIRTH_COLOR, 1)
        }
        if (!hasKey(PrefsConstants.REMINDER_COLOR)) {
            putInt(PrefsConstants.REMINDER_COLOR, 6)
        }
        if (!hasKey(PrefsConstants.APP_THEME)) {
            putInt(PrefsConstants.APP_THEME, ThemeUtil.THEME_AUTO)
        }
        if (!hasKey(PrefsConstants.APP_THEME_COLOR)) {
            putInt(PrefsConstants.APP_THEME_COLOR, 8)
        }
        if (!hasKey(PrefsConstants.DRIVE_USER)) {
            putString(PrefsConstants.DRIVE_USER, DRIVE_USER_NONE)
        }
        if (!hasKey(PrefsConstants.TTS_LOCALE)) {
            putString(PrefsConstants.TTS_LOCALE, Language.ENGLISH)
        }
        if (!hasKey(PrefsConstants.REMINDER_IMAGE)) {
            putString(PrefsConstants.REMINDER_IMAGE, Constants.DEFAULT)
        }

        if (!hasKey(PrefsConstants.VOICE_LOCALE)) {
            putInt(PrefsConstants.VOICE_LOCALE, 0)
        }
        if (!hasKey(PrefsConstants.TIME_MORNING)) {
            putString(PrefsConstants.TIME_MORNING, "7:0")
        }
        if (!hasKey(PrefsConstants.TIME_DAY)) {
            putString(PrefsConstants.TIME_DAY, "12:0")
        }
        if (!hasKey(PrefsConstants.TIME_EVENING)) {
            putString(PrefsConstants.TIME_EVENING, "19:0")
        }
        if (!hasKey(PrefsConstants.TIME_NIGHT)) {
            putString(PrefsConstants.TIME_NIGHT, "23:0")
        }
        if (!hasKey(PrefsConstants.DAYS_TO_BIRTHDAY)) {
            putInt(PrefsConstants.DAYS_TO_BIRTHDAY, 0)
        }
        if (!hasKey(PrefsConstants.QUICK_NOTE_REMINDER_TIME)) {
            putInt(PrefsConstants.QUICK_NOTE_REMINDER_TIME, 10)
        }
        if (!hasKey(PrefsConstants.NOTE_TEXT_SIZE)) {
            putInt(PrefsConstants.NOTE_TEXT_SIZE, 4)
        }
        if (!hasKey(PrefsConstants.START_DAY)) {
            putInt(PrefsConstants.START_DAY, 1)
        }
        if (!hasKey(PrefsConstants.BIRTHDAY_REMINDER_TIME)) {
            putString(PrefsConstants.BIRTHDAY_REMINDER_TIME, "12:00")
        }
        if (!hasKey(PrefsConstants.TRACK_DISTANCE)) {
            putInt(PrefsConstants.TRACK_DISTANCE, 1)
        }
        if (!hasKey(PrefsConstants.AUTO_BACKUP_INTERVAL)) {
            putInt(PrefsConstants.AUTO_BACKUP_INTERVAL, 6)
        }
        if (!hasKey(PrefsConstants.AUTO_CHECK_FOR_EVENTS_INTERVAL)) {
            putInt(PrefsConstants.AUTO_CHECK_FOR_EVENTS_INTERVAL, 6)
        }
        if (!hasKey(PrefsConstants.TRACK_TIME)) {
            putInt(PrefsConstants.TRACK_TIME, 1)
        }
        if (!hasKey(PrefsConstants.APP_RUNS_COUNT)) {
            putInt(PrefsConstants.APP_RUNS_COUNT, 0)
        }
        if (!hasKey(PrefsConstants.DELAY_TIME)) {
            putInt(PrefsConstants.DELAY_TIME, 5)
        }
        if (!hasKey(PrefsConstants.EVENT_DURATION)) {
            putInt(PrefsConstants.EVENT_DURATION, 30)
        }
        if (!hasKey(PrefsConstants.NOTIFICATION_REPEAT_INTERVAL)) {
            putInt(PrefsConstants.NOTIFICATION_REPEAT_INTERVAL, 15)
        }
        if (!hasKey(PrefsConstants.VOLUME)) {
            putInt(PrefsConstants.VOLUME, 25)
        }
        if (!hasKey(PrefsConstants.MAP_TYPE)) {
            putInt(PrefsConstants.MAP_TYPE, Constants.MAP_NORMAL)
        }
        if (!hasKey(PrefsConstants.MISSED_CALL_TIME)) {
            putInt(PrefsConstants.MISSED_CALL_TIME, 10)
        }
        if (!hasKey(PrefsConstants.SOUND_STREAM)) {
            putInt(PrefsConstants.SOUND_STREAM, 5)
        }
        if (!hasKey(PrefsConstants.RATE_SHOW)) {
            putBoolean(PrefsConstants.RATE_SHOW, false)
        }
        if (!hasKey(PrefsConstants.REMINDER_IMAGE_BLUR)) {
            putBoolean(PrefsConstants.REMINDER_IMAGE_BLUR, false)
        }
        if (!hasKey(PrefsConstants.QUICK_NOTE_REMINDER)) {
            putBoolean(PrefsConstants.QUICK_NOTE_REMINDER, false)
        }
        if (!hasKey(PrefsConstants.REMINDERS_IN_CALENDAR)) {
            putBoolean(PrefsConstants.REMINDERS_IN_CALENDAR, false)
        }
        if (!hasKey(PrefsConstants.TTS)) {
            putBoolean(PrefsConstants.TTS, false)
        }
        if (!hasKey(PrefsConstants.CONTACT_BIRTHDAYS)) {
            putBoolean(PrefsConstants.CONTACT_BIRTHDAYS, false)
        }
        if (!hasKey(PrefsConstants.BIRTHDAY_REMINDER)) {
            putBoolean(PrefsConstants.BIRTHDAY_REMINDER, true)
        }
        if (!hasKey(PrefsConstants.CALENDAR_IMAGE)) {
            putBoolean(PrefsConstants.CALENDAR_IMAGE, false)
        }
        if (!hasKey(PrefsConstants.SILENT_SMS)) {
            putBoolean(PrefsConstants.SILENT_SMS, false)
        }
        if (!hasKey(PrefsConstants.ITEM_PREVIEW)) {
            putBoolean(PrefsConstants.ITEM_PREVIEW, true)
        }
        if (!hasKey(PrefsConstants.WIDGET_BIRTHDAYS)) {
            putBoolean(PrefsConstants.WIDGET_BIRTHDAYS, false)
        }
        if (!hasKey(PrefsConstants.WEAR_NOTIFICATION)) {
            putBoolean(PrefsConstants.WEAR_NOTIFICATION, false)
        }
        if (!hasKey(PrefsConstants.EXPORT_TO_STOCK)) {
            putBoolean(PrefsConstants.EXPORT_TO_STOCK, false)
        }
        if (!hasKey(PrefsConstants.EXPORT_TO_CALENDAR)) {
            putBoolean(PrefsConstants.EXPORT_TO_CALENDAR, false)
        }
        if (!hasKey(PrefsConstants.AUTO_CHECK_BIRTHDAYS)) {
            putBoolean(PrefsConstants.AUTO_CHECK_BIRTHDAYS, false)
        }
        if (!hasKey(PrefsConstants.INFINITE_VIBRATION)) {
            putBoolean(PrefsConstants.INFINITE_VIBRATION, false)
        }
        if (!hasKey(PrefsConstants.AUTO_BACKUP)) {
            putBoolean(PrefsConstants.AUTO_BACKUP, false)
        }
        if (!hasKey(PrefsConstants.SMART_FOLD)) {
            putBoolean(PrefsConstants.SMART_FOLD, false)
        }
        if (!hasKey(PrefsConstants.NOTIFICATION_REPEAT)) {
            putBoolean(PrefsConstants.NOTIFICATION_REPEAT, false)
        }
        if (!hasKey(PrefsConstants.IS_24_TIME_FORMAT)) {
            putBoolean(PrefsConstants.IS_24_TIME_FORMAT, true)
        }
        if (!hasKey(PrefsConstants.UNLOCK_DEVICE)) {
            putBoolean(PrefsConstants.UNLOCK_DEVICE, false)
        }
        if (!hasKey(PrefsConstants.CALENDAR_FEATURE_TASKS)) {
            putBoolean(PrefsConstants.CALENDAR_FEATURE_TASKS, false)
        }
        if (!hasKey(PrefsConstants.MISSED_CALL_REMINDER)) {
            putBoolean(PrefsConstants.MISSED_CALL_REMINDER, false)
        }
        if (!hasKey(PrefsConstants.QUICK_SMS)) {
            putBoolean(PrefsConstants.QUICK_SMS, false)
        }
        if (!hasKey(PrefsConstants.FOLLOW_REMINDER)) {
            putBoolean(PrefsConstants.FOLLOW_REMINDER, false)
        }
        if (!hasKey(PrefsConstants.BIRTHDAY_PERMANENT)) {
            putBoolean(PrefsConstants.BIRTHDAY_PERMANENT, false)
        }
        if (!hasKey(PrefsConstants.REMINDER_CHANGED)) {
            putBoolean(PrefsConstants.REMINDER_CHANGED, false)
        }
        if (!hasKey(PrefsConstants.SYSTEM_VOLUME)) {
            putBoolean(PrefsConstants.SYSTEM_VOLUME, false)
        }
        if (!hasKey(PrefsConstants.INCREASING_VOLUME)) {
            putBoolean(PrefsConstants.INCREASING_VOLUME, false)
        }
        if (!hasKey(PrefsConstants.WAKE_STATUS)) {
            putBoolean(PrefsConstants.WAKE_STATUS, false)
        }
        if (!hasKey(PrefsConstants.GCM_ENABLED)) {
            putBoolean(PrefsConstants.GCM_ENABLED, true)
        }
        if (!hasKey(PrefsConstants.LIVE_CONVERSATION)) {
            putBoolean(PrefsConstants.LIVE_CONVERSATION, true)
        }
        if (!hasKey(PrefsConstants.IGNORE_WINDOW_TYPE)) {
            putBoolean(PrefsConstants.IGNORE_WINDOW_TYPE, true)
        }
        if (!hasKey(PrefsConstants.MAIN_IMAGE_ID)) {
            putInt(PrefsConstants.MAIN_IMAGE_ID, -1)
        }
        if (!hasKey(PrefsConstants.NOTE_COLOR_OPACITY)) {
            putInt(PrefsConstants.NOTE_COLOR_OPACITY, 100)
        }
        if (!hasKey(PrefsConstants.MAIN_IMAGE_PATH)) {
            putString(PrefsConstants.MAIN_IMAGE_PATH, MainImageActivity.DEFAULT_PHOTO)
        }
        if (!hasKey(PrefsConstants.CUSTOM_SOUND)) {
            putString(PrefsConstants.CUSTOM_SOUND, Constants.DEFAULT)
        }
        if (!hasKey(PrefsConstants.APP_LANGUAGE)) {
            putInt(PrefsConstants.APP_LANGUAGE, 0)
        }
        if (!hasKey(PrefsConstants.MAP_STYLE)) {
            putInt(PrefsConstants.MAP_STYLE, 6)
        }
        if (!hasKey(PrefsConstants.CALENDAR_IMAGES)) {
            putObject(PrefsConstants.CALENDAR_IMAGES, MonthImage())
        }
        if (Module.isPro) {
            if (!hasKey(PrefsConstants.LED_STATUS)) {
                putBoolean(PrefsConstants.LED_STATUS, true)
            }
            if (!hasKey(PrefsConstants.LED_COLOR)) {
                putInt(PrefsConstants.LED_COLOR, 11)
            }
            if (!hasKey(PrefsConstants.BIRTHDAY_LED_STATUS)) {
                putBoolean(PrefsConstants.BIRTHDAY_LED_STATUS, false)
            }
            if (!hasKey(PrefsConstants.BIRTHDAY_LED_COLOR)) {
                putInt(PrefsConstants.BIRTHDAY_LED_COLOR, 6)
            }
            if (!hasKey(PrefsConstants.BIRTHDAY_VIBRATION_STATUS)) {
                putBoolean(PrefsConstants.BIRTHDAY_VIBRATION_STATUS, false)
            }
            if (!hasKey(PrefsConstants.BIRTHDAY_SILENT_STATUS)) {
                putBoolean(PrefsConstants.BIRTHDAY_SILENT_STATUS, false)
            }
            if (!hasKey(PrefsConstants.BIRTHDAY_WAKE_STATUS)) {
                putBoolean(PrefsConstants.BIRTHDAY_WAKE_STATUS, false)
            }
            if (!hasKey(PrefsConstants.BIRTHDAY_INFINITE_SOUND)) {
                putBoolean(PrefsConstants.BIRTHDAY_INFINITE_SOUND, false)
            }
            if (!hasKey(PrefsConstants.BIRTHDAY_INFINITE_VIBRATION)) {
                putBoolean(PrefsConstants.BIRTHDAY_INFINITE_VIBRATION, false)
            }
            if (!hasKey(PrefsConstants.BIRTHDAY_USE_GLOBAL)) {
                putBoolean(PrefsConstants.BIRTHDAY_USE_GLOBAL, true)
            }
        } else {
            putInt(PrefsConstants.MARKER_STYLE, 5)
        }
    }

    companion object {

        const val DRIVE_USER_NONE = "none"

        private var instance: Prefs? = null

        fun getInstance(): Prefs {
            if (instance != null) {
                return instance!!
            }
            throw IllegalArgumentException("Use Prefs(Context context) constructor!")
        }

        fun getInstance(context: Context): Prefs {
            if (instance == null) {
                synchronized(Prefs::class.java) {
                    if (instance == null) {
                        instance = Prefs(context.applicationContext)
                    }
                }
            }
            return instance!!
        }
    }
}
