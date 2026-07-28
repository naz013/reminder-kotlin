package com.github.naz013.domain.reminder.v2

/**
 * The override shape held by [GroupV2] and [ReminderV2]: every field is nullable, where
 * `null` means "inherit from the next level down" (Reminder -> Group -> Settings).
 */
data class NotificationSettingsOverride(
  val color: Int? = null,
  val vibrate: Boolean? = null,
  val vibrationPattern: List<Long>? = null,
  val repeatNotification: Boolean? = null,
  val volume: Int? = null,
  val soundUri: String? = null,
  val quietHoursFrom: String? = null,
  val quietHoursTo: String? = null,
  val activeHours: List<Int>? = null,
  val delayMinutes: Int? = null,
  val priority: ReminderPriority? = null,
  val category: ReminderNotificationCategory? = null,
  val bypassDoNotDisturb: Boolean? = null,
  val wakeScreen: Boolean? = null,
  val lockScreenVisibility: LockScreenVisibility? = null,
  val remindBefore: Long? = null
)

/**
 * Resolves the 3-level hierarchy field-by-field: this override (Reminder) wins, then [group]'s
 * override, then [defaults] (the global Settings, always fully populated).
 */
fun NotificationSettingsOverride.resolve(
  group: NotificationSettingsOverride?,
  defaults: NotificationSettings
): NotificationSettings = NotificationSettings(
  color = color ?: group?.color ?: defaults.color,
  vibrate = vibrate ?: group?.vibrate ?: defaults.vibrate,
  vibrationPattern = vibrationPattern ?: group?.vibrationPattern ?: defaults.vibrationPattern,
  repeatNotification = repeatNotification ?: group?.repeatNotification ?: defaults.repeatNotification,
  volume = volume ?: group?.volume ?: defaults.volume,
  soundUri = soundUri ?: group?.soundUri ?: defaults.soundUri,
  quietHoursFrom = quietHoursFrom ?: group?.quietHoursFrom ?: defaults.quietHoursFrom,
  quietHoursTo = quietHoursTo ?: group?.quietHoursTo ?: defaults.quietHoursTo,
  activeHours = activeHours ?: group?.activeHours ?: defaults.activeHours,
  delayMinutes = delayMinutes ?: group?.delayMinutes ?: defaults.delayMinutes,
  priority = priority ?: group?.priority ?: defaults.priority,
  category = category ?: group?.category ?: defaults.category,
  bypassDoNotDisturb = bypassDoNotDisturb ?: group?.bypassDoNotDisturb ?: defaults.bypassDoNotDisturb,
  wakeScreen = wakeScreen ?: group?.wakeScreen ?: defaults.wakeScreen,
  lockScreenVisibility = lockScreenVisibility ?: group?.lockScreenVisibility ?: defaults.lockScreenVisibility,
  remindBefore = remindBefore ?: group?.remindBefore ?: defaults.remindBefore
)
