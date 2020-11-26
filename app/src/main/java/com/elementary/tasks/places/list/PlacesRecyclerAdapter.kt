package com.elementary.tasks.places.list

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseViewHolder
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.DrawableHelper
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemPlaceBinding

class PlacesRecyclerAdapter(
  private val currentStateHolder: CurrentStateHolder,
  private val actionsListener: ActionsListener<Place>? = null
) : ListAdapter<Place, PlacesRecyclerAdapter.ViewHolder>(PlaceDiffCallback()) {

  inner class ViewHolder(
    parent: ViewGroup
  ) : BaseViewHolder<ListItemPlaceBinding>(
    ListItemPlaceBinding.inflate(parent.inflater(), parent, false),
    currentStateHolder
  ) {
    fun bind(item: Place) {
      binding.textView.text = item.name
      DrawableHelper.withContext(itemView.context)
        .withDrawable(R.drawable.ic_twotone_place_24px)
        .withColor(theme.getNoteLightColor(item.marker))
        .tint()
        .applyTo(binding.markerImage)
    }

    init {
      binding.itemCard.setOnClickListener {
        actionsListener?.onAction(it, adapterPosition, getItem(adapterPosition), ListActions.OPEN)
      }
      binding.buttonMore.setOnClickListener {
        actionsListener?.onAction(it, adapterPosition, getItem(adapterPosition), ListActions.MORE)
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }
}
