package com.elementary.tasks.core.appWidgets.events

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import kotlinx.android.synthetic.main.fragment_events_widget_preview.*
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
        return inflater.inflate(R.layout.fragment_events_widget_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val eventsTheme = mList!![mPageNumber]

        val windowColor = eventsTheme.windowColor
        background.setBackgroundResource(windowColor)
        val windowTextColor = eventsTheme.windowTextColor
        themeTitle.setTextColor(windowTextColor)
        themeTip.setTextColor(windowTextColor)

        val headerColor = eventsTheme.headerColor
        val backgroundColor = eventsTheme.backgroundColor
        val titleColor = eventsTheme.titleColor
        val itemTextColor = eventsTheme.itemTextColor
        val itemBackground = eventsTheme.itemBackground

        val settingsIcon = eventsTheme.settingsIcon
        val plusIcon = eventsTheme.plusIcon
        val voiceIcon = eventsTheme.voiceIcon

        widgetDate.setTextColor(titleColor)
        taskText.setTextColor(itemTextColor)
        taskNumber.setTextColor(itemTextColor)
        taskDate.setTextColor(itemTextColor)
        taskTime.setTextColor(itemTextColor)

        headerBg.setBackgroundResource(headerColor)
        widgetBg.setBackgroundResource(backgroundColor)
        listItemCard.setBackgroundResource(itemBackground)

        plusButton.setImageResource(plusIcon)
        optionsButton.setImageResource(settingsIcon)
        voiceButton.setImageResource(voiceIcon)

        themeTitle.text = eventsTheme.title

        val monthYearStringBuilder = StringBuilder(50)
        val monthYearFormatter = Formatter(monthYearStringBuilder, Locale.getDefault())
        val monthYearFlag = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NO_MONTH_DAY or DateUtils.FORMAT_SHOW_YEAR
        val cal = GregorianCalendar()
        val monthTitle = DateUtils.formatDateRange(activity,
                monthYearFormatter, cal.timeInMillis, cal.timeInMillis, monthYearFlag).toString()
        widgetDate.text = monthTitle.toUpperCase()
    }

    companion object {

        internal const val ARGUMENT_PAGE_NUMBER = "arg_page_number"
        internal const val ARGUMENT_DATA = "arg_data"

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
