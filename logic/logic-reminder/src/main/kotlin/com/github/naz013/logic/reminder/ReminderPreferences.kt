package com.github.naz013.logic.reminder

import com.github.naz013.logic.reminder.snooze.SnoozePresetType

interface ReminderPreferences {
  val numberOfReminderOccurrences: Int
  var isSbNotificationEnabled: Boolean
  val isCalendarEnabled: Boolean
  var snoozeTime: Int

  /** Snooze presets offered on the notification, in display order. */
  var enabledSnoozePresets: List<SnoozePresetType>

  /** Wall-clock `HH:mm` used by the "this evening" snooze preset. */
  var snoozeEveningTime: String

  /** Wall-clock `HH:mm` used by the "tomorrow morning" snooze preset. */
  var snoozeMorningTime: String
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
  var isWearCompanionEnabled: Boolean
  var isSbIconEnabled: Boolean
  var isNotificationRepeatEnabled: Boolean
  var notificationRepeatTime: Int
  var maxRepeatCount: Int
  var escalateAfterRepeats: Int
  var isLedEnabled: Boolean
  var ledColor: Int
  var isDefaultVibrateEnabled: Boolean
  var defaultVibrationPattern: List<Long>
  var defaultNotificationCategory: String
  var isDefaultBypassDoNotDisturbEnabled: Boolean
  var isDefaultWakeScreenEnabled: Boolean

  /** When true the builder pre-fills new reminders with the current device zone instead of floating. */
  var useFixedZoneForNewReminders: Boolean

  var isDefaultSwipeToDismissEnabled: Boolean
  var isInAppAlertBannerEnabled: Boolean
  var defaultLockScreenVisibility: String
  var initPresets: Boolean
  var initDefaultPresets: Boolean
  var agendaViewMode: AgendaViewMode

  /** IANA id of the device zone seen by the last zone-change check, null before the first check. */
  var lastKnownZoneId: String?

  /** Floating reminders whose zone re-anchoring failed, as `sourceZone=id1,id2;otherZone=id3`; null when none. */
  var pendingZoneReanchor: String?
}
