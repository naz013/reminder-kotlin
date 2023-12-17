package com.elementary.tasks.calendar.dayview.day

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.birthdays.list.BirthdayHolder
import com.elementary.tasks.calendar.data.BirthdayEventModel
import com.elementary.tasks.calendar.data.EventModel
import com.elementary.tasks.calendar.data.ReminderEventModel
import com.elementary.tasks.core.data.ui.UiReminderListActive
import com.elementary.tasks.core.data.ui.UiReminderListActiveShop
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.home.scheduleview.viewholder.ScheduleReminderViewHolderCommon
import com.elementary.tasks.reminder.lists.adapter.ReminderViewHolder
import com.elementary.tasks.reminder.lists.adapter.ShoppingViewHolder

class DayEventsAdapter(
  private val isDark: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val reminderCommon = ScheduleReminderViewHolderCommon()
  private var data: List<EventModel> = ArrayList()
  private var mEventListener: ActionsListener<EventModel>? = null
  var showMore: Boolean = true

  fun setData(data: List<EventModel>) {
    this.data = data
    notifyDataSetChanged()
  }

  fun setEventListener(listener: ActionsListener<EventModel>?) {
    this.mEventListener = listener
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
      0 -> ReminderViewHolder(
        parent,
        editable = false,
        showMore = showMore
      ) { view, i, listActions ->
        mEventListener?.onAction(view, i, data[i], listActions)
      }

      1 -> ShoppingViewHolder(
        parent = parent,
        editable = false,
        showMore = showMore,
        isDark = isDark,
        scheduleReminderViewHolderCommon = reminderCommon
      ) { view, i, listActions ->
        mEventListener?.onAction(view, i, data[i], listActions)
      }

      else -> BirthdayHolder(parent, showMore) { view, i, listActions ->
        mEventListener?.onAction(view, i, data[i], listActions)
      }
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is BirthdayHolder -> {
        holder.setData((data[position] as BirthdayEventModel).model)
      }

      is ReminderViewHolder -> {
        holder.setData((data[position] as ReminderEventModel).model as UiReminderListActive)
      }

      is ShoppingViewHolder -> {
        holder.setData((data[position] as ReminderEventModel).model as UiReminderListActiveShop)
      }
    }
  }

  override fun getItemCount() = data.size

  override fun getItemViewType(position: Int) = data[position].viewType

  fun getItem(position: Int) = data[position]
}
