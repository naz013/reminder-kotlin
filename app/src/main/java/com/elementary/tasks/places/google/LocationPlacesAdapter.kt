package com.elementary.tasks.places.google

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.core.utils.ui.DrawableHelper
import com.elementary.tasks.databinding.ListItemLocationBinding

class LocationPlacesAdapter(
  private val themeUtil: ThemeProvider
) : RecyclerView.Adapter<LocationPlacesAdapter.ViewHolder>() {

  private val dataList = ArrayList<Reminder>()
  var actionsListener: ActionsListener<Reminder>? = null

  fun setData(list: List<Reminder>) {
    this.dataList.clear()
    this.dataList.addAll(list)
    notifyDataSetChanged()
  }

  inner class ViewHolder(
    parent: ViewGroup
  ) : HolderBinding<ListItemLocationBinding>(
    ListItemLocationBinding.inflate(parent.inflater(), parent, false)
  ) {
    fun bind(item: Reminder) {
      val place = item.places[0]
      var name = place.name
      if (item.places.size > 1) {
        name = item.summary + " (" + item.places.size + ")"
      }
      binding.textView.text = name
      loadMarker(binding.markerImage, place.marker)
    }

    init {
      binding.itemCard.setOnClickListener { view ->
        actionsListener?.onAction(view, adapterPosition, getItem(adapterPosition), ListActions.OPEN)
      }
      binding.itemCard.setOnLongClickListener { view ->
        actionsListener?.onAction(view, adapterPosition, getItem(adapterPosition), ListActions.MORE)
        true
      }
    }
  }

  fun loadMarker(view: ImageView, color: Int) {
    DrawableHelper.withContext(view.context)
      .withDrawable(R.drawable.ic_twotone_place_24px)
      .withColor(themeUtil.getMarkerLightColor(color))
      .tint()
      .applyTo(view)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(dataList[position])
  }

  fun getItem(position: Int) = dataList[position]

  override fun getItemCount() = dataList.size
}
