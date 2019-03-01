package com.elementary.tasks.core.app_widgets.calendar

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.TimeUtil
import hirondelle.date4j.DateTime
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.text.SimpleDateFormat
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
class CalendarWeekdayFactory(private val mContext: Context, intent: Intent) : RemoteViewsService.RemoteViewsFactory, KoinComponent {

    private val mWeekdaysList = ArrayList<String>()
    private val mWidgetId: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    private val startDayOfWeek = SUNDAY

    private val prefs: Prefs by inject()

    override fun onCreate() {
        mWeekdaysList.clear()
    }

    override fun onDataSetChanged() {
        mWeekdaysList.clear()
        val fmt = SimpleDateFormat("EEE", Locale.getDefault())

        val sunday = DateTime(2013, 2, 17, 0, 0, 0, 0)
        var nextDay = sunday.plusDays(startDayOfWeek - SUNDAY)
        if (prefs.startDay == 1) {
            nextDay = nextDay.plusDays(1)
        }
        for (i in 0..6) {
            val date = TimeUtil.convertDateTimeToDate(nextDay)
            mWeekdaysList.add(fmt.format(date).toUpperCase())
            nextDay = nextDay.plusDays(1)
        }
    }

    override fun onDestroy() {
        mWeekdaysList.clear()
    }

    override fun getCount(): Int {
        return mWeekdaysList.size
    }

    override fun getViewAt(i: Int): RemoteViews {
        val sp = mContext.getSharedPreferences(CalendarWidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE)
        val bgColor = sp.getInt(CalendarWidgetConfigActivity.WIDGET_BG + mWidgetId, 0)
        val textColor = if (WidgetUtils.isDarkBg(bgColor)) {
            ContextCompat.getColor(mContext, R.color.pureWhite)
        } else {
            ContextCompat.getColor(mContext, R.color.pureBlack)
        }

        val rv = RemoteViews(mContext.packageName, R.layout.list_item_weekday_grid)
        rv.setTextViewText(R.id.textView1, mWeekdaysList[i])
        rv.setTextColor(R.id.textView1, textColor)
        return rv
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    companion object {
        private const val SUNDAY = 1
    }
}