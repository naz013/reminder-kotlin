package com.elementary.tasks.google_tasks.list.pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.elementary.tasks.google_tasks.list.TaskListFragment

class TaskPagerAdapter(fm: FragmentManager, data: List<String>) : FragmentPagerAdapter(fm) {

    private val data = mutableListOf<TaskListFragment>()

    init {
        this.data.clear()
        for (id in data) {
            this.data.add(TaskListFragment.newInstance(id))
        }
    }

    fun getCurrent(position: Int): TaskListFragment? {
        return if (position < data.size) {
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
