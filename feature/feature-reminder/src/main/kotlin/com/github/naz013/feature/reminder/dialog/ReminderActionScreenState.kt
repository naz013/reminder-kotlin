package com.github.naz013.feature.reminder.dialog

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.github.naz013.feature.reminder.actions.ReminderAction

internal data class ReminderActionScreenState(
  val id: String,
  val header: ReminderActionScreenHeader,
  val todoList: ReminderActionScreenTodoList?,
  val mainAction: ReminderActionScreenActionItem,
  val secondaryActions: List<ReminderActionScreenActionItem>,
)

internal data class ReminderActionScreenActionItem(
  val action: ReminderAction,
  val text: String,
  val iconRes: Int,
)

internal data class ReminderActionScreenTodoList(
  val items: List<ReminderActionScreenTodoItem>,
)

internal data class ReminderActionScreenTodoItem(
  val id: String,
  val text: String,
  val isCompleted: Boolean,
)

internal sealed class ReminderActionScreenHeader {
  data class SimpleWithSummary(
    val text: String,
  ) : ReminderActionScreenHeader()

  data class MakeCall(
    val text: String,
    val phoneNumber: String,
    val contactName: String?,
    val contactPhoto: Bitmap?,
  ) : ReminderActionScreenHeader()

  data class SendSms(
    val text: String,
    val phoneNumber: String,
    val contactName: String?,
    val contactPhoto: Bitmap?,
  ) : ReminderActionScreenHeader()

  data class SendEmail(
    val text: String,
    val emailAddress: String,
    val contactName: String?,
    val subject: String?,
    val contactPhoto: Bitmap?,
  ) : ReminderActionScreenHeader()

  data class OpenApplication(
    val text: String,
    val appName: String,
    val appIcon: Drawable?,
  ) : ReminderActionScreenHeader()

  data class OpenLink(
    val text: String,
    val url: String,
  ) : ReminderActionScreenHeader()
}
