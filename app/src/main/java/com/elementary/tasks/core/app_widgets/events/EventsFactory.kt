package com.elementary.tasks.core.app_widgets.events

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.ReminderApp
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Birthday
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.HashMap
import java.util.Locale
import javax.inject.Inject
import kotlin.Comparator

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
class EventsFactory constructor(private val mContext: Context, intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private val data = ArrayList<CalendarItem>()
    private val map = HashMap<String, Reminder>()
    @Inject
    lateinit var prefs: Prefs
    private val widgetID: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    private val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        ReminderApp.appComponent.inject(this)
    }

    override fun onCreate() {
        data.clear()
        map.clear()
    }

    override fun onDataSetChanged() {
        data.clear()
        map.clear()
        val is24 = prefs.is24HourFormat
        val reminderItems = AppDb.getAppDatabase(mContext).reminderDao().getAll(true, false)
        for (item in reminderItems) {
            val type = item.type
            val summary = item.summary
            val eventTime = item.dateTime
            val id = item.uuId

            var time = ""
            var date = ""
            var viewType = 1
            when {
                Reminder.isGpsType(type) -> {
                    val place = item.places[0]
                    date = String.format(Locale.getDefault(), "%.5f", place.latitude)
                    time = String.format(Locale.getDefault(), "%.5f", place.longitude)
                }
                Reminder.isBase(type, Reminder.BY_WEEK) -> {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = eventTime
                    date = ReminderUtils.getRepeatString(mContext, prefs, item.weekdays)
                    time = TimeUtil.getTime(calendar.time, is24, prefs.appLanguage)
                }
                Reminder.isBase(type, Reminder.BY_MONTH) -> {
                    val calendar1 = Calendar.getInstance()
                    calendar1.timeInMillis = eventTime
                    date = TimeUtil.date(prefs.appLanguage).format(calendar1.time)
                    time = TimeUtil.getTime(calendar1.time, is24, prefs.appLanguage)
                }
                Reminder.isSame(type, Reminder.BY_DATE_SHOP) -> {
                    viewType = 2
                    map[id] = item
                }
                else -> {
                    val dT = TimeCount.getNextDateTime(eventTime, prefs)
                    date = dT[0]
                    time = dT[1]
                }
            }
            data.add(CalendarItem(CalendarItem.Type.REMINDER, summary, item.target, id, time, date, eventTime, viewType, item))
        }

        if (prefs.isBirthdayInWidgetEnabled) {
            var mDay: Int
            var mMonth: Int
            var n = 0
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = TimeUtil.getBirthdayTime(prefs.birthdayTime)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            calendar.timeInMillis = System.currentTimeMillis()
            do {
                mDay = calendar.get(Calendar.DAY_OF_MONTH)
                mMonth = calendar.get(Calendar.MONTH)
                val list = AppDb.getAppDatabase(mContext).birthdaysDao().getAll(mDay.toString() + "|" + mMonth)
                for (item in list) {
                    val birthday = item.date
                    val name = item.name
                    var eventTime: Long = 0
                    try {
                        val date = format.parse(birthday)
                        val calendar1 = Calendar.getInstance()
                        calendar1.timeInMillis = System.currentTimeMillis()
                        val year = calendar1.get(Calendar.YEAR)
                        if (date != null) {
                            calendar1.time = date
                            calendar1.set(Calendar.YEAR, year)
                            calendar1.set(Calendar.HOUR_OF_DAY, hour)
                            calendar1.set(Calendar.MINUTE, minute)
                            eventTime = calendar1.timeInMillis
                        }
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }

                    data.add(CalendarItem(CalendarItem.Type.BIRTHDAY, mContext.getString(R.string.birthday), name, item.key, birthday, "", eventTime, 1, item))
                }
                calendar.timeInMillis = calendar.timeInMillis + 1000 * 60 * 60 * 24
                n++
            } while (n <= 7)
        }
        data.sortWith(Comparator { eventsItem, o2 ->
            var time1: Long = 0
            var time2: Long = 0
            if (eventsItem.item is Birthday) {
                val item = eventsItem.item as Birthday
                val dateItem = TimeUtil.getFutureBirthdayDate(TimeUtil.getBirthdayTime(prefs.birthdayTime), item.date)
                if (dateItem != null) {
                    val calendar = dateItem.calendar
                    time1 = calendar.timeInMillis
                }
            } else if (eventsItem.item is Reminder) {
                val reminder = eventsItem.item as Reminder
                time1 = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
            }
            if (o2.item is Birthday) {
                val item = o2.item as Birthday
                val dateItem = TimeUtil.getFutureBirthdayDate(TimeUtil.getBirthdayTime(prefs.birthdayTime), item.date)
                if (dateItem != null) {
                    val calendar = dateItem.calendar
                    time2 = calendar.timeInMillis
                }
            } else if (o2.item is Reminder) {
                val reminder = o2.item as Reminder
                time2 = TimeUtil.getDateTimeFromGmt(reminder.eventTime)
            }
            (time1 - time2).toInt()
        })
    }

    override fun onDestroy() {
        map.clear()
        data.clear()
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getViewAt(i: Int): RemoteViews? {
        val sp = mContext.getSharedPreferences(EventsWidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE)
        val itemTextSize = sp.getFloat(EventsWidgetConfigActivity.WIDGET_TEXT_SIZE + widgetID, 0f)
        val itemBgColor = sp.getInt(EventsWidgetConfigActivity.WIDGET_ITEM_BG + widgetID, 0)

        var rv: RemoteViews? = null
        if (i >= count) {
            return null
        }
        val item = data[i]
        if (item.viewType == 1) {
            rv = RemoteViews(mContext.packageName, R.layout.list_item_widget_events)

            rv.setInt(R.id.listItemCard, "setBackgroundResource", WidgetUtils.newWidgetBg(itemBgColor))

            val textColor = if (WidgetUtils.isDarkBg(itemBgColor)) {
                ContextCompat.getColor(mContext, R.color.pureWhite)
            } else {
                ContextCompat.getColor(mContext, R.color.pureBlack)
            }
            rv.setTextColor(R.id.taskText, textColor)
            rv.setTextColor(R.id.taskDate, textColor)
            rv.setTextColor(R.id.taskNumber, textColor)
            rv.setTextColor(R.id.taskTime, textColor)
            rv.setTextColor(R.id.leftTime, textColor)

            val icon = if (item.type == CalendarItem.Type.REMINDER) {
                ViewUtils.createIcon(mContext, R.drawable.ic_twotone_alarm_24px, textColor)
            } else {
                ViewUtils.createIcon(mContext, R.drawable.ic_twotone_cake_24px, textColor)
            }
            rv.setImageViewBitmap(R.id.statusIcon, icon)

            var task = item.name
            if (task == null || task.isBlank()) {
                task = Contacts.getNameFromNumber(item.number, mContext)
            }
            rv.setTextViewText(R.id.taskText, task)

            rv.setTextViewTextSize(R.id.taskText, TypedValue.COMPLEX_UNIT_SP, itemTextSize)
            rv.setTextViewTextSize(R.id.taskNumber, TypedValue.COMPLEX_UNIT_SP, itemTextSize)
            rv.setTextViewTextSize(R.id.taskDate, TypedValue.COMPLEX_UNIT_SP, itemTextSize)
            rv.setTextViewTextSize(R.id.taskTime, TypedValue.COMPLEX_UNIT_SP, itemTextSize)
            rv.setTextViewTextSize(R.id.leftTime, TypedValue.COMPLEX_UNIT_SP, itemTextSize)

            val number = item.number
            if (number != null && number.isNotBlank()) {
                rv.setTextViewText(R.id.taskNumber, number)
                rv.setViewVisibility(R.id.taskNumber, View.VISIBLE)
            } else {
                rv.setViewVisibility(R.id.taskNumber, View.INVISIBLE)
            }
            rv.setTextViewText(R.id.taskDate, item.dayDate)
            rv.setTextViewText(R.id.taskTime, item.time)
            rv.setTextViewText(R.id.leftTime, TimeCount.getRemaining(mContext, item.date, prefs.appLanguage))

            if (item.id != null) {
                val fillInIntent = Intent()
                fillInIntent.putExtra(Constants.INTENT_ID, item.id)
                if (item.type == CalendarItem.Type.REMINDER) {
                    fillInIntent.putExtra(EventEditService.TYPE, true)
                } else {
                    fillInIntent.putExtra(EventEditService.TYPE, false)
                }
                rv.setOnClickFillInIntent(R.id.taskDate, fillInIntent)
                rv.setOnClickFillInIntent(R.id.taskTime, fillInIntent)
                rv.setOnClickFillInIntent(R.id.taskNumber, fillInIntent)
                rv.setOnClickFillInIntent(R.id.taskText, fillInIntent)
                rv.setOnClickFillInIntent(R.id.listItemCard, fillInIntent)
            }
        }
        if (item.viewType == 2) {
            rv = RemoteViews(mContext.packageName, R.layout.list_item_widget_shop_list)
            rv.setInt(R.id.listItemCard, "setBackgroundResource", WidgetUtils.newWidgetBg(itemBgColor))
            val textColor = if (WidgetUtils.isDarkBg(itemBgColor)) {
                ContextCompat.getColor(mContext, R.color.pureWhite)
            } else {
                ContextCompat.getColor(mContext, R.color.pureBlack)
            }

            val task = item.name
            rv.setTextViewText(R.id.taskText, task)
            rv.setTextColor(R.id.taskText, textColor)
            rv.setTextViewTextSize(R.id.taskText, TypedValue.COMPLEX_UNIT_SP, itemTextSize)

            var count = 0
            val lists = map[item.id]?.shoppings ?: listOf()
            rv.removeAllViews(R.id.todoList)
            for (list in lists) {
                val view = RemoteViews(mContext.packageName, R.layout.list_item_widget_shop_item)
                val icon = if (list.isChecked) {
                    ViewUtils.createIcon(mContext, R.drawable.ic_twotone_check_box_24px, textColor)
                } else {
                    ViewUtils.createIcon(mContext, R.drawable.ic_twotone_check_box_outline_blank_24px, textColor)
                }
                rv.setImageViewBitmap(R.id.checkView, icon)
                view.setTextColor(R.id.shopText, textColor)
                view.setTextViewTextSize(R.id.shopText, TypedValue.COMPLEX_UNIT_SP, itemTextSize)
                count++
                if (count == 9) {
                    view.setViewVisibility(R.id.checkView, View.INVISIBLE)
                    view.setTextViewText(R.id.shopText, "...")
                    rv.addView(R.id.todoList, view)
                    break
                } else {
                    view.setViewVisibility(R.id.checkView, View.VISIBLE)
                    view.setTextViewText(R.id.shopText, list.summary)
                    rv.addView(R.id.todoList, view)
                }
            }

            val fillInIntent = Intent()
            fillInIntent.putExtra(Constants.INTENT_ID, item.id)
            fillInIntent.putExtra(EventEditService.TYPE, true)
            rv.setOnClickFillInIntent(R.id.taskText, fillInIntent)
            rv.setOnClickFillInIntent(R.id.listItemCard, fillInIntent)
            rv.setOnClickFillInIntent(R.id.todoList, fillInIntent)
        }
        return rv
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}