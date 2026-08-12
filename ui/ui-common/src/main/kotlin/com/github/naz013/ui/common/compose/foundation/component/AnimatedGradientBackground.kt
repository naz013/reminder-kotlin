package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview

/**
 * A full-size [Box] whose background is a linear gradient that slowly drifts back and forth,
 * giving otherwise static/empty screens a bit of life. Colors default to the current theme's
 * container colors so it fits any screen without extra configuration.
 *
 * Usage:
 * ```
 * AnimatedGradientBackground {
 *   // screen content, drawn on top of the gradient
 * }
 * ```
 */
@Composable
fun AnimatedGradientBackground(
  modifier: Modifier = Modifier,
  colors: List<Color> = defaultAnimatedGradientColors(),
  durationMillis: Int = 8000,
  content: @Composable BoxScope.() -> Unit = {},
) {
  val infiniteTransition = rememberInfiniteTransition(label = "animatedGradientBackground")
  val animatedProgress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec =
      infiniteRepeatable(
        animation = tween(durationMillis = durationMillis, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse,
      ),
    label = "animatedGradientProgress",
  )

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val density = LocalDensity.current
    val widthPx = with(density) { maxWidth.toPx() }
    val heightPx = with(density) { maxHeight.toPx() }
    val shift = animatedProgress * widthPx

    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(
            brush =
              Brush.linearGradient(
                colors = colors,
                start = Offset(-widthPx + shift, 0f),
                end = Offset(shift, heightPx),
              ),
          ),
      content = content,
    )
  }
}

@Composable
fun defaultAnimatedGradientColors(): List<Color> =
  listOf(
    MaterialTheme.colorScheme.primaryContainer,
    MaterialTheme.colorScheme.tertiaryContainer,
    MaterialTheme.colorScheme.secondaryContainer,
  )

@Preview(showBackground = true)
@Composable
private fun AnimatedGradientBackgroundPreview() {
  AnimatedGradientBackground()
}
