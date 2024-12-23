package com.github.naz013.appwidgets.calendar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.github.naz013.appwidgets.AppWidgetActionActivity
import com.github.naz013.appwidgets.Direction
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.RandomRequestCode
import com.github.naz013.appwidgets.WidgetPrefsHolder
import com.github.naz013.appwidgets.WidgetUtils
import com.github.naz013.common.intent.PendingIntentWrapper
import com.github.naz013.logging.Logger
import com.github.naz013.ui.common.context.intentForClass
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar
import java.util.Formatter
import java.util.GregorianCalendar
import java.util.Locale

internal class CalendarWidget : AppWidgetProvider(), KoinComponent {

  private val widgetPrefsHolder by inject<WidgetPrefsHolder>()

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    for (id in appWidgetIds) {
      updateWidget(
        context = context,
        appWidgetManager = appWidgetManager,
        sp = widgetPrefsHolder.findOrCreate(
          id,
          CalendarWidgetPrefsProvider::class.java
        )
      )
    }
    super.onUpdate(context, appWidgetManager, appWidgetIds)
  }

  override fun onAppWidgetOptionsChanged(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    newOptions: Bundle?
  ) {
    updateWidget(
      context = context,
      appWidgetManager = appWidgetManager,
      sp = widgetPrefsHolder.findOrCreate(
        appWidgetId,
        CalendarWidgetPrefsProvider::class.java
      )
    )
    super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
  }

  companion object {

    fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      sp: CalendarWidgetPrefsProvider
    ) {
      val options = appWidgetManager.getAppWidgetOptions(sp.widgetId)
      val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
      val height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
      if (height != 0) {
        val rowHeight = (height - 58).toFloat() / 7f
        sp.setRowHeight(rowHeight)
        Logger.d("CALENDAR WIDGET SIZE w=$width, h=$height, row=$rowHeight")
      }

      val cal = GregorianCalendar()
      cal.set(Calendar.MONTH, sp.getMonth())
      cal.set(Calendar.YEAR, sp.getYear())
      val monthYearStringBuilder = StringBuilder(50)
      val monthYearFormatter = Formatter(
        monthYearStringBuilder,
        Locale.getDefault()
      )
      val monthYearFlag = (
        DateUtils.FORMAT_SHOW_DATE or
          DateUtils.FORMAT_NO_MONTH_DAY or
          DateUtils.FORMAT_SHOW_YEAR
        )
      val date = DateUtils.formatDateRange(
        context,
        monthYearFormatter,
        cal.timeInMillis,
        cal.timeInMillis,
        monthYearFlag
      ).toString().uppercase()

      val headerBgColor = sp.getHeaderBackground()
      val bgColor = sp.getBackground()

      val rv = RemoteViews(context.packageName, R.layout.widget_calendar)

      rv.setInt(R.id.headerBg, "setBackgroundResource", WidgetUtils.newWidgetBg(headerBgColor))
      rv.setInt(R.id.widgetBg, "setBackgroundResource", WidgetUtils.newWidgetBg(bgColor))

      rv.setTextViewText(R.id.widgetTitle, date)

      if (WidgetUtils.isDarkBg(headerBgColor)) {
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_settings,
          color = R.color.pureWhite,
          viewId = R.id.btn_settings,
          cls = CalendarWidgetConfigActivity::class.java
        ) {
          it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, sp.widgetId)
          return@initButton it
        }
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_add,
          color = R.color.pureWhite,
          viewId = R.id.btn_add_task,
          intent = createIntent(context, Direction.ADD_REMINDER)
        )

        WidgetUtils.setIcon(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_chevron_left,
          viewId = R.id.btn_prev,
          color = R.color.pureWhite
        )
        WidgetUtils.setIcon(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_chevron_right,
          viewId = R.id.btn_next,
          color = R.color.pureWhite
        )

        rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureWhite))
      } else {
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_settings,
          color = R.color.pureBlack,
          viewId = R.id.btn_settings,
          cls = CalendarWidgetConfigActivity::class.java
        ) {
          it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, sp.widgetId)
          return@initButton it
        }
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_add,
          color = R.color.pureBlack,
          viewId = R.id.btn_add_task,
          intent = createIntent(context, Direction.ADD_REMINDER)
        )

        WidgetUtils.setIcon(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_chevron_left,
          viewId = R.id.btn_prev,
          color = R.color.pureBlack
        )
        WidgetUtils.setIcon(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_fluent_chevron_right,
          viewId = R.id.btn_next,
          color = R.color.pureBlack
        )

        rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureBlack))
      }

      val weekdayAdapter = context.intentForClass(CalendarWeekdayService::class.java)
      weekdayAdapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, sp.widgetId)
      rv.setRemoteAdapter(R.id.weekdayGrid, weekdayAdapter)

      val startActivityIntent = AppWidgetActionActivity.createIntent(context)
      val startActivityPendingIntent = PendingIntentWrapper.getActivity(
        context = context,
        requestCode = RandomRequestCode.generate(),
        intent = startActivityIntent,
        flags = PendingIntent.FLAG_MUTABLE,
        ignoreIn13 = true
      )
      rv.setPendingIntentTemplate(R.id.monthGrid, startActivityPendingIntent)

      val monthAdapter = context.intentForClass(CalendarMonthService::class.java)
      monthAdapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, sp.widgetId)
      rv.setRemoteAdapter(R.id.monthGrid, monthAdapter)

      val nextIntent = context.intentForClass(CalendarNextReceiver::class.java)
      nextIntent.action = CalendarNextReceiver.ACTION_NEXT
      nextIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, sp.widgetId)
      val nextPendingIntent = PendingIntentWrapper.getBroadcast(
        context = context,
        requestCode = sp.widgetId,
        intent = nextIntent,
        flags = PendingIntent.FLAG_MUTABLE,
        ignoreIn13 = true
      )
      rv.setOnClickPendingIntent(R.id.btn_next, nextPendingIntent)

      Logger.d("updateWidget: id = ${sp.widgetId}")

      val previousIntent = context.intentForClass(CalendarPreviousReceiver::class.java)
      previousIntent.action = CalendarPreviousReceiver.ACTION_PREVIOUS
      previousIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, sp.widgetId)
      val previousPendingIntent = PendingIntentWrapper.getBroadcast(
        context = context,
        requestCode = sp.widgetId,
        intent = previousIntent,
        flags = PendingIntent.FLAG_MUTABLE,
        ignoreIn13 = true
      )
      rv.setOnClickPendingIntent(R.id.btn_prev, previousPendingIntent)

      appWidgetManager.updateAppWidget(sp.widgetId, rv)
      appWidgetManager.notifyAppWidgetViewDataChanged(sp.widgetId, R.id.weekdayGrid)
      appWidgetManager.notifyAppWidgetViewDataChanged(sp.widgetId, R.id.monthGrid)
    }

    private fun createIntent(context: Context, direction: Direction): Intent {
      return AppWidgetActionActivity.createIntent(context).apply {
        putExtra(AppWidgetActionActivity.DIRECTION, direction)
      }
    }
  }
}
