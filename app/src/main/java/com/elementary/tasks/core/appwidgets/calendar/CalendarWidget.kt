package com.elementary.tasks.core.appwidgets.calendar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.appwidgets.WidgetPrefsHolder
import com.elementary.tasks.core.appwidgets.WidgetUtils
import com.elementary.tasks.core.appwidgets.buttons.VoiceWidgetDialog
import com.elementary.tasks.core.os.PendingIntentWrapper
import com.elementary.tasks.core.utils.intentForClass
import com.elementary.tasks.home.BottomNavActivity
import com.elementary.tasks.reminder.create.CreateReminderActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.Calendar
import java.util.Formatter
import java.util.GregorianCalendar
import java.util.Locale

class CalendarWidget : AppWidgetProvider(), KoinComponent {

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
        Timber.d("CALENDAR WIDGET SIZE w=$width, h=$height, row=$rowHeight")
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
          iconId = R.drawable.ic_twotone_settings_24px,
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
          iconId = R.drawable.ic_twotone_add_24px,
          color = R.color.pureWhite,
          viewId = R.id.btn_add_task,
          cls = CreateReminderActivity::class.java
        )
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_twotone_mic_24px,
          color = R.color.pureWhite,
          viewId = R.id.btn_voice,
          cls = VoiceWidgetDialog::class.java
        )

        WidgetUtils.setIcon(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_twotone_keyboard_arrow_left_24px,
          viewId = R.id.btn_prev,
          color = R.color.pureWhite
        )
        WidgetUtils.setIcon(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_twotone_keyboard_arrow_right_24px,
          viewId = R.id.btn_next,
          color = R.color.pureWhite
        )

        rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureWhite))
      } else {
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_twotone_settings_24px,
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
          iconId = R.drawable.ic_twotone_add_24px,
          color = R.color.pureBlack,
          viewId = R.id.btn_add_task,
          cls = CreateReminderActivity::class.java
        )
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_twotone_mic_24px,
          color = R.color.pureBlack,
          viewId = R.id.btn_voice,
          cls = VoiceWidgetDialog::class.java
        )

        WidgetUtils.setIcon(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_twotone_keyboard_arrow_left_24px,
          viewId = R.id.btn_prev,
          color = R.color.pureBlack
        )
        WidgetUtils.setIcon(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_twotone_keyboard_arrow_right_24px,
          viewId = R.id.btn_next,
          color = R.color.pureBlack
        )

        rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureBlack))
      }

      val weekdayAdapter = context.intentForClass(CalendarWeekdayService::class.java)
      weekdayAdapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, sp.widgetId)
      rv.setRemoteAdapter(R.id.weekdayGrid, weekdayAdapter)

      val startActivityIntent = context.intentForClass(BottomNavActivity::class.java)
      startActivityIntent.action = Intent.ACTION_VIEW
      val startActivityPendingIntent = PendingIntentWrapper.getActivity(
        context = context,
        requestCode = 0,
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

      Timber.d("updateWidget: id = ${sp.widgetId}")

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
  }
}
