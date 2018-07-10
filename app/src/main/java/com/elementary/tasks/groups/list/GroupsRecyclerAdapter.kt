package com.elementary.tasks.groups.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.core.data.models.Group
import com.elementary.tasks.core.interfaces.SimpleListener
import com.elementary.tasks.core.utils.ThemeUtil
import com.elementary.tasks.databinding.ListItemGroupBinding

import java.util.ArrayList
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

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
class GroupsRecyclerAdapter internal constructor(private val mEventListener: SimpleListener) : RecyclerView.Adapter<GroupHolder>() {

    private val mDataList = ArrayList<Group>()

    fun setData(list: List<Group>) {
        this.mDataList.clear()
        this.mDataList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupHolder {
        return GroupHolder(ListItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false).root, mEventListener)
    }

    override fun onBindViewHolder(holder: GroupHolder, position: Int) {
        val item = mDataList[position]
        holder.setData(item)
    }

    fun getItem(position: Int): Group {
        return mDataList[position]
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    companion object {

        @BindingAdapter("loadIndicator")
        fun loadIndicator(view: View, color: Int) {
            view.setBackgroundResource(ThemeUtil.getInstance(view.context).getCategoryIndicator(color))
        }
    }
}
