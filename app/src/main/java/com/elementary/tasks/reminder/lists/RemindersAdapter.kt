package com.elementary.tasks.reminder.lists

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.home.scheduleview.viewholder.ScheduleReminderViewHolderCommon
import com.elementary.tasks.reminder.lists.data.UiReminderEventsList
import com.elementary.tasks.reminder.lists.data.UiReminderList
import com.elementary.tasks.reminder.lists.data.UiReminderListHeader
import com.elementary.tasks.reminder.lists.viewholder.HeaderViewHolder
import com.elementary.tasks.reminder.lists.viewholder.ReminderViewHolder

class RemindersAdapter(
  private val isEditable: Boolean = true,
  private val reminderCommon: ScheduleReminderViewHolderCommon = ScheduleReminderViewHolderCommon(),
  private val onItemClicked: (UiReminderList) -> Unit = { },
  private val onToggleClicked: (UiReminderList) -> Unit = { },
  private val onMoreClicked: (View, UiReminderList) -> Unit = { _, _ -> }
) : ListAdapter<UiReminderEventsList, RecyclerView.ViewHolder>(
  UiReminderEventsListDiffCallback()
) {
  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): RecyclerView.ViewHolder {
    return when (viewType) {
      1 -> HeaderViewHolder(parent)

      else -> {
        ReminderViewHolder(
          parent = parent,
          editable = isEditable,
          showMore = true,
          common = reminderCommon,
          onItemClicked = { i: Int ->
            find(i)?.run {
              onItemClicked(this)
            }
          },
          onToggleClicked = { i: Int ->
            find(i)?.run {
              onToggleClicked(this)
            }
          },
          onMoreClicked = { view: View, i: Int ->
            find(i)?.run {
              onMoreClicked(view, this)
            }
          }
        )
      }
    }
  }

  private fun find(position: Int): UiReminderList? {
    if (position != -1 && position < itemCount) {
      return try {
        getItem(position).takeIf { it is UiReminderList }
          ?.let { it as? UiReminderList }
      } catch (e: Throwable) {
        null
      }
    }
    return null
  }

  override fun onBindViewHolder(
    holder: RecyclerView.ViewHolder,
    position: Int
  ) {
    when (holder) {
      is ReminderViewHolder -> {
        holder.bind(getItem(position) as UiReminderList)
      }

      is HeaderViewHolder -> {
        holder.bind(getItem(position) as UiReminderListHeader)
      }

      else -> {
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (getItem(position)) {
      is UiReminderListHeader -> 1
      else -> 0
    }
  }
}
