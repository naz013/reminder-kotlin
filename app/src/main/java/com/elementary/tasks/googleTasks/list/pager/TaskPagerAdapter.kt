package com.elementary.tasks.googleTasks.list.pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.elementary.tasks.googleTasks.list.TaskListFragment

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
class TaskPagerAdapter(fm: FragmentManager, data: List<String>) : FragmentPagerAdapter(fm) {

    private val data = mutableListOf<TaskListFragment>()

    init {
        this.data.clear()
        for (id in data) {
            this.data.add(TaskListFragment.newInstance(id))
        }
    }

    fun getCurrent(position: Int): TaskListFragment? {
        return if (data.size < position) {
            data[position]
        } else {
            null
        }
    }

    override fun getItem(position: Int): Fragment {
        return this.data[position]
    }

    override fun getCount(): Int {
        return data.size
    }
}
