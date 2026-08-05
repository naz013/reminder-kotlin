package com.github.naz013.appwidgets.combinedbuttons

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class CombinedButtonsGlanceAppWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = CombinedButtonsGlanceAppWidget()
}
