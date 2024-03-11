package com.elementary.tasks.reminder.preview.data

sealed class UiReminderPreviewData {
  abstract val viewType: UiReminderPreviewDataViewType
  abstract val itemId: String
}

enum class UiReminderPreviewDataViewType {
  HEADER,
  STATUS,
  TEXT_ELEMENT,
  SUBTASK_ITEM,
  MAP,
  NOTE,
  GOOGLE_TASK,
  CALENDAR,
  ADS,
  ATTACHMENT
}
