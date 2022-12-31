package com.elementary.tasks.birthdays.list

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.ui.UiBirthdayList

class UiBirthdayListDiffCallback : DiffUtil.ItemCallback<UiBirthdayList>() {
  override fun areContentsTheSame(oldItem: UiBirthdayList, newItem: UiBirthdayList) =
    oldItem == newItem

  override fun areItemsTheSame(oldItem: UiBirthdayList, newItem: UiBirthdayList) =
    oldItem.uuId == newItem.uuId
}
