package com.elementary.tasks.core.app_widgets.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.databinding.FragmentTasksWidgetPreviewBinding

import java.util.ArrayList

import androidx.fragment.app.Fragment

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
class TasksThemeFragment : Fragment() {
    private var mPageNumber: Int = 0
    private var mList: List<TasksTheme>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = arguments
        mPageNumber = intent!!.getInt(ARGUMENT_PAGE_NUMBER)
        mList = intent.getParcelableArrayList(ARGUMENT_DATA)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentTasksWidgetPreviewBinding.inflate(inflater, container, false)

        val eventsTheme = mList!![mPageNumber]

        val windowColor = eventsTheme.windowColor
        binding.background.setBackgroundResource(windowColor)
        val windowTextColor = eventsTheme.windowTextColor
        binding.themeTitle.setTextColor(windowTextColor)
        binding.themeTip.setTextColor(windowTextColor)

        val headerColor = eventsTheme.headerColor
        val backgroundColor = eventsTheme.backgroundColor
        val titleColor = eventsTheme.titleColor
        val itemTextColor = eventsTheme.itemTextColor

        val settingsIcon = eventsTheme.settingsIcon
        val plusIcon = eventsTheme.plusIcon

        binding.widgetTitle.setTextColor(titleColor)
        binding.task.setTextColor(itemTextColor)
        binding.note.setTextColor(itemTextColor)
        binding.taskDate.setTextColor(itemTextColor)

        binding.headerBg.setBackgroundResource(headerColor)
        binding.widgetBg.setBackgroundResource(backgroundColor)

        binding.tasksCount.setImageResource(plusIcon)
        binding.optionsButton.setImageResource(settingsIcon)

        binding.themeTitle.text = eventsTheme.title
        return binding.root
    }

    companion object {

        private val ARGUMENT_PAGE_NUMBER = "arg_page_number"
        private val ARGUMENT_DATA = "arg_data"

        fun newInstance(page: Int, list: List<TasksTheme>): TasksThemeFragment {
            val pageFragment = TasksThemeFragment()
            val arguments = Bundle()
            arguments.putInt(ARGUMENT_PAGE_NUMBER, page)
            arguments.putParcelableArrayList(ARGUMENT_DATA, ArrayList(list))
            pageFragment.arguments = arguments
            return pageFragment
        }
    }
}
