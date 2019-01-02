package com.elementary.tasks.monthView

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.elementary.tasks.R
import com.elementary.tasks.core.calendar.Events
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.core.utils.launchDefault
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.core.views.MonthView
import com.elementary.tasks.dayView.day.EventModel
import hirondelle.date4j.DateTime
import kotlinx.android.synthetic.main.fragment_month_view.*
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.*

/**
 * Copyright 2018 Nazar Suhovich
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
class MonthFragment : Fragment() {

    private var callback: MonthCallback? = null
    private var mItem: MonthPagerItem? = null

    fun getModel(): MonthPagerItem? = mItem

    fun setModel(monthPagerItem: MonthPagerItem) {
        this.mItem = monthPagerItem
        Timber.d("setModel: $monthPagerItem")
        monthView?.setDate(monthPagerItem.year, monthPagerItem.month + 1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragment = parentFragment
        if (fragment != null) {
            callback = fragment as MonthCallback?
        }
        if (arguments != null) {
            mItem = arguments?.getSerializable(ARGUMENT_PAGE_NUMBER) as MonthPagerItem?
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_month_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val item = mItem
        if (item != null) {
            monthView?.setDate(item.year, item.month + 1)
            monthView?.setDateClick(object : MonthView.OnDateClick {
                override fun onClick(dateTime: DateTime) {
                    callback?.onDateClick(TimeUtil.convertDateTimeToDate(dateTime))
                }
            })
            monthView?.setDateLongClick(object : MonthView.OnDateLongClick {
                override fun onLongClick(dateTime: DateTime) {
                    callback?.onDateLongClick(TimeUtil.convertDateTimeToDate(dateTime))
                }
            })
        }
    }

    fun requestData() {
        val item = mItem
        if (item != null) {
            launchDefault {
                delay(250)
                withUIContext {
                    callback?.find(item) { eventsPagerItem, list ->
                        Timber.d("setModel: $eventsPagerItem, ${list.size}")
                        launchDefault {
                            val data = mapData(list)
                            withUIContext {
                                monthView?.setEventsMap(data)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun mapData(list: List<EventModel>): Map<DateTime, Events> {
        val map = mutableMapOf<DateTime, Events>()
        val birthdayColor = callback?.birthdayColor() ?: Color.GREEN
        val reminderColor = callback?.reminderColor() ?: Color.BLUE
        for (model in list) {
            val obj = model.model
            if (obj is Birthday) {
                var date: Date? = null
                try {
                    date = TimeUtil.BIRTH_DATE_FORMAT.parse(obj.date)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = System.currentTimeMillis()
                val year = calendar.get(Calendar.YEAR)
                if (date != null) {
                    calendar.time = date
                    var i = -1
                    while (i < 2) {
                        calendar.set(Calendar.YEAR, year + i)
                        setEvent(calendar.timeInMillis, obj.name, birthdayColor, Events.Type.BIRTHDAY, map)
                        i++
                    }
                }
            } else if (obj is Reminder) {
                val eventTime = obj.dateTime
                setEvent(eventTime, obj.summary, reminderColor, Events.Type.REMINDER, map)
            }
        }
        Timber.d("mapData: $map")
        return map
    }

    private fun setEvent(eventTime: Long, summary: String, color: Int, type: Events.Type, map: MutableMap<DateTime, Events>) {
        val key = TimeUtil.convertToDateTime(eventTime)
        if (map.containsKey(key)) {
            val events = map[key] ?: Events()
            events.addEvent(summary, color, type, eventTime)
            map[key] = events
        } else {
            val events = Events(summary, color, type, eventTime)
            map[key] = events
        }
    }

    companion object {
        private const val ARGUMENT_PAGE_NUMBER = "arg_page"
        fun newInstance(item: MonthPagerItem): MonthFragment {
            val pageFragment = MonthFragment()
            val bundle = Bundle()
            bundle.putSerializable(ARGUMENT_PAGE_NUMBER, item)
            pageFragment.arguments = bundle
            return pageFragment
        }
    }
}
