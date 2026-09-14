package com.github.naz013.domain.workflow

import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDateTime

/** Variants with a payload are Gson round-tripped directly (see `WorkflowTriggerActionCodec`), so
 * every field needs [SerializedName] - see [com.github.naz013.domain.reminder.v2.RecurrenceRule]
 * for why an unannotated field is a production-crash risk under R8. */
sealed class WorkflowTrigger {
  /** Fires the first time a reminder is ever saved - see `SaveReminderUseCase` for how "first
   * time" is detected. Powers keyword-based auto-grouping paired with [WorkflowCondition.TitleContains]
   * and [WorkflowAction.MoveToGroup]. */
  data object ReminderCreated : WorkflowTrigger()

  data object ReminderCompleted : WorkflowTrigger()

  data class ReminderSnoozedNTimes(
    @SerializedName("count")
    val count: Int
  ) : WorkflowTrigger()

  data object GroupAllCompleted : WorkflowTrigger()

  data object LocationEntered : WorkflowTrigger()

  data object LocationExited : WorkflowTrigger()

  /** Powers the auto-archive workflow: fires for completed reminders older than [days]. */
  data class ReminderAgeExceeded(
    @SerializedName("days")
    val days: Int
  ) : WorkflowTrigger()

  data class ReminderUnacknowledgedFor(
    @SerializedName("minutes")
    val minutes: Int
  ) : WorkflowTrigger()

  /** An absolute wall-clock trigger, not tied to any reminder's own state - see
   * `WorkflowEngine.runScheduleRules` for how it's evaluated and why it currently only supports
   * [WorkflowAction.RunBackgroundTask]. */
  data class ScheduleReached(
    @SerializedName("atDateTime")
    val atDateTime: LocalDateTime,
    @SerializedName("recurrence")
    val recurrence: ScheduleRecurrence = ScheduleRecurrence.ONCE
  ) : WorkflowTrigger()

  /** Fires when [deviceAddress] (a paired device's stable MAC) establishes an ACL connection -
   * like [ScheduleReached], not tied to any reminder's own state, so the action bulk-applies to
   * every reminder in [WorkflowRule.scope] rather than to "the reminder that matched". [deviceName]
   * is captured at rule-creation time for display only - matching is by [deviceAddress]. */
  data class BluetoothConnected(
    @SerializedName("deviceAddress")
    val deviceAddress: String,
    @SerializedName("deviceName")
    val deviceName: String
  ) : WorkflowTrigger()

  /** Symmetric counterpart to [BluetoothConnected] - fires when [deviceAddress] disconnects. */
  data class BluetoothDisconnected(
    @SerializedName("deviceAddress")
    val deviceAddress: String,
    @SerializedName("deviceName")
    val deviceName: String
  ) : WorkflowTrigger()

  /** Fires when the device associates with WiFi network [ssid] - same not-reminder-scoped shape
   * as [BluetoothConnected]. [ssid] is compared with surrounding quotes already stripped (see
   * `WifiConnectionMonitor` in the `app` module for where those quotes come from). */
  data class WifiConnected(
    @SerializedName("ssid")
    val ssid: String
  ) : WorkflowTrigger()

  /** Symmetric counterpart to [WifiConnected] - fires when [ssid] is lost/disassociated. */
  data class WifiDisconnected(
    @SerializedName("ssid")
    val ssid: String
  ) : WorkflowTrigger()
}
