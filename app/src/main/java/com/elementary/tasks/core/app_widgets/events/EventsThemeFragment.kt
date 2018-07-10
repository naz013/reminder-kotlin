package com.elementary.tasks.core.app_widgets.events

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.elementary.tasks.databinding.FragmentEventsWidgetPreviewBinding

import java.util.ArrayList
import java.util.Calendar
import java.util.Formatter
import java.util.GregorianCalendar
import java.util.Locale

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

class EventsThemeFragment : Fragment() {
    private var mPageNumber: Int = 0
    private var mList: List<EventsTheme>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = arguments
        mPageNumber = intent!!.getInt(ARGUMENT_PAGE_NUMBER)
        mList = intent.getParcelableArrayList(ARGUMENT_DATA)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentEventsWidgetPreviewBinding.inflate(inflater, container, false)

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
        val itemBackground = eventsTheme.itemBackground

        val settingsIcon = eventsTheme.settingsIcon
        val plusIcon = eventsTheme.plusIcon
        val voiceIcon = eventsTheme.voiceIcon

        binding.widgetDate.setTextColor(titleColor)
        binding.taskText.setTextColor(itemTextColor)
        binding.taskNumber.setTextColor(itemTextColor)
        binding.taskDate.setTextColor(itemTextColor)
        binding.taskTime.setTextColor(itemTextColor)

        binding.headerBg.setBackgroundResource(headerColor)
        binding.widgetBg.setBackgroundResource(backgroundColor)
        binding.listItemCard.setBackgroundResource(itemBackground)

        binding.plusButton.setImageResource(plusIcon)
        binding.optionsButton.setImageResource(settingsIcon)
        binding.voiceButton.setImageResource(voiceIcon)

        binding.themeTitle.text = eventsTheme.title

        val monthYearStringBuilder = StringBuilder(50)
        val monthYearFormatter = Formatter(monthYearStringBuilder, Locale.getDefault())
        val monthYearFlag = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NO_MONTH_DAY or DateUtils.FORMAT_SHOW_YEAR
        val cal = GregorianCalendar()
        val monthTitle = DateUtils.formatDateRange(activity,
                monthYearFormatter, cal.timeInMillis, cal.timeInMillis, monthYearFlag).toString()
        binding.widgetDate.text = monthTitle.toUpperCase()
        return binding.root
    }

    companion object {

        internal val ARGUMENT_PAGE_NUMBER = "arg_page_number"
        internal val ARGUMENT_DATA = "arg_data"

        fun newInstance(page: Int, list: List<EventsTheme>): EventsThemeFragment {
            val pageFragment = EventsThemeFragment()
            val arguments = Bundle()
            arguments.putInt(ARGUMENT_PAGE_NUMBER, page)
            arguments.putParcelableArrayList(ARGUMENT_DATA, ArrayList(list))
            pageFragment.arguments = arguments
            return pageFragment
        }
    }
}
