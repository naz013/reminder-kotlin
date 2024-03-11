package com.elementary.tasks.reminder.preview.data

import com.elementary.tasks.core.data.ui.google.UiGoogleTaskList

data class UiReminderPreviewGoogleTask(
  val googleTask: UiGoogleTaskList
) : UiReminderPreviewData() {
  override val viewType: UiReminderPreviewDataViewType = UiReminderPreviewDataViewType.GOOGLE_TASK
  override val itemId: String = googleTask.id
}
