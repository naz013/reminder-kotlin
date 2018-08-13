package com.elementary.tasks.groups.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseHolder
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.interfaces.SimpleListener
import com.mcxiaoke.koi.ext.onClick
import com.mcxiaoke.koi.ext.onLongClick
import kotlinx.android.synthetic.main.list_item_group.view.*

/**
 * Copyright 2017 Nazar Suhovich
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
class GroupHolder(parent: ViewGroup, private val mEventListener: SimpleListener?) :
        BaseHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_group, parent, false)) {

    init {
        itemView.onClick { view ->
            mEventListener?.onItemClicked(adapterPosition, view)
        }
        itemView.onLongClick { view ->
            mEventListener?.onItemLongClicked(adapterPosition, view)
            true
        }
    }

    fun setData(item: ReminderGroup) {
        itemView.textView.text = item.title
        loadIndicator(itemView.indicator, item.color)
    }

    private fun loadIndicator(view: View, color: Int) {
        view.setBackgroundResource(themeUtil.getCategoryIndicator(color))
    }
}