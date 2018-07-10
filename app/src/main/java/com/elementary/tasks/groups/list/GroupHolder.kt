package com.elementary.tasks.groups.list

import androidx.databinding.DataBindingUtil
import android.view.View

import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.interfaces.SimpleListener
import com.elementary.tasks.databinding.ListItemGroupBinding

import androidx.recyclerview.widget.RecyclerView

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

class GroupHolder(v: View, private val mEventListener: SimpleListener?) : RecyclerView.ViewHolder(v) {

    private val binding: ListItemGroupBinding?

    init {
        binding = DataBindingUtil.bind(v)
        v.setOnClickListener { view ->
            mEventListener?.onItemClicked(adapterPosition, view)
        }
        v.setOnLongClickListener { view ->
            mEventListener?.onItemLongClicked(adapterPosition, view)
            true
        }
    }

    fun setData(item: Group) {
        binding!!.item = item
    }
}