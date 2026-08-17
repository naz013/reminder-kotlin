package com.github.naz013.logic.reminder

interface ReminderPreferences {
  val numberOfReminderOccurrences: Int
  var isSbNotificationEnabled: Boolean
  val isCalendarEnabled: Boolean
  var snoozeTime: Int
  val is24HourFormat: Boolean
  val useMetric: Boolean
  val hapticsEnabled: Boolean
  var remindersCreatedCount: Int
  var reviewDialogShown: Boolean
  var playReviewFlowShown: Boolean
  val startDay: Int
  var isDoNotDisturbEnabled: Boolean
  var doNotDisturbFrom: String
  var doNotDisturbTo: String
  var doNotDisturbIgnore: Int
  var doNotDisturbAction: Int
  var defaultPriority: Int
  var moveCompleted: Boolean
  var isWearEnabled: Boolean
  var isSbIconEnabled: Boolean
  var isNotificationRepeatEnabled: Boolean
  var notificationRepeatTime: Int
  var isLedEnabled: Boolean
  var ledColor: Int
  var isDefaultVibrateEnabled: Boolean
  var defaultVibrationPattern: List<Long>
  var defaultNotificationCategory: String
  var isDefaultBypassDoNotDisturbEnabled: Boolean
  var isDefaultWakeScreenEnabled: Boolean
  var defaultLockScreenVisibility: String
  var initPresets: Boolean
  var initDefaultPresets: Boolean
}
