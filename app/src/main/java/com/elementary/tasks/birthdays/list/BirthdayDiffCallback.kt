package com.elementary.tasks.birthdays.list

import androidx.recyclerview.widget.DiffUtil

class BirthdayDiffCallback : DiffUtil.ItemCallback<BirthdayListItem>() {
  override fun areContentsTheSame(oldItem: BirthdayListItem, newItem: BirthdayListItem) =
    oldItem == newItem

  override fun areItemsTheSame(oldItem: BirthdayListItem, newItem: BirthdayListItem) =
    oldItem.uuId == newItem.uuId
}