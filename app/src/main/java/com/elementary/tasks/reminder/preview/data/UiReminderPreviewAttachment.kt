package com.elementary.tasks.reminder.preview.data

import com.elementary.tasks.core.data.ui.UiTextElement
import com.elementary.tasks.reminder.build.valuedialog.controller.attachments.AttachmentFile

data class UiReminderPreviewAttachment(
  val file: AttachmentFile,
  val text: UiTextElement
) : UiReminderPreviewData() {
  override val itemId: String = file.name + file.uri.toString()
  override val viewType: UiReminderPreviewDataViewType = UiReminderPreviewDataViewType.ATTACHMENT
}
