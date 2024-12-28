package com.github.naz013.appwidgets.compose

import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import com.github.naz013.appwidgets.WidgetUtils
import com.github.naz013.common.Module

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
