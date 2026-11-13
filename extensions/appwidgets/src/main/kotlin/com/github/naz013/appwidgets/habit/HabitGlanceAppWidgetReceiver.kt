package com.github.naz013.appwidgets.habit

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class HabitGlanceAppWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = HabitGlanceAppWidget()

  override fun onDeleted(context: Context, appWidgetIds: IntArray) {
    super.onDeleted(context, appWidgetIds)
    for (widgetId in appWidgetIds) {
      HabitWidgetPrefsProvider(context, widgetId).clear()
    }
  }
}
