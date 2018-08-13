package com.elementary.tasks.groups.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elementary.tasks.core.data.models.ReminderGroup
import com.elementary.tasks.core.interfaces.SimpleListener
import java.util.*

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
class GroupsRecyclerAdapter : RecyclerView.Adapter<GroupHolder>() {

    private val mDataList = ArrayList<ReminderGroup>()
    var mEventListener: SimpleListener? = null

    fun setData(list: List<ReminderGroup>) {
        this.mDataList.clear()
        this.mDataList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupHolder {
        return GroupHolder(parent, mEventListener)
    }

    override fun onBindViewHolder(holder: GroupHolder, position: Int) {
        val item = mDataList[position]
        holder.setData(item)
    }

    fun getItem(position: Int): ReminderGroup {
        return mDataList[position]
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }
}
