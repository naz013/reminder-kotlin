package com.elementary.tasks.navigation.settings.calendar

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.utils.CalendarUtils
import com.elementary.tasks.databinding.ListItemCalendarBinding

class CalendarsAdapter : RecyclerView.Adapter<CalendarsAdapter.ViewHolder>() {

  var data: List<CalendarUtils.CalendarItem> = listOf()
    set(list) {
      field = list
      notifyDataSetChanged()
    }

  fun selectIds(array: Array<Long>) {
    if (array.isEmpty()) {
      data.forEach { it.isSelected = false }
      notifyDataSetChanged()
      return
    }
    data.forEach {
      if (array.contains(it.id)) {
        it.isSelected = true
      }
    }
    notifyDataSetChanged()
  }

  fun getSelectedIds(): Array<Long> {
    return data.filter { it.isSelected }.map { it.id }.toTypedArray()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder(parent)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(data[position])
  }

  override fun getItemCount(): Int {
    return data.size
  }

  inner class ViewHolder(
    parent: ViewGroup
  ) : HolderBinding<ListItemCalendarBinding>(parent, R.layout.list_item_calendar) {

    init {
      binding.root.setOnClickListener {
        data[adapterPosition].isSelected = !data[adapterPosition].isSelected
        notifyItemChanged(adapterPosition)
      }
    }

    fun bind(item: CalendarUtils.CalendarItem) {
      binding.itemCheck.isChecked = item.isSelected
      binding.shopText.text = item.name
    }
  }
}