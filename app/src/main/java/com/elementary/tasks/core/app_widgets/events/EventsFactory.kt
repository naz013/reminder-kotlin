package com.elementary.tasks.core.app_widgets.events

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.elementary.tasks.Actions
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.data.models.Reminder
import com.elementary.tasks.core.utils.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class EventsFactory constructor(private val mContext: Context, intent: Intent) : RemoteViewsService.RemoteViewsFactory, KoinComponent {

    private var data = mutableListOf<CalendarItem>()
    private val map = mutableMapOf<String, Reminder>()
    private val prefs: Prefs by inject()
    private val appDb: AppDb by inject()
    private val widgetID: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

    override fun onCreate() {
        data.clear()
        map.clear()
    }

    override fun onDataSetChanged() {
        data.clear()
        map.clear()
        val is24 = prefs.is24HourFormat
        val reminderItems = appDb.reminderDao().getAll(active = true, removed = false)
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
                    if (item.hasReminder) {
                        val dT = TimeCount.getNextDateTime(eventTime, prefs)
                        date = dT[0]
                        time = dT[1]
                    }
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
            val birthTime = TimeUtil.getBirthdayTime(prefs.birthdayTime)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            do {
                mDay = calendar.get(Calendar.DAY_OF_MONTH)
                mMonth = calendar.get(Calendar.MONTH)
                val list = appDb.birthdaysDao().getAll("$mDay|$mMonth")
                for (item in list) {
                    val birthday = item.date
                    val name = item.name
                    val millis = TimeUtil.getFutureBirthdayDate(birthTime, item.date)?.millis ?: 0L

                    data.add(CalendarItem(CalendarItem.Type.BIRTHDAY, mContext.getString(R.string.birthday), name, item.key, birthday, "", millis, 1, item))
                }
                calendar.timeInMillis = calendar.timeInMillis + 1000 * 60 * 60 * 24
                n++
            } while (n <= 7)
        }
        data = data.sortedBy { it.date }.toMutableList()
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

        val textColor = if (WidgetUtils.isDarkBg(itemBgColor)) {
            ContextCompat.getColor(mContext, R.color.pureWhite)
        } else {
            ContextCompat.getColor(mContext, R.color.pureBlack)
        }

        var rv: RemoteViews? = null
        if (i >= count) {
            return null
        }
        val item = data[i]
        if (item.viewType == 1) {
            rv = RemoteViews(mContext.packageName, R.layout.list_item_widget_events)

            rv.setInt(R.id.listItemCard, "setBackgroundResource", WidgetUtils.newWidgetBg(itemBgColor))

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
            if (task == null || task.isBlank() && Permissions.checkPermission(mContext, Permissions.READ_CONTACTS)) {
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
                rv.setViewVisibility(R.id.taskNumber, View.GONE)
            }
            rv.setTextViewText(R.id.taskDate, item.dayDate)
            rv.setTextViewText(R.id.taskTime, item.time)
            rv.setTextViewText(R.id.leftTime, TimeCount.getRemaining(mContext, item.date, prefs.appLanguage))

            if (item.id != null) {
                val fillInIntent = Intent()
                fillInIntent.putExtra(Constants.INTENT_ID, item.id)
                fillInIntent.action = Actions.Reminder.ACTION_EDIT_EVENT
                if (item.type == CalendarItem.Type.REMINDER) {
                    fillInIntent.putExtra(EventActionReceiver.TYPE, true)
                } else {
                    fillInIntent.putExtra(EventActionReceiver.TYPE, false)
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

            rv.setImageViewBitmap(R.id.statusIcon, ViewUtils.createIcon(mContext, R.drawable.ic_twotone_shopping_cart_24px, textColor))

            val task = item.name
            rv.setTextViewText(R.id.taskText, task)
            rv.setTextColor(R.id.taskText, textColor)
            rv.setTextViewTextSize(R.id.taskText, TypedValue.COMPLEX_UNIT_SP, itemTextSize)

            val reminder = map[item.id]
            val lists = reminder?.shoppings ?: listOf()

            if (reminder != null && reminder.hasReminder) {
                rv.setTextViewText(R.id.taskDate, item.dayDate)
                rv.setTextViewText(R.id.taskTime, item.time)
                rv.setViewVisibility(R.id.dateTimeView, View.VISIBLE)
            } else {
                rv.setViewVisibility(R.id.dateTimeView, View.GONE)
            }

            var count = 0

            rv.removeAllViews(R.id.todoList)

            val checkedIcon = ViewUtils.createIcon(mContext, R.drawable.ic_twotone_check_box_24px, textColor)
            val unCheckedIcon = ViewUtils.createIcon(mContext, R.drawable.ic_twotone_check_box_outline_blank_24px, textColor)

            for (list in lists) {
                val view = RemoteViews(mContext.packageName, R.layout.list_item_widget_shop_item)
                val icon = if (list.isChecked) checkedIcon else unCheckedIcon
                view.setImageViewBitmap(R.id.checkView, icon)
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
            fillInIntent.putExtra(EventActionReceiver.TYPE, true)
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