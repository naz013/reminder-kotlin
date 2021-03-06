package com.elementary.tasks.core.app_widgets.calendar

import android.app.AlarmManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.WidgetDataProvider
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.Prefs
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.TimeUtil
import com.elementary.tasks.home.BottomNavActivity
import hirondelle.date4j.DateTime
import java.util.*

class CalendarMonthFactory(
  intent: Intent,
  private val context: Context,
  private val prefs: Prefs,
  private val widgetDataProvider: WidgetDataProvider
) : RemoteViewsService.RemoteViewsFactory {

  private val mDateTimeList = ArrayList<DateTime>()
  private val mPagerData = ArrayList<WidgetItem>()
  private val mWidgetId: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
  private var mDay: Int = 0
  private var mMonth: Int = 0
  private var mYear: Int = 0

  override fun onCreate() {
    mDateTimeList.clear()
    mPagerData.clear()
  }

  override fun onDataSetChanged() {
    mDateTimeList.clear()

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()

    val sp = context.getSharedPreferences(CalendarWidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE)
    val prefsMonth = sp.getInt(CalendarWidgetConfigActivity.CALENDAR_WIDGET_MONTH + mWidgetId, 0)

    var year = sp.getInt(CalendarWidgetConfigActivity.CALENDAR_WIDGET_YEAR + mWidgetId, calendar.get(Calendar.YEAR))
    mDay = calendar.get(Calendar.DAY_OF_MONTH)
    mMonth = prefsMonth + 1

    if (year < 1) {
      year = DateTime.now(TimeZone.getDefault()).year
    }
    mYear = year

    val firstDateOfMonth = DateTime(year, prefsMonth + 1, 1, 0, 0, 0, 0)
    val lastDateOfMonth = firstDateOfMonth.plusDays(firstDateOfMonth.numDaysInMonth - 1)

    var weekdayOfFirstDate = firstDateOfMonth.weekDay!!
    val startDayOfWeek = prefs.startDay + 1

    if (weekdayOfFirstDate < startDayOfWeek) {
      weekdayOfFirstDate += 7
    }

    while (weekdayOfFirstDate > 0) {
      val dateTime = firstDateOfMonth.minusDays(weekdayOfFirstDate - startDayOfWeek)
      if (!dateTime.lt(firstDateOfMonth)) {
        break
      }
      mDateTimeList.add(dateTime)
      weekdayOfFirstDate--
    }
    for (i in 0 until lastDateOfMonth.day) {
      mDateTimeList.add(firstDateOfMonth.plusDays(i))
    }
    var endDayOfWeek = startDayOfWeek - 1
    if (endDayOfWeek == 0) {
      endDayOfWeek = 7
    }
    if (lastDateOfMonth.weekDay != endDayOfWeek) {
      var i = 1
      while (true) {
        val nextDay = lastDateOfMonth.plusDays(i)
        mDateTimeList.add(nextDay)
        i++
        if (nextDay.weekDay == endDayOfWeek) {
          break
        }
      }
    }
    val size = mDateTimeList.size
    val numOfDays = 42 - size
    val lastDateTime = mDateTimeList[size - 1]
    for (i in 1..numOfDays) {
      val nextDateTime = lastDateTime.plusDays(i)
      mDateTimeList.add(nextDateTime)
    }
    showEvents()
  }

  private fun showEvents() {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = TimeUtil.getBirthdayTime(prefs.birthdayTime)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val isFeature = prefs.isFutureEventEnabled
    val isRemindersEnabled = prefs.isRemindersInCalendarEnabled

    widgetDataProvider.setTime(hour, minute)
    if (isRemindersEnabled) {
      widgetDataProvider.setFeature(isFeature)
    }
    widgetDataProvider.fillArray()
    mPagerData.clear()

    calendar.timeInMillis = System.currentTimeMillis()
    var currentDay: Int
    var currentMonth: Int
    var currentYear: Int
    var position = 0
    do {
      currentDay = calendar.get(Calendar.DAY_OF_MONTH)
      currentMonth = calendar.get(Calendar.MONTH)
      currentYear = calendar.get(Calendar.YEAR)
      val hasReminders = widgetDataProvider.hasReminder(currentDay, currentMonth, currentYear)
      val hasBirthdays = widgetDataProvider.hasBirthday(currentDay, currentMonth)
      mPagerData.add(WidgetItem(currentDay, currentMonth, currentYear,
        hasReminders, hasBirthdays))
      position++
      calendar.timeInMillis = calendar.timeInMillis + AlarmManager.INTERVAL_DAY
    } while (position < Configs.MAX_DAYS_COUNT)
  }

  override fun onDestroy() {
    mDateTimeList.clear()
    mPagerData.clear()
  }

  override fun getCount(): Int {
    return mDateTimeList.size
  }

  override fun getViewAt(i: Int): RemoteViews {
    val sp = context.getSharedPreferences(CalendarWidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE)
    val bgColor = sp.getInt(CalendarWidgetConfigActivity.WIDGET_BG + mWidgetId, 0)
    val textColor = if (WidgetUtils.isDarkBg(bgColor)) {
      ContextCompat.getColor(context, R.color.pureWhite)
    } else {
      ContextCompat.getColor(context, R.color.pureBlack)
    }
    val prefsMonth = sp.getInt(CalendarWidgetConfigActivity.CALENDAR_WIDGET_MONTH + mWidgetId, 0)
    val rv = RemoteViews(context.packageName, R.layout.list_item_month_grid)

    val selDay = mDateTimeList[i].day ?: 0
    val selMonth = mDateTimeList[i].month ?: 0
    val selYear = mDateTimeList[i].year ?: 0

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()
    val realMonth = calendar.get(Calendar.MONTH)
    val realYear = calendar.get(Calendar.YEAR)

    rv.setTextViewText(R.id.textView, selDay.toString())
    if (selMonth == prefsMonth + 1) {
      rv.setTextColor(R.id.textView, textColor)
    } else {
      rv.setTextColor(R.id.textView, ContextCompat.getColor(context, R.color.material_grey))
    }

    rv.setInt(R.id.currentMark, "setBackgroundColor", Color.TRANSPARENT)
    rv.setInt(R.id.reminderMark, "setBackgroundColor", Color.TRANSPARENT)
    rv.setInt(R.id.birthdayMark, "setBackgroundColor", Color.TRANSPARENT)

    if (mPagerData.size > 0) {
      for (item in mPagerData) {
        val day = item.day
        val month = item.month + 1
        val year = item.year
        if (day == selDay && month == selMonth) {
          if (item.isHasReminders && year == selYear) {
            rv.setInt(R.id.reminderMark, "setBackgroundColor",
              ThemeProvider.colorReminderCalendar(context, prefs))
          } else {
            rv.setInt(R.id.reminderMark, "setBackgroundColor", Color.TRANSPARENT)
          }
          if (item.isHasBirthdays) {
            rv.setInt(R.id.birthdayMark, "setBackgroundColor",
              ThemeProvider.colorBirthdayCalendar(context, prefs))
          } else {
            rv.setInt(R.id.birthdayMark, "setBackgroundColor", Color.TRANSPARENT)
          }
          break
        }
      }
    }

    if (mDay == selDay && mMonth == selMonth && mYear == realYear && mMonth == realMonth + 1
      && mYear == selYear) {
      rv.setInt(R.id.currentMark, "setBackgroundColor", ThemeProvider.colorCurrentCalendar(context, prefs))
    } else {
      rv.setInt(R.id.currentMark, "setBackgroundColor", Color.TRANSPARENT)
    }

    calendar.timeInMillis = System.currentTimeMillis()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    calendar.set(Calendar.MONTH, selMonth - 1)
    calendar.set(Calendar.DAY_OF_MONTH, selDay)
    calendar.set(Calendar.YEAR, selYear)
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    val dateMills = calendar.timeInMillis

    val fillInIntent = Intent()
    fillInIntent.putExtra("date", dateMills)
    fillInIntent.putExtra(BottomNavActivity.ARG_DEST, BottomNavActivity.Companion.Dest.DAY_VIEW)
    rv.setOnClickFillInIntent(R.id.textView, fillInIntent)
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

  data class WidgetItem(
    var day: Int,
    var month: Int,
    var year: Int,
    val isHasReminders: Boolean,
    val isHasBirthdays: Boolean
  )
}