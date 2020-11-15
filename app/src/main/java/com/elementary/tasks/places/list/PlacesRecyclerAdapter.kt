package com.elementary.tasks.places.list

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.DrawableHelper
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.databinding.ListItemPlaceBinding

class PlacesRecyclerAdapter(
  private val prefs: Prefs,
  private val themeUtil: ThemeUtil
) : ListAdapter<Place, PlacesRecyclerAdapter.ViewHolder>(PlaceDiffCallback()) {

  var actionsListener: ActionsListener<Place>? = null

  inner class ViewHolder(
    parent: ViewGroup
  ) : BaseHolder<ListItemPlaceBinding>(parent, R.layout.list_item_place, prefs) {
    fun bind(item: Place) {
      binding.textView.text = item.name
      DrawableHelper.withContext(itemView.context)
        .withDrawable(R.drawable.ic_twotone_place_24px)
        .withColor(themeUtil.getNoteLightColor(item.marker))
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

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(parent)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }
}
