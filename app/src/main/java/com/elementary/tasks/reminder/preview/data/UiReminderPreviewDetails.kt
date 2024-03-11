package com.elementary.tasks.reminder.preview.data

import com.elementary.tasks.core.data.ui.reminder.UiReminderStatus
import com.elementary.tasks.core.data.ui.reminder.UiReminderTarget
import com.elementary.tasks.core.data.ui.reminder.UiReminderType

data class UiReminderPreviewDetails(
  val id: String,
  val type: UiReminderType,
  val noteId: String,
  val actionTarget: UiReminderTarget?,
  val isRunning: Boolean,
  val status: UiReminderStatus
)
