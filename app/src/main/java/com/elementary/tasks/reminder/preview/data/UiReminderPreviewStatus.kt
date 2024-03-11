package com.elementary.tasks.reminder.preview.data

import com.elementary.tasks.core.data.ui.UiTextElement
import com.elementary.tasks.core.data.ui.reminder.UiReminderStatus

data class UiReminderPreviewStatus(
  val id: String,
  val status: UiReminderStatus,
  val statusText: UiTextElement
) : UiReminderPreviewData() {
  override val itemId: String = id
  override val viewType: UiReminderPreviewDataViewType = UiReminderPreviewDataViewType.STATUS
}
