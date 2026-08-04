package com.elementary.tasks.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.elementary.tasks.R

/**
 * Compose can't load `@mipmap/ic_launcher_round` directly - `painterResource` doesn't support the
 * `<adaptive-icon>` XML format, only its leaf drawables - so this recreates the round launcher icon
 * from those same leaves: the [R.drawable.ic_launcher_background] gradient clipped to a circle, with
 * [R.drawable.ic_launcher_foreground] centered on top, inset the way the OS insets adaptive icon
 * layers within their mask.
 */
@Composable
fun AppLauncherIcon(modifier: Modifier = Modifier) {
  Box(
    modifier =
      modifier
        .clip(CircleShape)
        .background(Brush.horizontalGradient(LAUNCHER_GRADIENT_COLORS)),
    contentAlignment = Alignment.Center,
  ) {
    Image(
      painter = painterResource(R.drawable.ic_launcher_foreground),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(FOREGROUND_SAFE_ZONE_FRACTION),
    )
  }
}

private val LAUNCHER_GRADIENT_COLORS =
  listOf(
    Color(0xFF0B4EDF),
    Color(0xFF005FE5),
    Color(0xFF006EE8),
    Color(0xFF007BE9),
    Color(0xFF0088E9),
    Color(0xFF0095E7),
    Color(0xFF03A1E5),
    Color(0xFF39ACE2),
  )

private const val FOREGROUND_SAFE_ZONE_FRACTION = 0.65f
