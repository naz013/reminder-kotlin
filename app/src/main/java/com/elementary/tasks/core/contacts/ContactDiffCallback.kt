package com.elementary.tasks.core.contacts

import androidx.recyclerview.widget.DiffUtil

class ContactDiffCallback : DiffUtil.ItemCallback<ContactItem>() {

    override fun areContentsTheSame(oldItem: ContactItem, newItem: ContactItem): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: ContactItem, newItem: ContactItem): Boolean {
        return oldItem.id == newItem.id
    }
}