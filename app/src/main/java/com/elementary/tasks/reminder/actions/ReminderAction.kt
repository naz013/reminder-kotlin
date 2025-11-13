package com.elementary.tasks.reminder.actions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.github.naz013.ui.common.R

enum class ReminderAction(
  @param:StringRes val titleRes: Int,
  @param:DrawableRes val iconRes: Int,
  val category: ActionCategory,
) {
  Open(R.string.action_open, R.drawable.ic_fluent_open, ActionCategory.Main),
  Complete(R.string.action_complete, R.drawable.ic_fluent_checkmark, ActionCategory.Main),
  Snooze(R.string.action_snooze, R.drawable.ic_fluent_alert_snooze, ActionCategory.Main),
  SnoozeCustom(R.string.action_snooze_custom, R.drawable.ic_fluent_snooze, ActionCategory.Secondary),
  Edit(R.string.action_edit, R.drawable.ic_fluent_edit, ActionCategory.Secondary),
  Dismiss(R.string.action_dismiss, R.drawable.ic_fluent_dismiss, ActionCategory.Secondary),

  MakeCall(R.string.make_call, R.drawable.ic_fluent_phone, ActionCategory.Action),
  SendSms(R.string.send_sms, R.drawable.ic_fluent_send, ActionCategory.Action),
  SendEmail(R.string.action_send_email, R.drawable.ic_fluent_send, ActionCategory.Action),
  OpenApp(R.string.open_app, R.drawable.ic_fluent_apps, ActionCategory.Action),
  OpenUrl(R.string.open_link, R.drawable.ic_fluent_globe, ActionCategory.Action),

  MoveToArchive(R.string.move_to_archive, R.drawable.ic_fluent_archive, ActionCategory.Secondary),
  Delete(R.string.action_delete, R.drawable.ic_fluent_delete, ActionCategory.Secondary),
}

enum class ActionCategory {
  Main,
  Secondary,
  Action
}
