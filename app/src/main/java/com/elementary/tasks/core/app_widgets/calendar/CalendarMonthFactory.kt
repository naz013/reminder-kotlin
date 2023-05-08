package com.elementary.tasks.core.app_widgets.calendar

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.WidgetDataProvider
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.protocol.StartDayOfWeekProtocol
import com.elementary.tasks.core.utils.Configs
import com.elementary.tasks.core.utils.ThemeProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.core.utils.ui.dp2px
import com.elementary.tasks.home.BottomNavActivity
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.util.Calendar

class CalendarMonthFactory(
  intent: Intent,
  private val context: Context,
  private val prefs: Prefs,
  private val widgetDataProvider: WidgetDataProvider,
  private val dateTimeManager: DateTimeManager
) : RemoteViewsService.RemoteViewsFactory {

  private val dateList = ArrayList<LocalDate>()
  private val pagerData = ArrayList<WidgetItem>()
  private val widgetId: Int =
    intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
  private var mDay: Int = 0
  private var mMonth: Int = 0
  private var mYear: Int = 0
  private val prefsProvider = CalendarWidgetPrefsProvider(context, widgetId)

  override fun onCreate() {
    dateList.clear()
    pagerData.clear()
  }

  override fun onDataSetChanged() {
    dateList.clear()

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = System.currentTimeMillis()

    val prefsMonth = prefsProvider.getMonth()
    var year = prefsProvider.getYear()
    mDay = calendar.get(Calendar.DAY_OF_MONTH)
    mMonth = prefsMonth + 1

    if (year < 1) {
      year = LocalDate.now().year
    }
    mYear = year

    val firstDateOfMonth = LocalDate.of(year, prefsMonth + 1, 1)
    val lastDateOfMonth = firstDateOfMonth.plusDays(firstDateOfMonth.lengthOfMonth() - 1L)

    var weekdayOfFirstDate = firstDateOfMonth.dayOfWeek.value
    val startDayOfWeek = StartDayOfWeekProtocol(prefs.startDay).getForCalendar()

    if (weekdayOfFirstDate < startDayOfWeek) {
      weekdayOfFirstDate += 7
    }

    while (weekdayOfFirstDate > 0) {
      val dateTime = firstDateOfMonth.minusDays(weekdayOfFirstDate - startDayOfWeek.toLong())
      if (!dateTime.isBefore(firstDateOfMonth)) {
        break
      }
      dateList.add(dateTime)
      weekdayOfFirstDate--
    }
    for (i in 0L until lastDateOfMonth.dayOfMonth) {
      dateList.add(firstDateOfMonth.plusDays(i))
    }
    var endDayOfWeek = startDayOfWeek - 1
    if (endDayOfWeek == 0) {
      endDayOfWeek = 7
    }
    if (lastDateOfMonth.dayOfWeek.value != endDayOfWeek) {
      var i = 1L
      while (true) {
        val nextDay = lastDateOfMonth.plusDays(i)
        dateList.add(nextDay)
        i++
        if (nextDay.dayOfWeek.value == endDayOfWeek) {
          break
        }
      }
    }
    val size = dateList.size
    val numOfDays = 42 - size
    val lastDateTime = dateList[size - 1]
    for (i in 1L..numOfDays) {
      val nextDateTime = lastDateTime.plusDays(i)
      dateList.add(nextDateTime)
    }
    showEvents()
  }

  private fun showEvents() {
    val birthdayTime = dateTimeManager.getBirthdayLocalTime()

    val isRemindersEnabled = prefs.isRemindersInCalendarEnabled

    birthdayTime?.also { widgetDataProvider.setTime(it) }

    if (isRemindersEnabled) {
      widgetDataProvider.setFeature(prefs.isFutureEventEnabled)
    }
    widgetDataProvider.prepare()
    pagerData.clear()

    var dateTime = LocalDateTime.of(LocalDate.now(), LocalTime.now())

    var position = 0
    do {
      val hasReminders =
        widgetDataProvider.hasReminder(dateTime.dayOfMonth, dateTime.monthValue, dateTime.year)
      val hasBirthdays = widgetDataProvider.hasBirthday(dateTime.dayOfMonth, dateTime.monthValue)
      pagerData.add(
        WidgetItem(
          dateTime.dayOfMonth,
          dateTime.monthValue,
          dateTime.year,
          hasReminders,
          hasBirthdays
        )
      )
      position++
      dateTime = dateTime.plusDays(1)
    } while (position < Configs.MAX_DAYS_COUNT)
  }

  override fun onDestroy() {
    dateList.clear()
    pagerData.clear()
  }

  override fun getCount(): Int {
    return dateList.size
  }

  override fun getViewAt(i: Int): RemoteViews {
    val bgColor = prefsProvider.getBackground()
    val textColor = if (WidgetUtils.isDarkBg(bgColor)) {
      ContextCompat.getColor(context, R.color.pureWhite)
    } else {
      ContextCompat.getColor(context, R.color.pureBlack)
    }
    val prefsMonth = prefsProvider.getMonth()
    val rv = RemoteViews(context.packageName, R.layout.list_item_month_grid)

    val selDay = dateList[i].dayOfMonth
    val selMonth = dateList[i].monthValue
    val selYear = dateList[i].year

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

    val markHeight = prefsProvider.getRowHeightDp() / 3f
    val markHeightPx = context.dp2px(markHeight.toInt())
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      rv.setViewLayoutHeight(R.id.currentMark, markHeight, TypedValue.COMPLEX_UNIT_DIP)
      rv.setViewLayoutHeight(R.id.reminderMark, markHeight, TypedValue.COMPLEX_UNIT_DIP)
      rv.setViewLayoutHeight(R.id.birthdayMark, markHeight, TypedValue.COMPLEX_UNIT_DIP)
    } else {
      rv.setInt(R.id.currentMark, "setHeight", markHeightPx)
      rv.setInt(R.id.reminderMark, "setHeight", markHeightPx)
      rv.setInt(R.id.birthdayMark, "setHeight", markHeightPx)
    }

    if (pagerData.size > 0) {
      for (item in pagerData) {
        val day = item.day
        val month = item.month + 1
        val year = item.year
        if (day == selDay && month == selMonth) {
          if (item.isHasReminders && year == selYear) {
            rv.setInt(
              R.id.reminderMark, "setBackgroundColor",
              ThemeProvider.colorReminderCalendar(context, prefs)
            )
          } else {
            rv.setInt(R.id.reminderMark, "setBackgroundColor", Color.TRANSPARENT)
          }
          if (item.isHasBirthdays) {
            rv.setInt(
              R.id.birthdayMark, "setBackgroundColor",
              ThemeProvider.colorBirthdayCalendar(context, prefs)
            )
          } else {
            rv.setInt(R.id.birthdayMark, "setBackgroundColor", Color.TRANSPARENT)
          }
          break
        }
      }
    }

    if (mDay == selDay && mMonth == selMonth && mYear == realYear && mMonth == realMonth + 1
      && mYear == selYear
    ) {
      rv.setInt(
        R.id.currentMark,
        "setBackgroundColor",
        ThemeProvider.colorCurrentCalendar(context, prefs)
      )
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