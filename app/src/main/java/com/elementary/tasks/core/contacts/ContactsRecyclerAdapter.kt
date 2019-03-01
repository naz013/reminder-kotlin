package com.elementary.tasks.core.contacts

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elementary.tasks.core.utils.ThemeUtil
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

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
