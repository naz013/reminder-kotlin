package com.github.naz013.domain.reminder.v2

import com.google.gson.annotations.SerializedName

/**
 * The override shape held by [GroupV2] and [ReminderV2]: every field is nullable, where
 * `null` means "inherit from the next level down" (Reminder -> Group -> Settings).
 *
 * Also embedded directly in [com.github.naz013.domain.workflow.WorkflowAction.ApplyNotificationOverride]
 * and Gson round-tripped there (see `WorkflowTriggerActionCodec`), so every field needs
 * [SerializedName] - see [RecurrenceRule] for why an unannotated field is a production-crash risk
 * under R8.
 */
data class NotificationSettingsOverride(
  @SerializedName("color")
  val color: Int? = null,
  @SerializedName("vibrate")
  val vibrate: Boolean? = null,
  @SerializedName("vibrationPattern")
  val vibrationPattern: List<Long>? = null,
  @SerializedName("repeatNotification")
  val repeatNotification: Boolean? = null,
  @SerializedName("volume")
  val volume: Int? = null,
  @SerializedName("soundUri")
  val soundUri: String? = null,
  @SerializedName("quietHoursFrom")
  val quietHoursFrom: String? = null,
  @SerializedName("quietHoursTo")
  val quietHoursTo: String? = null,
  @SerializedName("activeHours")
  val activeHours: List<Int>? = null,
  @SerializedName("delayMinutes")
  val delayMinutes: Int? = null,
  @SerializedName("priority")
  val priority: ReminderPriority? = null,
  @SerializedName("category")
  val category: ReminderNotificationCategory? = null,
  @SerializedName("bypassDoNotDisturb")
  val bypassDoNotDisturb: Boolean? = null,
  @SerializedName("wakeScreen")
  val wakeScreen: Boolean? = null,
  @SerializedName("lockScreenVisibility")
  val lockScreenVisibility: LockScreenVisibility? = null,
  @SerializedName("remindBefore")
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
