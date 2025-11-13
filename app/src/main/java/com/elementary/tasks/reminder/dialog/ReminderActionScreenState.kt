package com.elementary.tasks.reminder.dialog

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.elementary.tasks.reminder.actions.ReminderAction

data class ReminderActionScreenState(
  val id: String,
  val header: ReminderActionScreenHeader,
  val todoList: ReminderActionScreenTodoList?,
  val mainAction: ReminderActionScreenActionItem,
  val secondaryActions: List<ReminderActionScreenActionItem>
)

data class ReminderActionScreenActionItem(
  val action: ReminderAction,
  val text: String,
  val iconRes: Int
)

data class ReminderActionScreenTodoList(val items: List<ReminderActionScreenTodoItem>)

data class ReminderActionScreenTodoItem(
  val id: String,
  val text: String,
  val isCompleted: Boolean
)

sealed class ReminderActionScreenHeader {
  data class SimpleWithSummary(val text: String) : ReminderActionScreenHeader()
  data class MakeCall(
    val text: String,
    val phoneNumber: String,
    val contactName: String?,
    val contactPhoto: Bitmap?
  ) : ReminderActionScreenHeader()
  data class SendSms(
    val text: String,
    val phoneNumber: String,
    val contactName: String?,
    val contactPhoto: Bitmap?
  ) : ReminderActionScreenHeader()
  data class SendEmail(
    val text: String,
    val emailAddress: String,
    val contactName: String?,
    val subject: String?,
    val contactPhoto: Bitmap?
  ) : ReminderActionScreenHeader()
  data class OpenApplication(
    val text: String,
    val appName: String,
    val appIcon: Drawable?
  ) : ReminderActionScreenHeader()
  data class OpenLink(
    val text: String,
    val url: String
  ) : ReminderActionScreenHeader()
}
