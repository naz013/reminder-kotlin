package com.elementary.tasks.simplemap

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.ui.place.UiPlaceList
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ui.inflater
import com.elementary.tasks.core.utils.ui.visibleGone
import com.elementary.tasks.databinding.ListItemMapPlaceBinding
import com.elementary.tasks.places.list.UiPlaceListDiffCallback

class RecentPlacesAdapter :
  ListAdapter<UiPlaceList, RecentPlacesAdapter.ViewHolder>(UiPlaceListDiffCallback()) {

  var actionsListener: ActionsListener<UiPlaceList>? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent)

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  inner class ViewHolder(
    parent: ViewGroup
  ) : HolderBinding<ListItemMapPlaceBinding>(
    ListItemMapPlaceBinding.inflate(parent.inflater(), parent, false)
  ) {
    fun bind(item: UiPlaceList) {
      binding.textView.text = item.name

      binding.dateView.visibleGone(item.formattedDate != null)
      binding.dateView.text = item.formattedDate

      binding.markerImage.setImageDrawable(item.marker)

      binding.itemCard.setOnClickListener { view ->
        actionsListener?.onAction(
          view,
          bindingAdapterPosition,
          getItem(bindingAdapterPosition),
          ListActions.OPEN
        )
      }
    }
  }
}
