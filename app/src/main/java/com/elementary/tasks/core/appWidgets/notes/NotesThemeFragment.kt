package com.elementary.tasks.core.appWidgets.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.databinding.FragmentNoteWidgetPreviewBinding

import java.util.ArrayList

import androidx.fragment.app.Fragment

/**
 * Copyright 2015 Nazar Suhovich
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
class NotesThemeFragment : Fragment() {
    private var mPageNumber: Int = 0
    private var mList: List<NotesTheme>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = arguments
        mPageNumber = intent!!.getInt(ARGUMENT_PAGE_NUMBER)
        mList = intent.getParcelableArrayList(ARGUMENT_DATA)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentNoteWidgetPreviewBinding.inflate(inflater, container, false)
        val calendarTheme = mList!![mPageNumber]

        val windowColor = calendarTheme.windowColor
        binding.background.setBackgroundResource(windowColor)
        val windowTextColor = calendarTheme.windowTextColor
        binding.themeTitle.setTextColor(windowTextColor)
        binding.themeTip.setTextColor(windowTextColor)

        val headerColor = calendarTheme.headerColor
        val backgroundColor = calendarTheme.backgroundColor
        val titleColor = calendarTheme.titleColor

        val settingsIcon = calendarTheme.settingsIcon
        val plusIcon = calendarTheme.plusIcon

        binding.widgetTitle.setTextColor(titleColor)
        binding.headerBg.setBackgroundResource(headerColor)
        binding.widgetBg.setBackgroundResource(backgroundColor)

        binding.tasksCount.setImageResource(plusIcon)
        binding.settingsButton.setImageResource(settingsIcon)

        binding.themeTitle.text = calendarTheme.title
        return binding.root
    }

    companion object {

        internal val ARGUMENT_PAGE_NUMBER = "arg_page_number"
        internal val ARGUMENT_DATA = "arg_data"

        fun newInstance(page: Int, list: List<NotesTheme>): NotesThemeFragment {
            val pageFragment = NotesThemeFragment()
            val arguments = Bundle()
            arguments.putInt(ARGUMENT_PAGE_NUMBER, page)
            arguments.putParcelableArrayList(ARGUMENT_DATA, ArrayList(list))
            pageFragment.arguments = arguments
            return pageFragment
        }
    }
}
