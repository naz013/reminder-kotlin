package com.elementary.tasks.calendar.dayview.weekheader

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.databinding.ListItemDayViewWeekdayGridBinding

class WeekAdapter(
  private val onItemClickListener: (WeekDay) -> Unit
) : ListAdapter<WeekDay, WeekDayHolder>(
  WeekDayDiffCallback()
) {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekDayHolder {
    return WeekDayHolder(
      parent = parent,
      clickListener = { position ->
        getItem(position).takeIf { !it.isSelected }
          ?.also { onItemClickListener(it) }
      }
    )
  }

  override fun onBindViewHolder(holder: WeekDayHolder, position: Int) {
    holder.bind(getItem(position))
  }
}

class WeekDayHolder(
  parent: ViewGroup,
  private val clickListener: (Int) -> Unit,
  private val binding: ListItemDayViewWeekdayGridBinding =
    ListItemDayViewWeekdayGridBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false
    )
) : RecyclerView.ViewHolder(binding.root) {

  init {
    binding.root.setOnClickListener { clickListener(bindingAdapterPosition) }
  }

  fun bind(weekDay: WeekDay) {
    binding.dayTextView.text = weekDay.date
    binding.weekdayTextView.text = weekDay.weekday.uppercase()
    binding.eventIndicator.isVisible = weekDay.hasEvents
    if (weekDay.isSelected) {
      binding.root.setBackgroundResource(R.drawable.weekday_selected_background)
    } else {
      binding.root.background = null
    }
  }
}

class WeekDayDiffCallback : DiffUtil.ItemCallback<WeekDay>() {
  override fun areItemsTheSame(oldItem: WeekDay, newItem: WeekDay): Boolean {
    return oldItem.localDate == newItem.localDate
  }

  override fun areContentsTheSame(oldItem: WeekDay, newItem: WeekDay): Boolean {
    return oldItem == newItem
  }
}
