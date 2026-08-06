package com.github.naz013.appwidgets.compose

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background

@Composable
internal fun GlanceModifier.roundedBackground(color: Color): GlanceModifier {
  return this.cornerRadius(8.dp)
    .background(color)
}

@Composable
internal fun GlanceModifier.systemWidgetShape(): GlanceModifier {
  var modifier = this.appWidgetBackground()
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val context = LocalContext.current
    val hasSystemRadius = runCatching {
      context.resources.getResourceName(android.R.dimen.system_app_widget_background_radius)
    }.isSuccess
    if (hasSystemRadius) {
      modifier = modifier.cornerRadius(android.R.dimen.system_app_widget_background_radius)
    }
  }
  return modifier
}
