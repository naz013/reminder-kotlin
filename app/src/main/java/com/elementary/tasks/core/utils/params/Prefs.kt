package com.elementary.tasks.core.utils.params

import android.content.Context
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatDelegate
import com.elementary.tasks.core.data.platform.ReminderCreatorConfig
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.LED
import com.elementary.tasks.core.utils.Language
import com.elementary.tasks.core.utils.Module
import com.elementary.tasks.core.utils.SuperUtil
import com.elementary.tasks.core.utils.ui.font.FontParams
import com.google.android.gms.maps.GoogleMap
import java.io.File
import java.util.Locale

typealias PrefsObserver = (String) -> Unit

class Prefs(
  context: Context
) : SharedPrefs(context) {

  private val observersMap = mutableMapOf<String, List<PrefsObserver>>()

  fun addObserver(key: String, observer: PrefsObserver) {
    val observers: MutableList<PrefsObserver> = if (observersMap.containsKey(key)) {
      observersMap[key]?.toMutableList() ?: mutableListOf()
    } else {
      mutableListOf()
    }
    observers.add(observer)
    observersMap[key] = observers
  }

  fun removeObserver(key: String, observer: PrefsObserver) {
    val observers: MutableList<PrefsObserver> = if (observersMap.containsKey(key)) {
      observersMap[key]?.toMutableList() ?: mutableListOf()
    } else {
      mutableListOf()
    }
    observers.remove(observer)
    observersMap[key] = observers
  }

  private fun notifyKey(key: String) {
    val observers: MutableList<PrefsObserver> = if (observersMap.containsKey(key)) {
      observersMap[key]?.toMutableList() ?: mutableListOf()
    } else {
      mutableListOf()
    }
    observers.forEach {
      it.invoke(key)
    }
  }

  var lastVersionCode: Long
    get() = getLong(PrefsConstants.LAST_VERSION_CODE, def = Long.MAX_VALUE)
    set(value) = putLong(PrefsConstants.LAST_VERSION_CODE, value)

  var saleMessage: String
    get() = getString(PrefsConstants.SALE_MESSAGE)
    set(value) = putString(PrefsConstants.SALE_MESSAGE, value)

  var internalMessage: String
    get() = getString(PrefsConstants.INTERNAL_MESSAGE)
    set(value) = putString(PrefsConstants.INTERNAL_MESSAGE, value)

  var useMetric: Boolean
    get() = getBoolean(PrefsConstants.METRIC_SYSTEM, def = true)
    set(value) = putBoolean(PrefsConstants.METRIC_SYSTEM, value)

  var initPresets: Boolean
    get() = getBoolean(PrefsConstants.IS_PRESET_INIT, def = true)
    set(value) = putBoolean(PrefsConstants.IS_PRESET_INIT, value)

  var showAdvancedDayDialog: Boolean
    get() = getBoolean(PrefsConstants.RECUR_SHOW_ADVANCED_DAY_DIALOG, def = false)
    set(value) = putBoolean(PrefsConstants.RECUR_SHOW_ADVANCED_DAY_DIALOG, value)

  var trackCalendarIds: Array<Long>
    get() = getLongArray(PrefsConstants.CALENDAR_IDS)
    set(value) = putLongArray(PrefsConstants.CALENDAR_IDS, value)

  var analyticsEnabled: Boolean
    get() = getBoolean(PrefsConstants.ANALYTICS_ENABLED, def = true)
    set(value) = putBoolean(PrefsConstants.ANALYTICS_ENABLED, value)

  var backupAttachedFiles: Boolean
    get() = getBoolean(PrefsConstants.EXPORT_ATTACHED_FILES)
    set(value) = putBoolean(PrefsConstants.EXPORT_ATTACHED_FILES, value)

  @Deprecated(message = "Not used in 29 and above")
  var localBackup: Boolean
    get() = getBoolean(PrefsConstants.LOCAL_BACKUP)
    set(value) = putBoolean(PrefsConstants.LOCAL_BACKUP, value)

  var nightMode: Int
    get() = getInt(PrefsConstants.NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_NO)
    set(value) = putInt(PrefsConstants.NIGHT_MODE, value)

  var autoSyncState: Int
    get() = getInt(PrefsConstants.AUTO_SYNC_STATE)
    set(value) = putInt(PrefsConstants.AUTO_SYNC_STATE, value)

  var autoSyncFlags: Array<String>
    get() = getStringArray(PrefsConstants.AUTO_SYNC_FLAGS)
    set(value) = putStringArray(PrefsConstants.AUTO_SYNC_FLAGS, value)

  var autoBackupState: Int
    get() = getInt(PrefsConstants.AUTO_BACKUP_STATE)
    set(value) = putInt(PrefsConstants.AUTO_BACKUP_STATE, value)

  var autoBackupFlags: Array<String>
    get() = getStringArray(PrefsConstants.AUTO_BACKUP_FLAGS)
    set(value) = putStringArray(PrefsConstants.AUTO_BACKUP_FLAGS, value)

  @Deprecated("After Q")
  var playbackDuration: Int
    get() = getInt(PrefsConstants.PLAYBACK_DURATION)
    set(value) = putInt(PrefsConstants.PLAYBACK_DURATION, value)

  @Deprecated("After Q")
  var birthdayPlaybackDuration: Int
    get() = getInt(PrefsConstants.BIRTHDAY_PLAYBACK_DURATION)
    set(value) = putInt(PrefsConstants.BIRTHDAY_PLAYBACK_DURATION, value)

  var notePalette: Int
    get() = getInt(PrefsConstants.NOTE_PALETTE)
    set(value) = putInt(PrefsConstants.NOTE_PALETTE, value)

  var pinCode: String
    get() = SuperUtil.decrypt(getString(PrefsConstants.PIN_CODE))
    set(value) = putString(PrefsConstants.PIN_CODE, SuperUtil.encrypt(value))

  val hasPinCode: Boolean
    get() = pinCode.isNotEmpty()

  var useFingerprint: Boolean
    get() = getBoolean(PrefsConstants.USE_FINGERPRINT)
    set(value) = putBoolean(PrefsConstants.USE_FINGERPRINT, value)

  var shufflePinView: Boolean
    get() = getBoolean(PrefsConstants.SHUFFLE_PIN_VIEW, def = true)
    set(value) = putBoolean(PrefsConstants.SHUFFLE_PIN_VIEW, value)

  var isDoNotDisturbEnabled: Boolean
    get() = getBoolean(PrefsConstants.DO_NOT_DISTURB_ENABLED, def = false)
    set(value) {
      putBoolean(PrefsConstants.DO_NOT_DISTURB_ENABLED, value)
      notifyKey(PrefsConstants.DO_NOT_DISTURB_ENABLED)
    }

  var doNotDisturbFrom: String
    get() = getString(PrefsConstants.DO_NOT_DISTURB_FROM)
    set(value) {
      putString(PrefsConstants.DO_NOT_DISTURB_FROM, value)
      notifyKey(PrefsConstants.DO_NOT_DISTURB_FROM)
    }

  var doNotDisturbTo: String
    get() = getString(PrefsConstants.DO_NOT_DISTURB_TO)
    set(value) {
      putString(PrefsConstants.DO_NOT_DISTURB_TO, value)
      notifyKey(PrefsConstants.DO_NOT_DISTURB_TO)
    }

  var doNotDisturbIgnore: Int
    get() = getInt(PrefsConstants.DO_NOT_DISTURB_IGNORE)
    set(value) {
      putInt(PrefsConstants.DO_NOT_DISTURB_IGNORE, value)
      notifyKey(PrefsConstants.DO_NOT_DISTURB_IGNORE)
    }

  var doNotDisturbAction: Int
    get() = getInt(PrefsConstants.DO_NOT_DISTURB_ACTION)
    set(value) = putInt(PrefsConstants.DO_NOT_DISTURB_ACTION, value)

  var defaultPriority: Int
    get() = getInt(PrefsConstants.DEFAULT_PRIORITY)
    set(value) = putInt(PrefsConstants.DEFAULT_PRIORITY, value)

  var birthdayPriority: Int
    get() = getInt(PrefsConstants.BIRTHDAY_PRIORITY)
    set(value) = putInt(PrefsConstants.BIRTHDAY_PRIORITY, value)

  @Deprecated("After Q")
  var unlockPriority: Int
    get() = getInt(PrefsConstants.UNLOCK_SCREEN_PRIORITY)
    set(value) = putInt(PrefsConstants.UNLOCK_SCREEN_PRIORITY, value)

  val isTelephonyAllowed: Boolean
    get() = Module.hasTelephony(context) && isTelephonyEnabled

  var isTelephonyEnabled: Boolean
    get() = getBoolean(PrefsConstants.ALLOW_SMS_AND_CALL, true)
    set(value) = putBoolean(PrefsConstants.ALLOW_SMS_AND_CALL, value)

  var moveCompleted: Boolean
    get() = getBoolean(PrefsConstants.MOVE_TO_TRASH, false)
    set(value) = putBoolean(PrefsConstants.MOVE_TO_TRASH, value)

  var appLanguage: Int
    get() = getInt(PrefsConstants.APP_LANGUAGE)
    set(value) = putInt(PrefsConstants.APP_LANGUAGE, value)

  var isTellAboutEvent: Boolean
    get() = getBoolean(PrefsConstants.TELL_ABOUT_EVENT)
    set(value) = putBoolean(PrefsConstants.TELL_ABOUT_EVENT, value)

  var lastUsedReminder: Int
    get() = getInt(PrefsConstants.LAST_USED_REMINDER)
    set(value) = putInt(PrefsConstants.LAST_USED_REMINDER, value)

  var markerStyle: Int
    get() = getInt(PrefsConstants.MARKER_STYLE)
    set(value) = putInt(PrefsConstants.MARKER_STYLE, value)

  var todayColor: Int
    get() = getInt(PrefsConstants.TODAY_COLOR)
    set(value) = putInt(PrefsConstants.TODAY_COLOR, value)

  var reminderColor: Int
    get() = getInt(PrefsConstants.REMINDER_COLOR)
    set(value) = putInt(PrefsConstants.REMINDER_COLOR, value)

  var birthdayColor: Int
    get() = getInt(PrefsConstants.BIRTH_COLOR)
    set(value) = putInt(PrefsConstants.BIRTH_COLOR, value)

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
    get() = getInt(PrefsConstants.CONVERSATION_LOCALE)
    set(value) = putInt(PrefsConstants.CONVERSATION_LOCALE, value)

  @Deprecated("After Q")
  var ttsLocale: String
    get() = getString(PrefsConstants.TTS_LOCALE)
    set(value) = putString(PrefsConstants.TTS_LOCALE, value)

  @Deprecated("After Q")
  var birthdayTtsLocale: String
    get() = getString(PrefsConstants.BIRTHDAY_TTS_LOCALE)
    set(value) = putString(PrefsConstants.BIRTHDAY_TTS_LOCALE, value)

  var isSystemLoudnessEnabled: Boolean
    get() = getBoolean(PrefsConstants.SYSTEM_VOLUME)
    set(value) = putBoolean(PrefsConstants.SYSTEM_VOLUME, value)

  var soundStream: Int
    get() = getInt(PrefsConstants.SOUND_STREAM)
    set(value) = putInt(PrefsConstants.SOUND_STREAM, value)

  val is24HourFormat: Boolean
    get() {
      val hourFormat = hourFormat
      return if (hourFormat == 0) {
        DateFormat.is24HourFormat(context)
      } else {
        hourFormat == 1
      }
    }

  var hourFormat: Int
    get() = getInt(PrefsConstants.TIME_FORMAT)
    set(value) = putInt(PrefsConstants.TIME_FORMAT, value)

  var isCalendarEnabled: Boolean
    get() = getBoolean(PrefsConstants.EXPORT_TO_CALENDAR)
    set(value) = putBoolean(PrefsConstants.EXPORT_TO_CALENDAR, value)

  var isStockCalendarEnabled: Boolean
    get() = getBoolean(PrefsConstants.EXPORT_TO_STOCK)
    set(value) = putBoolean(PrefsConstants.EXPORT_TO_STOCK, value)

  @Deprecated("After Q")
  var isFoldingEnabled: Boolean
    get() = getBoolean(PrefsConstants.SMART_FOLD)
    set(value) = putBoolean(PrefsConstants.SMART_FOLD, value)

  var isWearEnabled: Boolean
    get() = getBoolean(PrefsConstants.WEAR_NOTIFICATION)
    set(value) = putBoolean(PrefsConstants.WEAR_NOTIFICATION, value)

  var isSbNotificationEnabled: Boolean
    get() = getBoolean(PrefsConstants.STATUS_BAR_NOTIFICATION)
    set(value) = putBoolean(PrefsConstants.STATUS_BAR_NOTIFICATION, value)

  var radius: Int
    get() = getInt(PrefsConstants.LOCATION_RADIUS, def = DefaultValues.RADIUS)
    set(value) = putInt(PrefsConstants.LOCATION_RADIUS, value)

  var isDistanceNotificationEnabled: Boolean
    get() = getBoolean(
      PrefsConstants.TRACKING_NOTIFICATION,
      def = DefaultValues.LOCATION_TRACK_NOTIFICATION
    )
    set(value) = putBoolean(PrefsConstants.TRACKING_NOTIFICATION, value)

  var mapType: Int
    get() = getInt(PrefsConstants.MAP_TYPE)
    set(value) = putInt(PrefsConstants.MAP_TYPE, value)

  var mapStyle: Int
    get() = getInt(PrefsConstants.MAP_STYLE)
    set(value) = putInt(PrefsConstants.MAP_STYLE, value)

  var trackTime: Int
    get() = getInt(PrefsConstants.TRACK_TIME)
    set(value) = putInt(PrefsConstants.TRACK_TIME, value)

  var driveUser: String
    get() = SuperUtil.decrypt(getString(PrefsConstants.DRIVE_USER))
    set(value) = putString(PrefsConstants.DRIVE_USER, SuperUtil.encrypt(value))

  var tasksUser: String
    get() = SuperUtil.decrypt(getString(PrefsConstants.TASKS_USER))
    set(value) = putString(PrefsConstants.TASKS_USER, SuperUtil.encrypt(value))

  @Deprecated("After Q")
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

  @Deprecated("After Q")
  var isSoundInSilentModeEnabled: Boolean
    get() = getBoolean(PrefsConstants.SILENT_SOUND)
    set(value) = putBoolean(PrefsConstants.SILENT_SOUND, value)

  @Deprecated("After Q")
  var isInfiniteSoundEnabled: Boolean
    get() = getBoolean(PrefsConstants.INFINITE_SOUND)
    set(value) = putBoolean(PrefsConstants.INFINITE_SOUND, value)

  var melodyFile: String
    get() = getString(PrefsConstants.CUSTOM_SOUND)
    set(value) = putString(PrefsConstants.CUSTOM_SOUND, value)

  var loudness: Int
    get() = getInt(PrefsConstants.VOLUME)
    set(value) = putInt(PrefsConstants.VOLUME, value)

  @Deprecated("After Q")
  var isIncreasingLoudnessEnabled: Boolean
    get() = getBoolean(PrefsConstants.INCREASING_VOLUME)
    set(value) = putBoolean(PrefsConstants.INCREASING_VOLUME, value)

  @Deprecated("After Q")
  var isTtsEnabled: Boolean
    get() = getBoolean(PrefsConstants.TTS)
    set(value) = putBoolean(PrefsConstants.TTS, value)

  @Deprecated("After Q")
  var isDeviceUnlockEnabled: Boolean
    get() = getBoolean(PrefsConstants.UNLOCK_DEVICE)
    set(value) = putBoolean(PrefsConstants.UNLOCK_DEVICE, value)

  @Deprecated("After Q")
  var isAutoLaunchEnabled: Boolean
    get() = getBoolean(PrefsConstants.APPLICATION_AUTO_LAUNCH)
    set(value) = putBoolean(PrefsConstants.APPLICATION_AUTO_LAUNCH, value)

  @Deprecated("After Q")
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

  var isBackupEnabled: Boolean
    get() = getBoolean(PrefsConstants.DATA_BACKUP)
    set(value) {
      putBoolean(PrefsConstants.DATA_BACKUP, value)
      notifyKey(PrefsConstants.DATA_BACKUP)
    }

  var calendarEventDuration: Int
    get() = getInt(PrefsConstants.EVENT_DURATION)
    set(value) = putInt(PrefsConstants.EVENT_DURATION, value)

  @Deprecated("Use {defaultCalendarId} parameter")
  private var calendarId: Int
    get() = getInt(PrefsConstants.CALENDAR_ID)
    set(value) = putInt(PrefsConstants.CALENDAR_ID, value)

  var defaultCalendarId: Long
    get() = getLong(PrefsConstants.DEFAULT_CALENDAR_ID)
    set(value) = putLong(PrefsConstants.DEFAULT_CALENDAR_ID, value)

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

  @Deprecated("Use {calendarIds} parameter")
  private var eventsCalendar: Int
    get() = getInt(PrefsConstants.EVENTS_CALENDAR)
    set(value) = putInt(PrefsConstants.EVENTS_CALENDAR, value)

  var isBirthdayReminderEnabled: Boolean
    get() = getBoolean(PrefsConstants.BIRTHDAY_REMINDER, def = true)
    set(value) = putBoolean(PrefsConstants.BIRTHDAY_REMINDER, value)

  var birthdayTime: String
    get() = getString(PrefsConstants.BIRTHDAY_REMINDER_TIME)
    set(value) = putString(PrefsConstants.BIRTHDAY_REMINDER_TIME, value)

  var isBirthdayInWidgetEnabled: Boolean
    get() = getBoolean(PrefsConstants.WIDGET_BIRTHDAYS, def = true)
    set(value) = putBoolean(PrefsConstants.WIDGET_BIRTHDAYS, value)

  var isBirthdayPermanentEnabled: Boolean
    get() = getBoolean(PrefsConstants.BIRTHDAY_PERMANENT)
    set(value) = putBoolean(PrefsConstants.BIRTHDAY_PERMANENT, value)

  var daysToBirthday: Int
    get() = getInt(PrefsConstants.DAYS_TO_BIRTHDAY)
    set(value) = putInt(PrefsConstants.DAYS_TO_BIRTHDAY, value)

  var birthdayDurationInDays: Int
    get() = getInt(PrefsConstants.TO_BIRTHDAY_DAYS)
    set(value) = putInt(PrefsConstants.TO_BIRTHDAY_DAYS, value)

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

  @Deprecated("After Q")
  var isBirthdaySilentEnabled: Boolean
    get() = getBoolean(PrefsConstants.BIRTHDAY_SILENT_STATUS)
    set(value) = putBoolean(PrefsConstants.BIRTHDAY_SILENT_STATUS, value)

  @Deprecated("After Q")
  var isBirthdayInfiniteSoundEnabled: Boolean
    get() = getBoolean(PrefsConstants.BIRTHDAY_INFINITE_SOUND)
    set(value) = putBoolean(PrefsConstants.BIRTHDAY_INFINITE_SOUND, value)

  @Deprecated("After Q")
  var isBirthdayWakeEnabled: Boolean
    get() = getBoolean(PrefsConstants.BIRTHDAY_WAKE_STATUS)
    set(value) = putBoolean(PrefsConstants.BIRTHDAY_WAKE_STATUS, value)

  var isBirthdayLedEnabled: Boolean
    get() = getBoolean(PrefsConstants.BIRTHDAY_LED_STATUS)
    set(value) = putBoolean(PrefsConstants.BIRTHDAY_LED_STATUS, value)

  @Deprecated("After Q")
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

  var rateCount: Int
    get() = getInt(PrefsConstants.RATE_COUNT)
    set(count) = putInt(PrefsConstants.RATE_COUNT, count)

  var isUserLogged: Boolean
    get() = getBoolean(PrefsConstants.USER_LOGGED)
    set(value) {
      putBoolean(PrefsConstants.USER_LOGGED, value)
      notifyKey(PrefsConstants.USER_LOGGED)
    }

  var isPrivacyPolicyShowed: Boolean
    get() = getBoolean(PrefsConstants.PRIVACY_SHOWED)
    set(value) {
      putBoolean(PrefsConstants.PRIVACY_SHOWED, value)
      notifyKey(PrefsConstants.PRIVACY_SHOWED)
    }

  var isLiveEnabled: Boolean
    get() = getBoolean(PrefsConstants.LIVE_CONVERSATION)
    set(value) = putBoolean(PrefsConstants.LIVE_CONVERSATION, value)

  var isNoteFontSizeRememberingEnabled: Boolean
    get() = getBoolean(PrefsConstants.REMEMBER_NOTE_FONT_SIZE, def = true)
    set(value) = putBoolean(PrefsConstants.REMEMBER_NOTE_FONT_SIZE, value)

  var lastNoteFontSize: Int
    get() = getInt(PrefsConstants.LAST_NOTE_FONT_SIZE, def = FontParams.DEFAULT_FONT_SIZE)
    set(value) = putInt(PrefsConstants.LAST_NOTE_FONT_SIZE, value)

  var isNoteFontStyleRememberingEnabled: Boolean
    get() = getBoolean(PrefsConstants.REMEMBER_NOTE_FONT_STYLE, def = true)
    set(value) = putBoolean(PrefsConstants.REMEMBER_NOTE_FONT_STYLE, value)

  var lastNoteFontStyle: Int
    get() = getInt(PrefsConstants.LAST_NOTE_FONT_STYLE, def = FontParams.DEFAULT_FONT_STYLE)
    set(value) = putInt(PrefsConstants.LAST_NOTE_FONT_STYLE, value)

  var isNoteColorRememberingEnabled: Boolean
    get() = getBoolean(PrefsConstants.REMEMBER_NOTE_COLOR)
    set(value) = putBoolean(PrefsConstants.REMEMBER_NOTE_COLOR, value)

  var lastNoteColor: Int
    get() = getInt(PrefsConstants.LAST_NOTE_COLOR)
    set(count) = putInt(PrefsConstants.LAST_NOTE_COLOR, count)

  var noteColorOpacity: Int
    get() = getInt(PrefsConstants.NOTE_COLOR_OPACITY)
    set(count) = putInt(PrefsConstants.NOTE_COLOR_OPACITY, count)

  @Deprecated("After Q")
  var reminderType: Int
    get() = getInt(PrefsConstants.REMINDER_TYPE)
    set(reminderType) = putInt(PrefsConstants.REMINDER_TYPE, reminderType)

  var dropboxUid: String
    get() = getString(PrefsConstants.DROPBOX_UID)
    set(uid) = putString(PrefsConstants.DROPBOX_UID, uid)

  var dropboxToken: String
    get() = getString(PrefsConstants.DROPBOX_TOKEN)
    set(token) = putString(PrefsConstants.DROPBOX_TOKEN, token)

  @Deprecated("After Q")
  var isIgnoreWindowType: Boolean
    get() = getBoolean(PrefsConstants.IGNORE_WINDOW_TYPE)
    set(value) = putBoolean(PrefsConstants.IGNORE_WINDOW_TYPE, value)

  var privacyUrl: String
    get() = getString(
      PrefsConstants.PRIVACY_POLICY_URL,
      "https://sukhovych.com/reminder-privacy-policy/"
    )
    set(value) = putString(PrefsConstants.PRIVACY_POLICY_URL, value)

  var termsUrl: String
    get() = getString(PrefsConstants.TERMS_URL, "https://sukhovych.com/terms-and-conditions/")
    set(value) = putString(PrefsConstants.TERMS_URL, value)

  var voiceHelpUrls: String
    get() = getString(PrefsConstants.VOICE_HELP_URLS, "{}")
    set(value) = putString(PrefsConstants.VOICE_HELP_URLS, value)

  var isAutoMicClick: Boolean
    get() = getBoolean(PrefsConstants.CONVERSATION_AUTO_MIC, true)
    set(value) = putBoolean(PrefsConstants.CONVERSATION_AUTO_MIC, value)

  var useDynamicColors: Boolean
    get() = getBoolean(PrefsConstants.DYNAMIC_COLORS, false)
    set(value) = putBoolean(PrefsConstants.DYNAMIC_COLORS, value)

  var noteMigrationDone: Boolean
    get() = getBoolean("note_migration", false)
    set(value) = putBoolean("note_migration", value)

  var reminderCreatorParams: ReminderCreatorConfig
    get() = ReminderCreatorConfig(
      getString(
        PrefsConstants.REMINDER_CREATOR_PARAMS,
        ReminderCreatorConfig.DEFAULT_VALUE
      )
    )
    set(value) = putString(PrefsConstants.REMINDER_CREATOR_PARAMS, value.toHex())

  fun initPrefs() {
    val settingsUI = File(
      "/data/data/" + context.packageName + "/shared_prefs/" + PrefsConstants.PREFS_NAME + ".xml"
    )
    if (!settingsUI.exists()) {
      val preferences = context.getSharedPreferences(
        PrefsConstants.PREFS_NAME,
        Context.MODE_PRIVATE
      )
      val editor = preferences.edit()
      editor.putInt(PrefsConstants.TODAY_COLOR, 0)
      editor.putInt(PrefsConstants.BIRTH_COLOR, 2)
      editor.putInt(PrefsConstants.REMINDER_COLOR, 4)
      editor.putInt(PrefsConstants.MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL)
      editor.putString(PrefsConstants.DRIVE_USER, DRIVE_USER_NONE)
      editor.putString(PrefsConstants.REMINDER_IMAGE, Constants.DEFAULT)
      editor.putInt(PrefsConstants.LED_COLOR, LED.BLUE)
      editor.putInt(PrefsConstants.BIRTHDAY_LED_COLOR, LED.BLUE)
      editor.putInt(PrefsConstants.LOCATION_RADIUS, DefaultValues.RADIUS)
      editor.putInt(PrefsConstants.MARKER_STYLE, 5)
      editor.putInt(PrefsConstants.TRACK_TIME, 1)
      editor.putInt(PrefsConstants.VOLUME, 25)
      val localeCheck = Locale.getDefault().toString().lowercase()
      val locale = when {
        localeCheck.startsWith("uk") -> 2
        else -> 0
      }
      editor.putInt(PrefsConstants.CONVERSATION_LOCALE, locale)
      editor.putString(PrefsConstants.TIME_MORNING, "7:0")
      editor.putString(PrefsConstants.TIME_DAY, "12:0")
      editor.putString(PrefsConstants.TIME_EVENING, "19:0")
      editor.putString(PrefsConstants.TIME_NIGHT, "23:0")
      editor.putString(PrefsConstants.DO_NOT_DISTURB_FROM, "20:00")
      editor.putString(PrefsConstants.DO_NOT_DISTURB_TO, "7:00")
      editor.putString(PrefsConstants.TTS_LOCALE, Language.ENGLISH)
      editor.putString(PrefsConstants.CUSTOM_SOUND, Constants.SOUND_NOTIFICATION)
      editor.putInt(PrefsConstants.DEFAULT_PRIORITY, 2)
      editor.putInt(PrefsConstants.BIRTHDAY_PRIORITY, 2)
      editor.putInt(PrefsConstants.DO_NOT_DISTURB_IGNORE, 5)
      editor.putInt(PrefsConstants.APP_LANGUAGE, 0)
      editor.putInt(PrefsConstants.START_DAY, 1)
      editor.putInt(PrefsConstants.DAYS_TO_BIRTHDAY, 0)
      editor.putInt(PrefsConstants.NOTIFICATION_REPEAT_INTERVAL, 15)
      editor.putInt(PrefsConstants.APP_RUNS_COUNT, 0)
      editor.putInt(PrefsConstants.DELAY_TIME, 5)
      editor.putInt(PrefsConstants.EVENT_DURATION, 30)
      editor.putInt(PrefsConstants.AUTO_CHECK_FOR_EVENTS_INTERVAL, 6)
      editor.putInt(PrefsConstants.SOUND_STREAM, 5)
      editor.putInt(PrefsConstants.NOTE_COLOR_OPACITY, 100)
      editor.putInt(PrefsConstants.MAP_STYLE, 6)
      editor.putInt(PrefsConstants.NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
      editor.putBoolean(
        PrefsConstants.TRACKING_NOTIFICATION,
        DefaultValues.LOCATION_TRACK_NOTIFICATION
      )
      editor.putBoolean(PrefsConstants.RATE_SHOW, false)
      editor.putBoolean(PrefsConstants.CONTACT_BIRTHDAYS, false)
      editor.putBoolean(PrefsConstants.BIRTHDAY_REMINDER, true)
      editor.putBoolean(PrefsConstants.EXPORT_TO_CALENDAR, false)
      editor.putBoolean(PrefsConstants.AUTO_CHECK_BIRTHDAYS, false)
      editor.putBoolean(PrefsConstants.INFINITE_VIBRATION, false)
      editor.putBoolean(PrefsConstants.NOTIFICATION_REPEAT, false)
      editor.putBoolean(PrefsConstants.WIDGET_BIRTHDAYS, false)
      editor.putBoolean(PrefsConstants.EXPORT_TO_STOCK, false)
      editor.putBoolean(PrefsConstants.REMINDERS_IN_CALENDAR, true)
      editor.putInt(PrefsConstants.TIME_FORMAT, 0)
      editor.putBoolean(PrefsConstants.UNLOCK_DEVICE, false)
      editor.putBoolean(PrefsConstants.CALENDAR_FEATURE_TASKS, true)
      editor.putBoolean(PrefsConstants.TTS, false)
      editor.putBoolean(PrefsConstants.BIRTHDAY_PERMANENT, false)
      editor.putBoolean(PrefsConstants.REMINDER_CHANGED, false)
      editor.putBoolean(PrefsConstants.SYSTEM_VOLUME, false)
      editor.putBoolean(PrefsConstants.INCREASING_VOLUME, false)
      editor.putBoolean(PrefsConstants.LIVE_CONVERSATION, true)
      editor.putBoolean(PrefsConstants.IGNORE_WINDOW_TYPE, true)
      if (Module.isPro) {
        editor.putBoolean(PrefsConstants.BIRTHDAY_LED_STATUS, false)
        editor.putBoolean(PrefsConstants.LED_STATUS, true)
        editor.putInt(PrefsConstants.BIRTHDAY_LED_COLOR, 6)
        editor.putInt(PrefsConstants.LED_COLOR, 11)
        editor.putBoolean(PrefsConstants.BIRTHDAY_USE_GLOBAL, true)
        editor.putBoolean(PrefsConstants.BIRTHDAY_INFINITE_VIBRATION, false)
        editor.putBoolean(PrefsConstants.BIRTHDAY_VIBRATION_STATUS, false)
        editor.putBoolean(PrefsConstants.BIRTHDAY_WAKE_STATUS, false)
      }
      editor.apply()
    }
  }

  fun checkPrefs() {
    if (hasKey(PrefsConstants.CALENDAR_ID)) {
      if (calendarId != 0) {
        defaultCalendarId = calendarId.toLong()
      }
      removeKey(PrefsConstants.CALENDAR_ID)
    }
    if (hasKey(PrefsConstants.EVENTS_CALENDAR)) {
      if (eventsCalendar != 0) {
        trackCalendarIds = arrayOf(eventsCalendar.toLong())
      }
      removeKey(PrefsConstants.EVENTS_CALENDAR)
    }
    if (!hasKey(PrefsConstants.TODAY_COLOR)) {
      putInt(PrefsConstants.TODAY_COLOR, 4)
    }
    if (!hasKey(PrefsConstants.BIRTH_COLOR)) {
      putInt(PrefsConstants.BIRTH_COLOR, 1)
    }
    if (!hasKey(PrefsConstants.REMINDER_COLOR)) {
      putInt(PrefsConstants.REMINDER_COLOR, 6)
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

    if (!hasKey(PrefsConstants.CONVERSATION_LOCALE)) {
      putInt(PrefsConstants.CONVERSATION_LOCALE, 0)
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
    if (!hasKey(PrefsConstants.START_DAY)) {
      putInt(PrefsConstants.START_DAY, 1)
    }
    if (!hasKey(PrefsConstants.DO_NOT_DISTURB_IGNORE)) {
      putInt(PrefsConstants.DO_NOT_DISTURB_IGNORE, 5)
    }
    if (!hasKey(PrefsConstants.DEFAULT_PRIORITY)) {
      putInt(PrefsConstants.DEFAULT_PRIORITY, 2)
    }
    if (!hasKey(PrefsConstants.BIRTHDAY_PRIORITY)) {
      putInt(PrefsConstants.BIRTHDAY_PRIORITY, 2)
    }
    if (!hasKey(PrefsConstants.BIRTHDAY_REMINDER_TIME)) {
      putString(PrefsConstants.BIRTHDAY_REMINDER_TIME, "12:00")
    }
    if (!hasKey(PrefsConstants.DO_NOT_DISTURB_FROM)) {
      putString(PrefsConstants.DO_NOT_DISTURB_FROM, "20:00")
    }
    if (!hasKey(PrefsConstants.DO_NOT_DISTURB_TO)) {
      putString(PrefsConstants.DO_NOT_DISTURB_TO, "7:00")
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
      putInt(PrefsConstants.MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL)
    }
    if (!hasKey(PrefsConstants.SOUND_STREAM)) {
      putInt(PrefsConstants.SOUND_STREAM, 5)
    }
    if (!hasKey(PrefsConstants.RATE_SHOW)) {
      putBoolean(PrefsConstants.RATE_SHOW, false)
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
    if (!hasKey(PrefsConstants.NOTIFICATION_REPEAT)) {
      putBoolean(PrefsConstants.NOTIFICATION_REPEAT, false)
    }
    if (!hasKey(PrefsConstants.TIME_FORMAT)) {
      putInt(PrefsConstants.TIME_FORMAT, 0)
    }
    if (!hasKey(PrefsConstants.UNLOCK_DEVICE)) {
      putBoolean(PrefsConstants.UNLOCK_DEVICE, false)
    }
    if (!hasKey(PrefsConstants.CALENDAR_FEATURE_TASKS)) {
      putBoolean(PrefsConstants.CALENDAR_FEATURE_TASKS, false)
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
    if (!hasKey(PrefsConstants.LIVE_CONVERSATION)) {
      putBoolean(PrefsConstants.LIVE_CONVERSATION, true)
    }
    if (!hasKey(PrefsConstants.IGNORE_WINDOW_TYPE)) {
      putBoolean(PrefsConstants.IGNORE_WINDOW_TYPE, true)
    }
    if (!hasKey(PrefsConstants.NOTE_COLOR_OPACITY)) {
      putInt(PrefsConstants.NOTE_COLOR_OPACITY, 100)
    }
    if (!hasKey(PrefsConstants.CUSTOM_SOUND)) {
      putString(PrefsConstants.CUSTOM_SOUND, Constants.SOUND_NOTIFICATION)
    }
    if (!hasKey(PrefsConstants.APP_LANGUAGE)) {
      putInt(PrefsConstants.APP_LANGUAGE, 0)
    }
    if (!hasKey(PrefsConstants.MAP_STYLE)) {
      putInt(PrefsConstants.MAP_STYLE, 6)
    }
    if (!hasKey(PrefsConstants.NIGHT_MODE)) {
      putInt(PrefsConstants.NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
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
  }
}
