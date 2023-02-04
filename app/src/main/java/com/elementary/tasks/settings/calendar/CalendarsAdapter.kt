package com.elementary.tasks.settings.calendar

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.core.binding.HolderBinding
import com.elementary.tasks.core.utils.GoogleCalendarUtils
import com.elementary.tasks.core.utils.inflater
import com.elementary.tasks.databinding.ListItemCalendarBinding

class CalendarsAdapter : RecyclerView.Adapter<CalendarsAdapter.ViewHolder>() {

  var data: List<GoogleCalendarUtils.CalendarItem> = listOf()
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

  fun getSelectedIds() = data.filter { it.isSelected }.map { it.id }.toTypedArray()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(data[position])
  }

  override fun getItemCount() = data.size

  inner class ViewHolder(
    parent: ViewGroup
  ) : HolderBinding<ListItemCalendarBinding>(
    ListItemCalendarBinding.inflate(parent.inflater(), parent, false)
  ) {

    init {
      binding.root.setOnClickListener {
        data[bindingAdapterPosition].isSelected = !data[bindingAdapterPosition].isSelected
        notifyItemChanged(bindingAdapterPosition)
      }
    }

    fun bind(item: GoogleCalendarUtils.CalendarItem) {
      binding.checkView.isChecked = item.isSelected
      binding.textView.text = item.name
    }
  }
}