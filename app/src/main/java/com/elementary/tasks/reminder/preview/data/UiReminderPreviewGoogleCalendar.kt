package com.elementary.tasks.reminder.preview.data

data class UiReminderPreviewGoogleCalendar(
  val data: UiCalendarEventList
) : UiReminderPreviewData() {
  override val viewType: UiReminderPreviewDataViewType = UiReminderPreviewDataViewType.CALENDAR
  override val itemId: String = data.localId
}
