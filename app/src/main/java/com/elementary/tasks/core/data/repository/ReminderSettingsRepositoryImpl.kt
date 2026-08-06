package com.elementary.tasks.core.data.repository

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.domain.reminder.v2.LockScreenVisibility
import com.github.naz013.domain.reminder.v2.NotificationSettings
import com.github.naz013.domain.reminder.v2.ReminderNotificationCategory
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.repository.ReminderSettingsRepository

/** Wraps [Prefs] (the app's existing SharedPreferences-backed settings store) as the base of the
 * Settings -> Group -> Reminder notification-customization hierarchy. */
class ReminderSettingsRepositoryImpl(
  private val prefs: Prefs
) : ReminderSettingsRepository {

  override fun getNotificationDefaults(): NotificationSettings = NotificationSettings(
    color = prefs.ledColor,
    vibrate = prefs.isDefaultVibrateEnabled,
    vibrationPattern = prefs.defaultVibrationPattern.takeIf { it.isNotEmpty() },
    repeatNotification = prefs.isNotificationRepeatEnabled,
    volume = prefs.defaultVolume,
    soundUri = prefs.defaultSoundUri.takeIf { it.isNotEmpty() },
    quietHoursFrom = prefs.doNotDisturbFrom,
    quietHoursTo = prefs.doNotDisturbTo,
    activeHours = emptyList(),
    delayMinutes = prefs.snoozeTime,
    priority = toReminderPriority(prefs.defaultPriority),
    category = runCatching {
      ReminderNotificationCategory.valueOf(prefs.defaultNotificationCategory)
    }.getOrDefault(ReminderNotificationCategory.DEFAULT),
    bypassDoNotDisturb = prefs.isDefaultBypassDoNotDisturbEnabled,
    wakeScreen = prefs.isDefaultWakeScreenEnabled,
    lockScreenVisibility = runCatching {
      LockScreenVisibility.valueOf(prefs.defaultLockScreenVisibility)
    }.getOrDefault(LockScreenVisibility.PRIVATE),
    remindBefore = 0
  )

  override fun setNotificationDefaults(settings: NotificationSettings) {
    prefs.ledColor = settings.color
    prefs.isDefaultVibrateEnabled = settings.vibrate
    prefs.defaultVibrationPattern = settings.vibrationPattern.orEmpty()
    prefs.isNotificationRepeatEnabled = settings.repeatNotification
    prefs.defaultVolume = settings.volume
    prefs.defaultSoundUri = settings.soundUri.orEmpty()
    prefs.doNotDisturbFrom = settings.quietHoursFrom
    prefs.doNotDisturbTo = settings.quietHoursTo
    prefs.snoozeTime = settings.delayMinutes
    prefs.defaultPriority = settings.priority.ordinal
    prefs.defaultNotificationCategory = settings.category.name
    prefs.isDefaultBypassDoNotDisturbEnabled = settings.bypassDoNotDisturb
    prefs.isDefaultWakeScreenEnabled = settings.wakeScreen
    prefs.defaultLockScreenVisibility = settings.lockScreenVisibility.name
  }

  private fun toReminderPriority(priority: Int): ReminderPriority =
    ReminderPriority.entries.getOrElse(priority) { ReminderPriority.NORMAL }
}
