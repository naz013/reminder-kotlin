package com.elementary.tasks.reminder.preview.data

import com.elementary.tasks.core.data.ui.UiIcon
import com.elementary.tasks.core.data.ui.UiTextElement
import java.util.UUID

data class UiReminderPreviewElement(
  val icon: UiIcon? = null,
  val textElement: UiTextElement
) : UiReminderPreviewData() {
  override val viewType: UiReminderPreviewDataViewType = UiReminderPreviewDataViewType.TEXT_ELEMENT
  override val itemId: String = icon?.value?.toString() ?: UUID.randomUUID().toString()
}
