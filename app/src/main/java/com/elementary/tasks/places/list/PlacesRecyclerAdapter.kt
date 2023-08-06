package com.elementary.tasks.places.list

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.ui.place.UiPlaceList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemPlaceBinding

class PlacesRecyclerAdapter(
  private val actionsListener: ActionsListener<UiPlaceList>? = null
) : ListAdapter<UiPlaceList, PlacesRecyclerAdapter.ViewHolder>(UiPlaceListDiffCallback()) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  inner class ViewHolder(
    parent: ViewGroup
  ) : HolderBinding<ListItemPlaceBinding>(
    ListItemPlaceBinding.inflate(parent.inflater(), parent, false)
  ) {
    fun bind(item: UiPlaceList) {
      binding.textView.text = item.name
      binding.markerImage.setImageDrawable(item.marker)
    }

    init {
      binding.itemCard.setOnClickListener {
        actionsListener?.onAction(
          view = it,
          position = bindingAdapterPosition,
          t = getItem(bindingAdapterPosition),
          actions = ListActions.OPEN
        )
      }
      binding.buttonMore.setOnClickListener {
        actionsListener?.onAction(
          view = it,
          position = bindingAdapterPosition,
          t = getItem(bindingAdapterPosition),
          actions = ListActions.MORE
        )
      }
    }
  }
}
