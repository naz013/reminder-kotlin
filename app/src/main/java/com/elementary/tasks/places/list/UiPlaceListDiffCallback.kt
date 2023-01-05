package com.elementary.tasks.places.list

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.ui.place.UiPlaceList

class UiPlaceListDiffCallback : DiffUtil.ItemCallback<UiPlaceList>() {

  override fun areContentsTheSame(oldItem: UiPlaceList, newItem: UiPlaceList): Boolean {
    return oldItem == newItem
  }

  override fun areItemsTheSame(oldItem: UiPlaceList, newItem: UiPlaceList): Boolean {
    return oldItem.id == newItem.id
  }
}
