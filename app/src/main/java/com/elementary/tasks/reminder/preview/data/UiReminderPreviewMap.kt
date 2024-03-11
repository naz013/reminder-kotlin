package com.elementary.tasks.reminder.preview.data

import com.elementary.tasks.core.data.ui.UiTextElement
import com.elementary.tasks.core.data.ui.reminder.UiReminderPlace
import java.util.UUID

data class UiReminderPreviewMap(
  val placesText: UiTextElement,
  val places: List<UiReminderPlace>
) : UiReminderPreviewData() {
  override val viewType: UiReminderPreviewDataViewType = UiReminderPreviewDataViewType.MAP
  override val itemId: String = UUID.randomUUID().toString()
}
