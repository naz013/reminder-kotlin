package com.elementary.tasks.calendar.dayview.day

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.birthdays.list.BirthdayHolder
import com.elementary.tasks.calendar.data.BirthdayEventModel
import com.elementary.tasks.calendar.data.EventModel
import com.elementary.tasks.calendar.data.EventModelDiffCallback
import com.elementary.tasks.calendar.data.ReminderEventModel
import com.elementary.tasks.core.data.ui.UiReminderListActive
import com.elementary.tasks.core.data.ui.UiReminderListActiveShop
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.home.scheduleview.viewholder.ScheduleReminderViewHolderCommon
import com.elementary.tasks.reminder.lists.adapter.ReminderViewHolder
import com.elementary.tasks.reminder.lists.adapter.ShoppingViewHolder

class DayEventsAdapter(
  private val isDark: Boolean,
  private val showMore: Boolean = true,
  private val eventListener: ActionsListener<EventModel>? = null
) : ListAdapter<EventModel, RecyclerView.ViewHolder>(EventModelDiffCallback) {

  private val reminderCommon = ScheduleReminderViewHolderCommon()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      0 -> ReminderViewHolder(
        parent,
        editable = false,
        showMore = showMore
      ) { view, i, listActions ->
        eventListener?.onAction(view, i, getItem(i), listActions)
      }

      1 -> ShoppingViewHolder(
        parent = parent,
        editable = false,
        showMore = showMore,
        isDark = isDark,
        scheduleReminderViewHolderCommon = reminderCommon
      ) { view, i, listActions ->
        eventListener?.onAction(view, i, getItem(i), listActions)
      }

      else -> BirthdayHolder(parent, showMore) { view, i, listActions ->
        eventListener?.onAction(view, i, getItem(i), listActions)
      }
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is BirthdayHolder -> {
        holder.setData((getItem(position) as BirthdayEventModel).model)
      }

      is ReminderViewHolder -> {
        holder.setData((getItem(position) as ReminderEventModel).model as UiReminderListActive)
      }

      is ShoppingViewHolder -> {
        holder.setData((getItem(position) as ReminderEventModel).model as UiReminderListActiveShop)
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    val viewType = getItem(position).viewType
    return if (viewType == 0) {
      val item = getItem(position)
      if (item is ReminderEventModel && item.model is UiReminderListActiveShop) {
        1
      } else {
        0
      }
    } else {
      viewType
    }
  }
}
