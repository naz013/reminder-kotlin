package com.elementary.tasks.reminder.lists.adapter

import androidx.recyclerview.widget.DiffUtil
import com.elementary.tasks.core.data.models.Reminder

class ReminderDiffCallback : DiffUtil.ItemCallback<Reminder>() {

    override fun areContentsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: Reminder, newItem: Reminder): Boolean {
        return oldItem.uuId == newItem.uuId
    }
}