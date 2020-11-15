package com.elementary.tasks.day_view.day

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.birthdays.list.BirthdayHolder
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.interfaces.ActionsListener
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.reminder.lists.adapter.ReminderHolder
import com.elementary.tasks.reminder.lists.adapter.ShoppingHolder
import java.util.*

class CalendarEventsAdapter(
  private val prefs: Prefs
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
      0 -> ReminderHolder(parent, prefs, hasHeader = false, editable = false, showMore = showMore) { view, i, listActions ->
        mEventListener?.onAction(view, i, data[i], listActions)
      }
      1 -> ShoppingHolder(parent, prefs, false, showMore) { view, i, listActions ->
        mEventListener?.onAction(view, i, data[i], listActions)
      }
      else -> BirthdayHolder(parent, prefs, showMore) { view, i, listActions ->
        mEventListener?.onAction(view, i, data[i], listActions)
      }
    }
  }

  override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
      is BirthdayHolder -> {
        holder.setData(data[position].model as Birthday)
      }
      is ReminderHolder -> {
        holder.setData(data[position].model as Reminder)
      }
      is ShoppingHolder -> {
        holder.setData(data[position].model as Reminder)
      }
    }
  }

  override fun getItemCount() = data.size

  override fun getItemViewType(position: Int) = data[position].viewType

  fun getItem(position: Int) = data[position]
}
