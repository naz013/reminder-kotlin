package com.github.naz013.appwidgets.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

@Composable
fun EmptyData(
  modifier: GlanceModifier = GlanceModifier,
  text: String,
  color: ColorProvider,
) {
  Text(
    text = text,
    modifier = modifier,
    style = TextStyle(
      fontSize = 22.sp,
      color = color,
    )
  )
}
