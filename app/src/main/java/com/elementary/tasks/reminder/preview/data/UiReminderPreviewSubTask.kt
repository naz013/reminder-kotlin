package com.elementary.tasks.reminder.preview.data

import com.elementary.tasks.core.data.ui.UiTextElement

data class UiReminderPreviewSubTask(
  val id: String,
  val textElement: UiTextElement,
  val isChecked: Boolean
) : UiReminderPreviewData() {
  override val viewType: UiReminderPreviewDataViewType = UiReminderPreviewDataViewType.SUBTASK_ITEM
  override val itemId: String = id
}
