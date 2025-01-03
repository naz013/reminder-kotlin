package com.github.naz013.appwidgets.calendar

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.TypedValue
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.github.naz013.appwidgets.AppWidgetPreferences
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.WidgetPrefsHolder
import com.github.naz013.appwidgets.WidgetUtils
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.ui.common.context.dp2px
import org.threeten.bp.LocalDate

internal class CalendarWeekdayFactory(
  private val context: Context,
  intent: Intent,
  private val dateTimeManager: DateTimeManager,
  private val widgetPrefsHolder: WidgetPrefsHolder,
  private val appWidgetPreferences: AppWidgetPreferences
) : RemoteViewsService.RemoteViewsFactory {

  private val weekdaysList = ArrayList<String>()
  private val widgetId: Int = intent.getIntExtra(
    AppWidgetManager.EXTRA_APPWIDGET_ID,
    AppWidgetManager.INVALID_APPWIDGET_ID
  )
  private val prefsProvider = widgetPrefsHolder.findOrCreate(
    widgetId,
    CalendarWidgetPrefsProvider::class.java
  )

  override fun onCreate() {
    weekdaysList.clear()
  }

  override fun onDataSetChanged() {
    weekdaysList.clear()
    var date = if (isSunday()) {
      LocalDate.of(2022, 12, 25)
    } else {
      LocalDate.of(2022, 12, 26)
    }
    for (i in 0 until 7) {
      weekdaysList.add(dateTimeManager.formatCalendarWeekday(date).uppercase())
      date = date.plusDays(1)
    }
  }

  override fun onDestroy() {
    weekdaysList.clear()
  }

  override fun getCount(): Int {
    return weekdaysList.size
  }

  override fun getViewAt(i: Int): RemoteViews {
    val bgColor = prefsProvider.getBackground()
    val textColor = if (WidgetUtils.isDarkBg(bgColor)) {
      ContextCompat.getColor(context, R.color.pureWhite)
    } else {
      ContextCompat.getColor(context, R.color.pureBlack)
    }

    val rv = RemoteViews(context.packageName, R.layout.list_item_weekday_grid)
    if (i >= weekdaysList.size) {
      return rv
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      rv.setViewLayoutHeight(
        R.id.textView1,
        prefsProvider.getRowHeightDp(),
        TypedValue.COMPLEX_UNIT_DIP
      )
    } else {
      rv.setInt(R.id.textView1, "setHeight", context.dp2px(prefsProvider.getRowHeightDp().toInt()))
    }
    rv.setTextViewText(R.id.textView1, weekdaysList[i])
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

  private fun isSunday(): Boolean {
    return appWidgetPreferences.startDay == 0
  }
}
