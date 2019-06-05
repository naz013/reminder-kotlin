package com.elementary.tasks.core.contacts

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.core.utils.ThemeUtil
import org.koin.core.KoinComponent
import org.koin.core.inject

class ContactsRecyclerAdapter : ListAdapter<ContactItem, ContactHolder>(ContactDiffCallback()), KoinComponent {

    var clickListener: ((name: String, number: String) -> Unit)? = null

    private val themeUtil: ThemeUtil by inject()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        return ContactHolder(parent, themeUtil.isDark) { performClick(it) }
    }

    private fun performClick(it: Int) {
        val item = getItem(it)
        clickListener?.invoke(item.name, "")
    }

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
