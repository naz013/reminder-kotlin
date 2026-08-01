package com.elementary.tasks.groups

import android.content.Context
import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.formatter.CategoryFormatter
import com.elementary.tasks.reminder.build.formatter.DelayMinutesFormatter
import com.elementary.tasks.reminder.build.formatter.LockScreenVisibilityFormatter
import com.elementary.tasks.reminder.build.formatter.PriorityFormatter
import com.elementary.tasks.reminder.build.formatter.VibrationPatternFormatter
import com.github.naz013.common.ContextProvider
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.repository.ReminderSettingsRepository

data class NotificationOverrideSubtitles(
  val vibrate: String = "",
  val repeatNotification: String = "",
  val bypassDnd: String = "",
  val wakeScreen: String = "",
  val priority: String = "",
  val category: String = "",
  val lockScreenVisibility: String = "",
  val vibrationPattern: String = "",
  val delayMinutes: String = "",
)

/**
 * Formats a group/reminder [NotificationSettingsOverride] into per-field subtitles that read
 * either as the overridden value or as "Inherited: <effective settings value>". Shared by
 * [com.elementary.tasks.groups.create.EditGroupViewModel] and
 * [com.elementary.tasks.groups.details.GroupDetailsViewModel] so the "applied" wording stays
 * identical between the editable and read-only screens.
 */
class NotificationOverrideSubtitleFormatter(
  private val contextProvider: ContextProvider,
  private val reminderSettingsRepository: ReminderSettingsRepository,
) {

  fun format(notification: NotificationSettingsOverride): NotificationOverrideSubtitles {
    val context = contextProvider.themedContext
    val defaults = reminderSettingsRepository.getNotificationDefaults()
    return NotificationOverrideSubtitles(
      vibrate = boolSubtitle(context, notification.vibrate, defaults.vibrate),
      repeatNotification = boolSubtitle(context, notification.repeatNotification, defaults.repeatNotification),
      bypassDnd = boolSubtitle(context, notification.bypassDoNotDisturb, defaults.bypassDoNotDisturb),
      wakeScreen = boolSubtitle(context, notification.wakeScreen, defaults.wakeScreen),
      priority = notification.priority?.let { PriorityFormatter(context).format(it.ordinal) }
        ?: inherited(context, PriorityFormatter(context).format(defaults.priority.ordinal)),
      category = notification.category?.let { CategoryFormatter(context).format(it.ordinal) }
        ?: inherited(context, CategoryFormatter(context).format(defaults.category.ordinal)),
      lockScreenVisibility = notification.lockScreenVisibility?.let {
        LockScreenVisibilityFormatter(context).format(it.ordinal)
      } ?: inherited(context, LockScreenVisibilityFormatter(context).format(defaults.lockScreenVisibility.ordinal)),
      vibrationPattern = notification.vibrationPattern?.let { VibrationPatternFormatter(context).format(it) }
        ?: inherited(context, VibrationPatternFormatter(context).format(defaults.vibrationPattern.orEmpty())),
      delayMinutes = notification.delayMinutes?.let { DelayMinutesFormatter(context).format(it) }
        ?: inherited(context, DelayMinutesFormatter(context).format(defaults.delayMinutes)),
    )
  }

  private fun boolSubtitle(context: Context, value: Boolean?, effective: Boolean): String {
    val label = context.getString(if (value ?: effective) R.string.on else R.string.off)
    return if (value != null) label else inherited(context, label)
  }

  private fun inherited(context: Context, value: String): String =
    context.getString(R.string.inherited_format, value)
}
