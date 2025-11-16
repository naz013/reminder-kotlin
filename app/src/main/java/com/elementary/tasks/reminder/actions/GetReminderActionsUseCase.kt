package com.elementary.tasks.reminder.actions

import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger

class GetReminderActionsUseCase {

  suspend operator fun invoke(
    reminder: Reminder,
    supportedActions: Set<ReminderAction>
  ): List<ReminderAction> {
    val type = reminder.readType()
    return supportedActions.filter { action ->
      when (action) {
        ReminderAction.Complete -> reminder.isActive && !reminder.isRemoved
        ReminderAction.Snooze -> reminder.isActive && !reminder.isRemoved
        ReminderAction.SnoozeCustom -> reminder.isActive && !reminder.isRemoved
        ReminderAction.Dismiss -> reminder.isActive && !reminder.isRemoved
        ReminderAction.Edit -> true
        ReminderAction.Open -> !reminder.isRemoved
        ReminderAction.MoveToArchive -> !reminder.isRemoved
        ReminderAction.Delete -> true
        ReminderAction.MakeCall -> type.hasCallAction()
        ReminderAction.SendSms -> type.hasSmsAction()
        ReminderAction.SendEmail -> type.hasEmailAction()
        ReminderAction.OpenApp -> type.hasApplicationAction()
        ReminderAction.OpenUrl -> type.hasLinkAction()
        ReminderAction.ShowNotification -> true
      }
    }.also {
      Logger.i(TAG, "Available actions for reminder ${reminder.uuId}: $it")
    }
  }

  companion object {
    private const val TAG = "GetReminderActionsUseCase"
  }
}
