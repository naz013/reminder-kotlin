package com.github.naz013.ui.common.compose.foundation.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.icon.DrawableCatalog

private val BADGE_SIZE = 40.dp
private const val ICON_SIZE_FRACTION = 0.55f
private val DOT_SIZE = 12.dp
private const val LIGHT_BACKGROUND_LUMINANCE = 0.5f

/**
 * Round badge showing a reminder's chosen icon on its chosen tag color. With only a color it
 * collapses to a small dot; with only an icon it uses the neutral surface color. Renders nothing
 * when both are null.
 */
@Composable
fun ReminderTagBadge(
  modifier: Modifier = Modifier,
  @DrawableRes iconRes: Int?,
  tagColor: Int?,
  size: Dp = BADGE_SIZE,
) {
  if (iconRes == null && tagColor == null) return
  val background = tagColor?.let { Color(it) } ?: MaterialTheme.colorScheme.surfaceContainerHigh
  val content = when {
    tagColor == null -> MaterialTheme.colorScheme.onSurface
    background.luminance() > LIGHT_BACKGROUND_LUMINANCE -> Color.Black
    else -> Color.White
  }
  Box(
    modifier = modifier
      .size(if (iconRes == null) DOT_SIZE else size)
      .clip(CircleShape)
      .background(background),
    contentAlignment = Alignment.Center,
  ) {
    if (iconRes != null) {
      Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = content,
        modifier = Modifier.size(size * ICON_SIZE_FRACTION),
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun ReminderTagBadgePreview() {
  AppTheme {
    ReminderTagBadge(iconRes = DrawableCatalog.Fluent.Home, tagColor = 0xFF86E3CE.toInt())
  }
}
