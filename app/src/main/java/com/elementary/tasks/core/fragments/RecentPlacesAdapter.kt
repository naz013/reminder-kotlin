package com.elementary.tasks.core.fragments

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseViewHolder
import com.elementary.tasks.core.arch.CurrentStateHolder
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.core.utils.ui.DrawableHelper
import com.elementary.tasks.databinding.ListItemMapPlaceBinding

class RecentPlacesAdapter(
  private val currentStateHolder: CurrentStateHolder,
  private val dateTimeManager: DateTimeManager
) : RecyclerView.Adapter<RecentPlacesAdapter.ViewHolder>() {

  private val mData = mutableListOf<Place>()
  var actionsListener: ActionsListener<Place>? = null

  var data: List<Place>
    get() = mData
    set(list) {
      this.mData.clear()
      this.mData.addAll(list)
      notifyDataSetChanged()
    }

  override fun getItemCount(): Int {
    return mData.size
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    ViewHolder(parent, currentStateHolder)

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(holder.getItem(position))
  }


  inner class ViewHolder(
    parent: ViewGroup,
    currentStateHolder: CurrentStateHolder
  ) : BaseViewHolder<ListItemMapPlaceBinding>(
    ListItemMapPlaceBinding.inflate(parent.inflater(), parent, false),
    currentStateHolder
  ) {
    fun bind(item: Place) {
      binding.textView.text = item.name

      val date = dateTimeManager.getPlaceDateTimeFromGmt(item.dateTime)
      binding.dayView.text = date.day
      binding.monthYearView.text = "${date.month}\n${date.year}"

      DrawableHelper.withContext(itemView.context)
        .withDrawable(R.drawable.ic_twotone_place_24px)
        .withColor(theme.getMarkerLightColor(item.marker))
        .tint()
        .applyTo(binding.markerImage)

      binding.itemCard.setOnClickListener { view ->
        actionsListener?.onAction(
          view,
          bindingAdapterPosition,
          getItem(bindingAdapterPosition),
          ListActions.OPEN
        )
      }
    }

    fun getItem(position: Int) = mData[position]
  }
}
