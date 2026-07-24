package com.elementary.tasks.eventaction

import com.elementary.tasks.core.utils.PhoneNumberValidator
import com.github.naz013.domain.Reminder
import com.github.naz013.logging.Logger
import java.io.File

class ResolveReminderEventActionUseCase {
  operator fun invoke(reminder: Reminder): ResolvedEventAction? {
    Logger.i(TAG, "Resolving event action for reminder: $reminder")
    return when {
      reminder.readType().hasSmsAction() -> {
        if (reminder.summary.isEmpty()) {
          null
        } else {
          ResolvedEventAction.SendSms(
            phoneNumber = reminder.to,
            message = reminder.summary,
          )
        }
      }
      isAppType(reminder) -> {
        if (Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)) {
          ResolvedEventAction.OpenApp(reminder.target)
        } else {
          ResolvedEventAction.OpenLink(reminder.target)
        }
      }
      Reminder.isSame(reminder.type, Reminder.BY_DATE_EMAIL) -> {
        val file = File(reminder.attachmentFile).takeIf { it.exists() && it.canRead() }
        ResolvedEventAction.SendEmail(
          email = reminder.to,
          subject = reminder.subject,
          body = reminder.summary,
          attachmentFile = file,
        )
      }
      else -> {
        if (PhoneNumberValidator.isPhoneNumber(reminder.target)) {
          ResolvedEventAction.MakeCall(reminder.target)
        } else {
          null
        }
      }
    }
  }

  private fun isAppType(reminder: Reminder): Boolean =
    Reminder.isSame(reminder.type, Reminder.BY_DATE_LINK) ||
      Reminder.isSame(reminder.type, Reminder.BY_DATE_APP)

  companion object {
    private const val TAG = "ResolveReminderEventActionUseCase"
  }
}
