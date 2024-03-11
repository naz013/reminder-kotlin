package com.elementary.tasks.reminder.preview.data

import com.elementary.tasks.core.data.ui.UiTextElement

data class UiReminderPreviewHeader(
  val textElement: UiTextElement
) : UiReminderPreviewData() {
  override val viewType: UiReminderPreviewDataViewType = UiReminderPreviewDataViewType.HEADER
  override val itemId: String = textElement.text
}
