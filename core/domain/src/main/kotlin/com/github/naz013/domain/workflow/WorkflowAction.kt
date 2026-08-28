package com.github.naz013.domain.workflow

import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.google.gson.annotations.SerializedName

/** Variants with a payload are Gson round-tripped directly (see `WorkflowTriggerActionCodec`), so
 * every field needs [SerializedName] - see [com.github.naz013.domain.reminder.v2.RecurrenceRule]
 * for why an unannotated field is a production-crash risk under R8. */
sealed class WorkflowAction {
  /** Powers the auto-archive workflow. */
  data object ArchiveReminder : WorkflowAction()

  data object CompleteReminder : WorkflowAction()

  /** Hard delete, powering the auto-purge workflow - unlike [ArchiveReminder], this is not
   * reversible from the UI once applied. */
  data object PurgeReminder : WorkflowAction()

  data class ApplyNotificationOverride(
    @SerializedName("override")
    val override: NotificationSettingsOverride
  ) : WorkflowAction()

  /** Symmetric counterpart to [ApplyNotificationOverride] - resets to an all-default
   * [NotificationSettingsOverride], letting scope fall back through the normal reminder→group→
   * global override hierarchy. Powers the "revert on end date" half of vacation-mode rules. */
  data object ClearNotificationOverride : WorkflowAction()

  /** Chained/dependent reminders: activates another reminder by id. */
  data class ActivateReminder(
    @SerializedName("reminderId")
    val reminderId: String
  ) : WorkflowAction()

  /** Reassigns the reminder's group. Powers keyword-based auto-grouping paired with
   * [WorkflowTrigger.ReminderCreated] and [WorkflowCondition.TitleContains]. */
  data class MoveToGroup(
    @SerializedName("groupId")
    val groupId: String
  ) : WorkflowAction()

  /** Escape hatch mirroring ResolvedEventAction's open-endedness: runs an existing BackgroundTask. */
  data class RunBackgroundTask(
    @SerializedName("taskKey")
    val taskKey: String
  ) : WorkflowAction()
}
