package com.elementary.tasks.core.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeUtil
import javax.inject.Inject

/**
 * Copyright 2016 Nazar Suhovich
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class ContactsRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val data: MutableList<Any> = mutableListOf()
    var clickListener: ((name: String, number: String) -> Unit)? = null
    var type = ContactsActivity.CONTACT

    @Inject
    lateinit var themeUtil: ThemeUtil
    @Inject
    lateinit var prefs: Prefs

    init {
        ReminderApp.appComponent.inject(this)
    }

    @Suppress("UNCHECKED_CAST")
    fun setData(type: Int, data: List<Any>) {
        if (this.type == type) {
            this.type = type
            val diffResult = if (type == ContactsActivity.CALL) {
                DiffUtil.calculateDiff(CallDiffCallback(this.data as List<CallsItem>, data as List<CallsItem>))
            } else {
                DiffUtil.calculateDiff(ContactDiffCallback(this.data as List<ContactItem>, data as List<ContactItem>))
            }
            this.data.clear()
            this.data.addAll(data)
            diffResult.dispatchUpdatesTo(this)
        } else {
            this.type = type
            this.data.clear()
            this.data.addAll(data)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ContactsActivity.CONTACT) {
            ContactHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_contact, parent, false), themeUtil.isDark) {
                performClick(it)
            }
        } else {
            CallHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_call, parent, false),
                    themeUtil.isDark, prefs.is24HourFormatEnabled) {
                performClick(it)
            }
        }
    }

    private fun performClick(it: Int) {
        val item = data[it]
        if (item is ContactItem) {
            clickListener?.invoke(item.name, "")
        } else if (item is CallsItem) {
            clickListener?.invoke(if (item.name != null) item.name!! else "", item.number)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ContactHolder -> {
                holder.bind(data[position] as ContactItem)
            }
            is CallHolder -> {
                holder.bind(data[position] as CallsItem)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position] is CallsItem) ContactsActivity.CALL else ContactsActivity.CONTACT
    }

    override fun getItemCount(): Int {
        return data.size
    }

    internal class ContactDiffCallback(private var oldList: List<ContactItem>, private var newList: List<ContactItem>) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val p1 = oldList[oldItemPosition]
            val p2 = newList[newItemPosition]
            return p1.id == p2.id
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val p1 = oldList[oldItemPosition]
            val p2 = newList[newItemPosition]
            return p1 == p2
        }
    }

    internal class CallDiffCallback(private var oldList: List<CallsItem>, private var newList: List<CallsItem>) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val p1 = oldList[oldItemPosition]
            val p2 = newList[newItemPosition]
            return p1.id == p2.id
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val p1 = oldList[oldItemPosition]
            val p2 = newList[newItemPosition]
            return p1 == p2
        }
    }
}
