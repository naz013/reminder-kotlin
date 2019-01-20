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
class ContactsRecyclerAdapter : RecyclerView.Adapter<ContactHolder>() {

    val data: MutableList<ContactItem> = mutableListOf()
    var clickListener: ((name: String, number: String) -> Unit)? = null

    @Inject
    lateinit var themeUtil: ThemeUtil
    @Inject
    lateinit var prefs: Prefs

    init {
        ReminderApp.appComponent.inject(this)
    }

    fun setData(data: List<ContactItem>) {
        val diffResult = DiffUtil.calculateDiff(ContactDiffCallback(this.data as List<ContactItem>, data))
        this.data.clear()
        this.data.addAll(data)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        return ContactHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_contact, parent, false),
                themeUtil.isDark) { performClick(it) }
    }

    private fun performClick(it: Int) {
        val item = data[it]
        clickListener?.invoke(item.name, "")
    }

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        holder.bind(data[position])
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
}
