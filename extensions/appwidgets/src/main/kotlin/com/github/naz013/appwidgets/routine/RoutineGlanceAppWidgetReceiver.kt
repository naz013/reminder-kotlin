package com.github.naz013.appwidgets.routine

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class RoutineGlanceAppWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = RoutineGlanceAppWidget()

  override fun onDeleted(context: Context, appWidgetIds: IntArray) {
    super.onDeleted(context, appWidgetIds)
    for (widgetId in appWidgetIds) {
      RoutineWidgetPrefsProvider(context, widgetId).clear()
    }
  }
}
