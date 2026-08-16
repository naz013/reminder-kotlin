package com.github.naz013.feature.reminder.actions

import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.domain.reminder.v2.ReminderAction as DomainReminderAction
import com.github.naz013.logging.Logger

class GetReminderActionsUseCase {
  suspend operator fun invoke(
    reminder: ReminderV2,
    supportedActions: Set<ReminderAction>,
  ): List<ReminderAction> {
    val action = reminder.action
    return supportedActions
      .filter { supported ->
        when (supported) {
          ReminderAction.Complete -> reminder.isActive && !reminder.isRemoved
          ReminderAction.Snooze -> reminder.isActive && !reminder.isRemoved
          ReminderAction.SnoozeCustom -> reminder.isActive && !reminder.isRemoved
          ReminderAction.Dismiss -> reminder.isActive && !reminder.isRemoved
          ReminderAction.Edit -> true
          ReminderAction.Open -> !reminder.isRemoved
          ReminderAction.MoveToArchive -> !reminder.isRemoved
          ReminderAction.Delete -> true
          ReminderAction.MakeCall -> action is DomainReminderAction.Call
          ReminderAction.SendSms -> action is DomainReminderAction.Sms
          ReminderAction.SendEmail -> action is DomainReminderAction.Email
          ReminderAction.OpenApp -> action is DomainReminderAction.App
          ReminderAction.OpenUrl -> action is DomainReminderAction.Link
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
