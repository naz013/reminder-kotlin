package com.elementary.tasks.core.appwidgets.events

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.core.appwidgets.AppWidgetActionActivity
import com.elementary.tasks.core.appwidgets.WidgetUtils
import com.elementary.tasks.core.appwidgets.buttons.VoiceWidgetDialog
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
          context = context,
          rv = rv,
          iconId = R.drawable.ic_twotone_settings_24px,
          color = R.color.pureWhite,
          viewId = R.id.btn_settings,
          cls = EventsWidgetConfigActivity::class.java
        ) {
          it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefsProvider.widgetId)
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
        rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureWhite))
      } else {
        WidgetUtils.initButton(
          context = context,
          rv = rv,
          iconId = R.drawable.ic_twotone_settings_24px,
          color = R.color.pureBlack,
          viewId = R.id.btn_settings,
          cls = EventsWidgetConfigActivity::class.java
        ) {
          it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefsProvider.widgetId)
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
        rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, R.color.pureBlack))
      }

      val startActivityIntent = AppWidgetActionActivity.createIntent(context)
      val startActivityPendingIntent = PendingIntentWrapper.getActivity(
        context = context,
        requestCode = 0,
        intent = startActivityIntent,
        flags = PendingIntent.FLAG_MUTABLE,
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
