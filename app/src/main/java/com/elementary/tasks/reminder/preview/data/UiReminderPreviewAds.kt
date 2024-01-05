package com.elementary.tasks.reminder.preview.data

import java.util.UUID

data object UiReminderPreviewAds : UiReminderPreviewData() {
  override val viewType: UiReminderPreviewDataViewType = UiReminderPreviewDataViewType.ADS
  override val itemId: String = UUID.randomUUID().toString()
}
