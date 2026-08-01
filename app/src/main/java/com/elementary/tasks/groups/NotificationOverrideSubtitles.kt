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
  val vibrate: String? = null,
  val repeatNotification: String? = null,
  val bypassDnd: String? = null,
  val wakeScreen: String? = null,
  val priority: String? = null,
  val category: String? = null,
  val lockScreenVisibility: String? = null,
  val vibrationPattern: String? = null,
  val delayMinutes: String? = null,
) {
  val allDefault: Boolean
    get() {
      return vibrate == null && repeatNotification == null && bypassDnd == null && wakeScreen == null &&
        priority == null && category == null && lockScreenVisibility == null && vibrationPattern == null &&
        delayMinutes == null
    }
}

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

  fun format(
    notification: NotificationSettingsOverride,
    excludeDefault: Boolean = false,
  ): NotificationOverrideSubtitles {
    val context = contextProvider.themedContext
    val defaults = reminderSettingsRepository.getNotificationDefaults()
    return NotificationOverrideSubtitles(
      vibrate = boolSubtitle(context, notification.vibrate, defaults.vibrate)
        .takeIf { notification.vibrate != null || !excludeDefault },
      repeatNotification = boolSubtitle(context, notification.repeatNotification, defaults.repeatNotification)
        .takeIf { notification.repeatNotification != null || !excludeDefault },
      bypassDnd = boolSubtitle(context, notification.bypassDoNotDisturb, defaults.bypassDoNotDisturb)
        .takeIf { notification.bypassDoNotDisturb != null || !excludeDefault },
      wakeScreen = boolSubtitle(context, notification.wakeScreen, defaults.wakeScreen)
        .takeIf { notification.wakeScreen != null || !excludeDefault },
      priority = notification.priority?.let { PriorityFormatter(context).format(it.ordinal) }
        ?: inherited(context, PriorityFormatter(context).format(defaults.priority.ordinal))
          .takeIf { !excludeDefault },
      category = notification.category?.let { CategoryFormatter(context).format(it.ordinal) }
        ?: inherited(context, CategoryFormatter(context).format(defaults.category.ordinal))
          .takeIf { !excludeDefault },
      lockScreenVisibility = notification.lockScreenVisibility?.let {
        LockScreenVisibilityFormatter(context).format(it.ordinal)
      } ?: inherited(context, LockScreenVisibilityFormatter(context).format(defaults.lockScreenVisibility.ordinal))
        .takeIf { !excludeDefault },
      vibrationPattern = notification.vibrationPattern?.let { VibrationPatternFormatter(context).format(it) }
        ?: inherited(context, VibrationPatternFormatter(context).format(defaults.vibrationPattern.orEmpty()))
          .takeIf { !excludeDefault },
      delayMinutes = notification.delayMinutes?.let { DelayMinutesFormatter(context).format(it) }
        ?: inherited(context, DelayMinutesFormatter(context).format(defaults.delayMinutes))
          .takeIf { !excludeDefault },
    )
  }

  private fun boolSubtitle(context: Context, value: Boolean?, effective: Boolean): String {
    val label = context.getString(if (value ?: effective) R.string.on else R.string.off)
    return if (value != null) label else inherited(context, label)
  }

  private fun inherited(context: Context, value: String): String =
    context.getString(R.string.inherited_format, value)
}
