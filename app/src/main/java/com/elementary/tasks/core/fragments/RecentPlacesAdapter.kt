package com.elementary.tasks.core.fragments

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.Place
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.DrawableHelper
import com.elementary.tasks.core.utils.ListActions
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.databinding.ListItemMapPlaceBinding

class RecentPlacesAdapter(
  private val themeUtil: ThemeUtil,
  private val prefs: Prefs
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

  inner class ViewHolder(
    parent: ViewGroup,
    prefs: Prefs
  ) : BaseHolder<ListItemMapPlaceBinding>(parent, R.layout.list_item_map_place, prefs) {
    fun bind(item: Place) {
      binding.textView.text = item.name

      val dmy = TimeUtil.getPlaceDateTimeFromGmt(item.dateTime, prefs.appLanguage)
      binding.dayView.text = dmy.day
      binding.monthYearView.text = "${dmy.month}\n${dmy.year}"

      DrawableHelper.withContext(itemView.context)
        .withDrawable(R.drawable.ic_twotone_place_24px)
        .withColor(themeUtil.getNoteLightColor(item.marker))
        .tint()
        .applyTo(binding.markerImage)
    }

    init {
      binding.itemCard.setOnClickListener { view ->
        actionsListener?.onAction(view, adapterPosition, getItem(adapterPosition), ListActions.OPEN)
      }
    }
  }

  fun getItem(position: Int) = mData[position]

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent, prefs)

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }
}
