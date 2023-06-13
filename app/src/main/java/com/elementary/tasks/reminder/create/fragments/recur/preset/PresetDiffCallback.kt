package com.elementary.tasks.reminder.create.fragments.recur.preset

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.ui.preset.UiPresetList
import java.util.Objects

class PresetDiffCallback : DiffUtil.ItemCallback<UiPresetList>() {

  override fun areItemsTheSame(oldItem: UiPresetList, newItem: UiPresetList): Boolean {
    return oldItem == newItem
  }

  override fun areContentsTheSame(oldItem: UiPresetList, newItem: UiPresetList): Boolean {
    return Objects.equals(oldItem, newItem)
  }
}
