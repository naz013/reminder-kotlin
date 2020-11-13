package com.elementary.tasks.birthdays.list

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.models.Birthday

class BirthdayDiffCallback : DiffUtil.ItemCallback<Birthday>() {

  override fun areContentsTheSame(oldItem: Birthday, newItem: Birthday): Boolean {
    return oldItem == newItem
  }

  override fun areItemsTheSame(oldItem: Birthday, newItem: Birthday): Boolean {
    return oldItem.uuId == newItem.uuId
  }
}