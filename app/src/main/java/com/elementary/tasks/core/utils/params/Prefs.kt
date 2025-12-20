package com.elementary.tasks.core.utils.params

import android.content.Context
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatDelegate
import com.elementary.tasks.core.cloud.worker.WorkerNetworkType
import com.elementary.tasks.core.data.platform.ReminderCreatorConfig
import com.elementary.tasks.core.utils.SuperUtil
import com.github.naz013.common.Module
import com.github.naz013.domain.font.FontParams

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

  var addRemindersToGoogleCalendar: Boolean
    get() = getBoolean(PrefsConstants.GOOGLE_CALENDAR_ADD_REMINDERS, def = false)
    set(value) = putBoolean(PrefsConstants.GOOGLE_CALENDAR_ADD_REMINDERS, value)

  var scanGoogleCalendarEvents: Boolean
    get() = getBoolean(PrefsConstants.GOOGLE_CALENDAR_SYNC, def = false)
    set(value) = putBoolean(PrefsConstants.GOOGLE_CALENDAR_SYNC, value)

  var googleCalendarReminderId: Long
    get() = getLong(PrefsConstants.GOOGLE_CALENDAR_ID, def = -1L)
    set(value) = putLong(PrefsConstants.GOOGLE_CALENDAR_ID, value)

  var occurrenceMigrated: Boolean
    get() = getBoolean(PrefsConstants.OCCURRENCE_MIGRATED, def = false)
    set(value) = putBoolean(PrefsConstants.OCCURRENCE_MIGRATED, value)

  var numberOfReminderOccurrences: Int
    get() = getInt(PrefsConstants.OCCURRENCE_COUNT_REMINDERS, def = 1000)
    set(value) = putInt(PrefsConstants.OCCURRENCE_COUNT_REMINDERS, value)

  var numberOfBirthdayOccurrences: Int
    get() = getInt(PrefsConstants.OCCURRENCE_COUNT_BIRTHDAYS, def = 10)
    set(value) = putInt(PrefsConstants.OCCURRENCE_COUNT_BIRTHDAYS, value)

  var workerNetworkType: WorkerNetworkType
    get() {
      val type = getInt(PrefsConstants.WORKER_NETWORK_TYPE, def = 1)
      return when (type) {
        0 -> WorkerNetworkType.Any
        1 -> WorkerNetworkType.Wifi
        2 -> WorkerNetworkType.Cellular
        else -> WorkerNetworkType.Any
      }
    }
    set(value) = putInt(PrefsConstants.WORKER_NETWORK_TYPE, value.ordinal)

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

  var initDefaultPresets: Boolean
    get() = getBoolean(PrefsConstants.IS_DEFAULT_PRESET_INIT, def = true)
    set(value) = putBoolean(PrefsConstants.IS_DEFAULT_PRESET_INIT, value)

  var remindersCreatedCount: Int
    get() = getInt(PrefsConstants.REMINDERS_CREATED_COUNT, def = 0)
    set(value) = putInt(PrefsConstants.REMINDERS_CREATED_COUNT, value)

  var reviewDialogShown: Boolean
    get() = getBoolean(PrefsConstants.REVIEW_DIALOG_SHOWN, def = false)
    set(value) = putBoolean(PrefsConstants.REVIEW_DIALOG_SHOWN, value)

  var analyticsEnabled: Boolean
    get() = getBoolean(PrefsConstants.ANALYTICS_ENABLED, def = true)
    set(value) = putBoolean(PrefsConstants.ANALYTICS_ENABLED, value)

  var nightMode: Int
    get() = getInt(PrefsConstants.NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_NO)
    set(value) = putInt(PrefsConstants.NIGHT_MODE, value)

  var autoBackupState: Int
    get() = getInt(PrefsConstants.AUTO_BACKUP_STATE)
    set(value) = putInt(PrefsConstants.AUTO_BACKUP_STATE, value)

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

  var isSbIconEnabled: Boolean
    get() = getBoolean(PrefsConstants.STATUS_BAR_ICON)
    set(value) = putBoolean(PrefsConstants.STATUS_BAR_ICON, value)

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

  var calendarEventDuration: Int
    get() = getInt(PrefsConstants.EVENT_DURATION)
    set(value) = putInt(PrefsConstants.EVENT_DURATION, value)

  var startDay: Int
    get() = getInt(PrefsConstants.START_DAY)
    set(value) = putInt(PrefsConstants.START_DAY, value)

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

  var isBirthdayLedEnabled: Boolean
    get() = getBoolean(PrefsConstants.BIRTHDAY_LED_STATUS)
    set(value) = putBoolean(PrefsConstants.BIRTHDAY_LED_STATUS, value)

  var birthdayLedColor: Int
    get() = getInt(PrefsConstants.BIRTHDAY_LED_COLOR)
    set(value) = putInt(PrefsConstants.BIRTHDAY_LED_COLOR, value)

  var noteOrder: String
    get() = getString(PrefsConstants.NOTES_ORDER)
    set(value) = putString(PrefsConstants.NOTES_ORDER, value)

  var isNotesGridEnabled: Boolean
    get() = getBoolean(PrefsConstants.NOTES_LIST_STYLE)
    set(value) = putBoolean(PrefsConstants.NOTES_LIST_STYLE, value)

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
    get() = getBoolean(PrefsConstants.REMEMBER_NOTE_COLOR, def = true)
    set(value) = putBoolean(PrefsConstants.REMEMBER_NOTE_COLOR, value)

  var lastNoteColor: Int
    get() = getInt(PrefsConstants.LAST_NOTE_COLOR)
    set(count) = putInt(PrefsConstants.LAST_NOTE_COLOR, count)

  var noteColorOpacity: Int
    get() = getInt(PrefsConstants.NOTE_COLOR_OPACITY)
    set(count) = putInt(PrefsConstants.NOTE_COLOR_OPACITY, count)

  var dropboxToken: String
    get() = getString(PrefsConstants.DROPBOX_TOKEN)
    set(token) = putString(PrefsConstants.DROPBOX_TOKEN, token)

  var privacyUrl: String
    get() = getString(
      PrefsConstants.PRIVACY_POLICY_URL,
      "https://sukhovych.com/reminder-privacy-policy/"
    )
    set(value) = putString(PrefsConstants.PRIVACY_POLICY_URL, value)

  var termsUrl: String
    get() = getString(PrefsConstants.TERMS_URL, "https://sukhovych.com/terms-and-conditions/")
    set(value) = putString(PrefsConstants.TERMS_URL, value)

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
}
