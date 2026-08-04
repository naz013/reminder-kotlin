package com.github.naz013.appwidgets.compose

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import com.github.naz013.appwidgets.WidgetUtils
import com.github.naz013.common.system.Module

internal fun GlanceModifier.roundedBackground(color: Int): GlanceModifier {
  return if (Module.is12) {
    this.cornerRadius(8.dp)
      .background(WidgetUtils.getComposeColor(color))
  } else {
    this.background(
      imageProvider = ImageProvider(WidgetUtils.newWidgetBg(color))
    )
  }
}

/**
 * Clips the widget's outer silhouette to the OEM launcher's system corner radius (API 31+, where
 * defined) instead of a hardcoded value, so the widget's outline matches every other widget on
 * that home screen. Intended for the top-level container only - inner elements (header, list
 * items) keep their own [roundedBackground].
 */
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
