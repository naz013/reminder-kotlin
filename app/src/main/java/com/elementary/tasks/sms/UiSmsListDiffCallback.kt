package com.elementary.tasks.sms

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.ui.sms.UiSmsList

@Deprecated("After S")
class UiSmsListDiffCallback : DiffUtil.ItemCallback<UiSmsList>() {
  override fun areContentsTheSame(oldItem: UiSmsList, newItem: UiSmsList) =
    oldItem == newItem

  override fun areItemsTheSame(oldItem: UiSmsList, newItem: UiSmsList) =
    oldItem.id == newItem.id
}
