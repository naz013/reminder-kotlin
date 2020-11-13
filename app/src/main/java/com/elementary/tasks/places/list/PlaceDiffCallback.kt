package com.elementary.tasks.places.list

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.models.Place

class PlaceDiffCallback : DiffUtil.ItemCallback<Place>() {

  override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
    return oldItem == newItem
  }

  override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
    return oldItem.id == newItem.id
  }
}