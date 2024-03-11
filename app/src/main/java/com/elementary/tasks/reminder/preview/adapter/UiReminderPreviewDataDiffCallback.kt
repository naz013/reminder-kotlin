package com.elementary.tasks.reminder.preview.adapter

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.reminder.preview.data.UiReminderPreviewData

class UiReminderPreviewDataDiffCallback : DiffUtil.ItemCallback<UiReminderPreviewData>() {

  override fun areItemsTheSame(
    oldItem: UiReminderPreviewData,
    newItem: UiReminderPreviewData
  ): Boolean {
    return oldItem.itemId == newItem.itemId
  }

  override fun areContentsTheSame(
    oldItem: UiReminderPreviewData,
    newItem: UiReminderPreviewData
  ): Boolean {
    return oldItem == newItem
  }
}
