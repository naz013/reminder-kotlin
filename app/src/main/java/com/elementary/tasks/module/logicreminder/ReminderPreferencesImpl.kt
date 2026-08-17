package com.elementary.tasks.module.logicreminder

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.logic.reminder.ReminderPreferences

class ReminderPreferencesImpl(
  private val prefs: Prefs,
) : ReminderPreferences {
  override val numberOfReminderOccurrences: Int
    get() = prefs.numberOfReminderOccurrences

  override var isSbNotificationEnabled: Boolean
    get() = prefs.isSbNotificationEnabled
    set(value) { prefs.isSbNotificationEnabled = value }

  override val isCalendarEnabled: Boolean
    get() = prefs.isCalendarEnabled

  override var snoozeTime: Int
    get() = prefs.snoozeTime
    set(value) { prefs.snoozeTime = value }

  override val is24HourFormat: Boolean
    get() = prefs.is24HourFormat

  override val useMetric: Boolean
    get() = prefs.useMetric

  override val hapticsEnabled: Boolean
    get() = prefs.hapticsEnabled

  override var remindersCreatedCount: Int
    get() = prefs.remindersCreatedCount
    set(value) { prefs.remindersCreatedCount = value }

  override var reviewDialogShown: Boolean
    get() = prefs.reviewDialogShown
    set(value) { prefs.reviewDialogShown = value }

  override var playReviewFlowShown: Boolean
    get() = prefs.playReviewFlowShown
    set(value) { prefs.playReviewFlowShown = value }

  override val startDay: Int
    get() = prefs.startDay

  override var isDoNotDisturbEnabled: Boolean
    get() = prefs.isDoNotDisturbEnabled
    set(value) { prefs.isDoNotDisturbEnabled = value }

  override var doNotDisturbFrom: String
    get() = prefs.doNotDisturbFrom
    set(value) { prefs.doNotDisturbFrom = value }

  override var doNotDisturbTo: String
    get() = prefs.doNotDisturbTo
    set(value) { prefs.doNotDisturbTo = value }

  override var doNotDisturbIgnore: Int
    get() = prefs.doNotDisturbIgnore
    set(value) { prefs.doNotDisturbIgnore = value }

  override var doNotDisturbAction: Int
    get() = prefs.doNotDisturbAction
    set(value) { prefs.doNotDisturbAction = value }

  override var defaultPriority: Int
    get() = prefs.defaultPriority
    set(value) { prefs.defaultPriority = value }

  override var moveCompleted: Boolean
    get() = prefs.moveCompleted
    set(value) { prefs.moveCompleted = value }

  override var isWearEnabled: Boolean
    get() = prefs.isWearEnabled
    set(value) { prefs.isWearEnabled = value }

  override var isSbIconEnabled: Boolean
    get() = prefs.isSbIconEnabled
    set(value) { prefs.isSbIconEnabled = value }

  override var isNotificationRepeatEnabled: Boolean
    get() = prefs.isNotificationRepeatEnabled
    set(value) { prefs.isNotificationRepeatEnabled = value }

  override var notificationRepeatTime: Int
    get() = prefs.notificationRepeatTime
    set(value) { prefs.notificationRepeatTime = value }

  override var isLedEnabled: Boolean
    get() = prefs.isLedEnabled
    set(value) { prefs.isLedEnabled = value }

  override var ledColor: Int
    get() = prefs.ledColor
    set(value) { prefs.ledColor = value }

  override var isDefaultVibrateEnabled: Boolean
    get() = prefs.isDefaultVibrateEnabled
    set(value) { prefs.isDefaultVibrateEnabled = value }

  override var defaultVibrationPattern: List<Long>
    get() = prefs.defaultVibrationPattern
    set(value) { prefs.defaultVibrationPattern = value }

  override var defaultNotificationCategory: String
    get() = prefs.defaultNotificationCategory
    set(value) { prefs.defaultNotificationCategory = value }

  override var isDefaultBypassDoNotDisturbEnabled: Boolean
    get() = prefs.isDefaultBypassDoNotDisturbEnabled
    set(value) { prefs.isDefaultBypassDoNotDisturbEnabled = value }

  override var isDefaultWakeScreenEnabled: Boolean
    get() = prefs.isDefaultWakeScreenEnabled
    set(value) { prefs.isDefaultWakeScreenEnabled = value }

  override var defaultLockScreenVisibility: String
    get() = prefs.defaultLockScreenVisibility
    set(value) { prefs.defaultLockScreenVisibility = value }

  override var initPresets: Boolean
    get() = prefs.initPresets
    set(value) { prefs.initPresets = value }

  override var initDefaultPresets: Boolean
    get() = prefs.initDefaultPresets
    set(value) { prefs.initDefaultPresets = value }
}
