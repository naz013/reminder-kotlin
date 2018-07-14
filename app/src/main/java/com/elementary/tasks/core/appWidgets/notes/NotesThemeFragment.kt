package com.elementary.tasks.core.appWidgets.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import kotlinx.android.synthetic.main.fragment_note_widget_preview.*
import java.util.*

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
        return inflater.inflate(R.layout.fragment_note_widget_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val calendarTheme = mList!![mPageNumber]

        val windowColor = calendarTheme.windowColor
        background.setBackgroundResource(windowColor)
        val windowTextColor = calendarTheme.windowTextColor
        themeTitle.setTextColor(windowTextColor)
        themeTip.setTextColor(windowTextColor)

        val headerColor = calendarTheme.headerColor
        val backgroundColor = calendarTheme.backgroundColor
        val titleColor = calendarTheme.titleColor

        val settingsIcon = calendarTheme.settingsIcon
        val plusIcon = calendarTheme.plusIcon

        widgetTitle.setTextColor(titleColor)
        headerBg.setBackgroundResource(headerColor)
        widgetBg.setBackgroundResource(backgroundColor)

        tasksCount.setImageResource(plusIcon)
        settingsButton.setImageResource(settingsIcon)

        themeTitle.text = calendarTheme.title
    }

    companion object {

        internal const val ARGUMENT_PAGE_NUMBER = "arg_page_number"
        internal const val ARGUMENT_DATA = "arg_data"

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
