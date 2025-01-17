package com.github.naz013.appwidgets.calendar

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.github.naz013.appwidgets.AppWidgetActionActivity
import com.github.naz013.appwidgets.AppWidgetPreferences
import com.github.naz013.appwidgets.Direction
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.WidgetPrefsHolder
import com.github.naz013.appwidgets.WidgetUtils
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.domain.calendar.StartDayOfWeekProtocol
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.DeepLinkDestination
import com.github.naz013.navigation.DayViewScreen
import com.github.naz013.ui.common.context.dp2px
import com.github.naz013.ui.common.theme.ThemeProvider
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

internal class CalendarMonthFactory(
  intent: Intent,
  private val context: Context,
  private val widgetDataProvider: WidgetDataProvider,
  private val dateTimeManager: DateTimeManager,
  widgetPrefsHolder: WidgetPrefsHolder,
  private val appWidgetPreferences: AppWidgetPreferences
) : RemoteViewsService.RemoteViewsFactory {

  private val dateList = ArrayList<LocalDate>()
  private val pagerData = ArrayList<WidgetItem>()
  private val widgetId: Int = intent.getIntExtra(
    AppWidgetManager.EXTRA_APPWIDGET_ID,
    AppWidgetManager.INVALID_APPWIDGET_ID
  )
  private val prefsProvider: CalendarWidgetPrefsProvider = widgetPrefsHolder.findOrCreate(
    widgetId,
    CalendarWidgetPrefsProvider::class.java
  )

  override fun onCreate() {
    dateList.clear()
    pagerData.clear()
  }

  override fun onDataSetChanged() {
    dateList.clear()

    var year = prefsProvider.getYear()

    val mMonth = prefsProvider.getMonth() + 1

    if (year < 1) {
      year = LocalDate.now().year
    }
    val mYear = year

    Logger.d("onDataSetChanged: mMonth=$mMonth, mYear=$mYear")

    val firstDateOfMonth = LocalDate.of(mYear, mMonth, 1)
    val lastDateOfMonth = firstDateOfMonth.plusDays(firstDateOfMonth.lengthOfMonth() - 1L)

    var weekdayOfFirstDate = firstDateOfMonth.dayOfWeek.value
    val startDayOfWeek = StartDayOfWeekProtocol(appWidgetPreferences.startDay).getForCalendar()

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

    val isRemindersEnabled = appWidgetPreferences.isRemindersInCalendarEnabled

    birthdayTime?.also { widgetDataProvider.setTime(it) }

    if (isRemindersEnabled) {
      widgetDataProvider.setFuture(appWidgetPreferences.isFutureEventEnabled)
    }
    widgetDataProvider.prepare()
    pagerData.clear()

    var localDate = LocalDate.now()
    var position = 0
    do {
      val hasReminders = widgetDataProvider.hasReminder(localDate)
      val hasBirthdays = widgetDataProvider.hasBirthday(localDate.dayOfMonth, localDate.monthValue)
      pagerData.add(WidgetItem(localDate, hasReminders, hasBirthdays))
      position++
      localDate = localDate.plusDays(1)
    } while (position < WidgetDataProvider.MAX_DAYS_COUNT)
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
    val prefsMonth = prefsProvider.getMonth() + 1
    val rv = RemoteViews(context.packageName, R.layout.list_item_month_grid)

    val selDate = dateList[i]
    val nowDate = LocalDate.now()

    rv.setTextViewText(R.id.textView, selDate.dayOfMonth.toString())
    if (selDate.monthValue == prefsMonth) {
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
        val itemDate = item.date

        if (itemDate.dayOfMonth == selDate.dayOfMonth &&
          itemDate.monthValue == selDate.monthValue
        ) {
          if (item.isHasReminders && itemDate.year == selDate.year) {
            rv.setInt(
              R.id.reminderMark,
              "setBackgroundColor",
              ThemeProvider.colorReminderCalendar(context, appWidgetPreferences.reminderColor)
            )
          } else {
            rv.setInt(R.id.reminderMark, "setBackgroundColor", Color.TRANSPARENT)
          }
          if (item.isHasBirthdays) {
            rv.setInt(
              R.id.birthdayMark,
              "setBackgroundColor",
              ThemeProvider.colorBirthdayCalendar(context, appWidgetPreferences.birthdayColor)
            )
          } else {
            rv.setInt(R.id.birthdayMark, "setBackgroundColor", Color.TRANSPARENT)
          }
          break
        }
      }
    }

    if (selDate == nowDate) {
      rv.setInt(
        R.id.currentMark,
        "setBackgroundColor",
        ThemeProvider.colorTodayCalendar(context, appWidgetPreferences.todayColor)
      )
    } else {
      rv.setInt(R.id.currentMark, "setBackgroundColor", Color.TRANSPARENT)
    }

    val bundle = Bundle().apply {
      putLong(
        "date",
        dateTimeManager.toMillis(LocalDateTime.of(selDate, LocalTime.now()))
      )
    }

    val fillInIntent = Intent()
    fillInIntent.putExtra(AppWidgetActionActivity.DIRECTION, Direction.HOME)
    fillInIntent.putExtra(DeepLinkDestination.KEY, DayViewScreen(bundle))
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
    val date: LocalDate,
    val isHasReminders: Boolean,
    val isHasBirthdays: Boolean
  )
}
