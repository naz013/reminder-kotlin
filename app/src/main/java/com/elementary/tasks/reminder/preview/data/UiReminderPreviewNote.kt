package com.elementary.tasks.reminder.preview.data

import com.elementary.tasks.core.data.ui.note.UiNoteList

data class UiReminderPreviewNote(
  val note: UiNoteList
) : UiReminderPreviewData() {
  override val viewType: UiReminderPreviewDataViewType = UiReminderPreviewDataViewType.NOTE
  override val itemId: String = note.id
}
