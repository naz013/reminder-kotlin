package com.elementary.tasks.core.app_widgets.calendar

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.params.Prefs
import org.threeten.bp.LocalDate

class CalendarWeekdayFactory(
  private val context: Context,
  intent: Intent,
  private val prefs: Prefs,
  private val dateTimeManager: DateTimeManager
) : RemoteViewsService.RemoteViewsFactory {

  private val mWeekdaysList = ArrayList<String>()
  private val mWidgetId: Int = intent.getIntExtra(
    AppWidgetManager.EXTRA_APPWIDGET_ID,
    AppWidgetManager.INVALID_APPWIDGET_ID
  )

  override fun onCreate() {
    mWeekdaysList.clear()
  }

  override fun onDataSetChanged() {
    mWeekdaysList.clear()
    var date = if (isSunday()) {
      LocalDate.of(2022, 12, 25)
    } else {
      LocalDate.of(2022, 12, 26)
    }
    for (i in 0 until 7) {
      mWeekdaysList.add(dateTimeManager.formatCalendarWeekday(date).uppercase())
      date = date.plusDays(1)
    }
  }

  override fun onDestroy() {
    mWeekdaysList.clear()
  }

  override fun getCount(): Int {
    return mWeekdaysList.size
  }

  override fun getViewAt(i: Int): RemoteViews {
    val sp = context.getSharedPreferences(CalendarWidgetConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE)
    val bgColor = sp.getInt(CalendarWidgetConfigActivity.WIDGET_BG + mWidgetId, 0)
    val textColor = if (WidgetUtils.isDarkBg(bgColor)) {
      ContextCompat.getColor(context, R.color.pureWhite)
    } else {
      ContextCompat.getColor(context, R.color.pureBlack)
    }

    val rv = RemoteViews(context.packageName, R.layout.list_item_weekday_grid)
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

  private fun isSunday(): Boolean {
    return prefs.startDay == 0
  }
}
