package com.elementary.tasks.core.app_widgets.birthdays

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.elementary.tasks.R
import com.elementary.tasks.birthdays.create.AddBirthdayActivity
import com.elementary.tasks.core.app_widgets.AppWidgetActionActivity
import com.elementary.tasks.core.app_widgets.WidgetUtils
import com.elementary.tasks.core.os.PendingIntentWrapper

class BirthdaysWidget : AppWidgetProvider() {

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray
  ) {
    for (id in appWidgetIds) {
      updateWidget(context, appWidgetManager, BirthdaysWidgetPrefsProvider(context, id))
    }
    super.onUpdate(context, appWidgetManager, appWidgetIds)
  }

  companion object {

    fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      prefsProvider: BirthdaysWidgetPrefsProvider
    ) {
      val rv = RemoteViews(context.packageName, R.layout.widget_birthdays)

      val headerBgColor = prefsProvider.getHeaderBackground()

      rv.setInt(R.id.headerBg, "setBackgroundResource", WidgetUtils.newWidgetBg(headerBgColor))

      val colorRes = if (WidgetUtils.isDarkBg(headerBgColor)) {
        R.color.pureWhite
      } else {
        R.color.pureBlack
      }

      WidgetUtils.initButton(
        context,
        rv,
        R.drawable.ic_twotone_settings_24px,
        colorRes,
        R.id.btn_settings,
        BirthdaysWidgetConfigActivity::class.java
      ) {
        it.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefsProvider.widgetId)
        return@initButton it
      }
      WidgetUtils.initButton(
        context,
        rv,
        R.drawable.ic_twotone_add_24px,
        colorRes,
        R.id.btn_add,
        AddBirthdayActivity::class.java
      )
      rv.setTextColor(R.id.widgetTitle, ContextCompat.getColor(context, colorRes))

      val startActivityIntent = AppWidgetActionActivity.createIntent(context)
      val startActivityPendingIntent = PendingIntentWrapper.getActivity(
        context,
        0,
        startActivityIntent,
        PendingIntent.FLAG_MUTABLE,
        ignoreIn13 = true
      )
      rv.setPendingIntentTemplate(android.R.id.list, startActivityPendingIntent)

      val adapter = Intent(context, BirthdaysService::class.java)
      adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, prefsProvider.widgetId)
      rv.setRemoteAdapter(android.R.id.list, adapter)
      appWidgetManager.updateAppWidget(prefsProvider.widgetId, rv)
      appWidgetManager.notifyAppWidgetViewDataChanged(prefsProvider.widgetId, android.R.id.list)
    }
  }
}
