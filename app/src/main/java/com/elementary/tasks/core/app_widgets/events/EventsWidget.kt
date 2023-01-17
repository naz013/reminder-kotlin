package com.elementary.tasks.core.app_widgets.events

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.app_widgets.buttons.VoiceWidgetDialog
import com.elementary.tasks.core.os.PendingIntentWrapper
import com.elementary.tasks.reminder.create.CreateReminderActivity
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale

class EventsWidget : AppWidgetProvider() {

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    for (id in appWidgetIds) {
      updateWidget(context, appWidgetManager, EventsWidgetPrefsProvider(context, id))
    }
    super.onUpdate(context, appWidgetManager, appWidgetIds)
  }

  companion object {

    fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      prefsProvider: EventsWidgetPrefsProvider
    ) {
      val cal = GregorianCalendar()
      val dateFormat = SimpleDateFormat("EEE, dd MMMM yyyy", Locale.getDefault())
      dateFormat.calendar = cal
      val date = dateFormat.format(cal.time)

      val rv = RemoteViews(context.packageName, R.layout.widget_current_tasks)
      rv.setTextViewText(R.id.widgetTitle, date)

      val headerBgColor = prefsProvider.getHeaderBackground()

      rv.setInt(R.id.headerBg, "setBackgroundResource", WidgetUtils.newWidgetBg(headerBgColor))

      if (WidgetUtils.isDarkBg(headerBgColor)) {
        WidgetUtils.initButton(
          context, rv, R.drawable.ic_twotone_settings_24px, R.color.pureWhite,
          R.id.btn_settings, EventsWidgetConfigActivity::class.java
        ) {
          it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefsProvider.widgetId)
          return@initButton it
        }
        WidgetUtils.initButton(
          context, rv, R.drawable.ic_twotone_add_24px, R.color.pureWhite,
          R.id.btn_add_task, CreateReminderActivity::class.java
        )
        WidgetUtils.initButton(
          context, rv, R.drawable.ic_twotone_mic_24px, R.color.pureWhite,
          R.id.btn_voice, VoiceWidgetDialog::class.java
        )
        rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureWhite))
      } else {
        WidgetUtils.initButton(
          context, rv, R.drawable.ic_twotone_settings_24px, R.color.pureBlack,
          R.id.btn_settings, EventsWidgetConfigActivity::class.java
        ) {
          it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefsProvider.widgetId)
          return@initButton it
        }
        WidgetUtils.initButton(
          context, rv, R.drawable.ic_twotone_add_24px, R.color.pureBlack,
          R.id.btn_add_task, CreateReminderActivity::class.java
        )
        WidgetUtils.initButton(
          context, rv, R.drawable.ic_twotone_mic_24px, R.color.pureBlack,
          R.id.btn_voice, VoiceWidgetDialog::class.java
        )
        rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureBlack))
      }

      val startActivityIntent = Intent(context, EventActionReceiver::class.java)
      val startActivityPendingIntent = PendingIntentWrapper.getBroadcast(
        context,
        0,
        startActivityIntent,
        PendingIntent.FLAG_MUTABLE,
        ignoreIn13 = true
      )
      rv.setPendingIntentTemplate(android.R.id.list, startActivityPendingIntent)

      val adapter = Intent(context, EventsService::class.java)
      adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefsProvider.widgetId)
      rv.setRemoteAdapter(android.R.id.list, adapter)
      appWidgetManager.updateAppWidget(prefsProvider.widgetId, rv)
      appWidgetManager.notifyAppWidgetViewDataChanged(prefsProvider.widgetId, android.R.id.list)
    }
  }
}
