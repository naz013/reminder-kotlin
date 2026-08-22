package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppTheme
import kotlin.math.cos
import kotlin.math.sin

private const val BladeCount = 12
private const val SpinDurationMillis = 900
private val ContentPullOffset = 56.dp

/**
 * Drop-in replacement for [PullToRefreshBox] that renders an iOS-style radial spinner
 * (a ring of fading blades) instead of the Material circular pull arc, and pushes the
 * list content down as the user drags so the spinner has room to reveal itself above it
 * (like `UIRefreshControl`) instead of floating over the top of the content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPullToRefreshBox(
  isRefreshing: Boolean,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
  state: PullToRefreshState = rememberPullToRefreshState(),
  contentAlignment: Alignment = Alignment.TopStart,
  content: @Composable BoxScope.() -> Unit,
) {
  PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = onRefresh,
    modifier = modifier,
    state = state,
    contentAlignment = contentAlignment,
    indicator = {
      IosStylePullToRefreshIndicator(
        isRefreshing = isRefreshing,
        state = state,
        modifier =
          Modifier
            .align(Alignment.TopCenter)
            .padding(top = 20.dp),
      )
    },
    content = {
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .graphicsLayer {
              translationY = state.distanceFraction.coerceIn(0f, 1f) * ContentPullOffset.toPx()
            },
      ) {
        content()
      }
    },
  )
}

/**
 * An iOS `UIActivityIndicatorView`-style spinner: a ring of tapered blades whose alpha
 * fades around the circle. It scales/fades in as the user drags past [state]'s
 * distanceFraction and rotates continuously while [isRefreshing] is true.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IosStylePullToRefreshIndicator(
  isRefreshing: Boolean,
  state: PullToRefreshState,
  modifier: Modifier = Modifier,
  color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
  size: Dp = 24.dp,
) {
  val pullProgress = state.distanceFraction.coerceIn(0f, 1f)
  val scale = if (isRefreshing) 1f else pullProgress
  if (scale <= 0f) return

  val infiniteTransition = rememberInfiniteTransition(label = "iosSpinnerRotation")
  val rotation =
    if (isRefreshing) {
      infiniteTransition
        .animateFloat(
          initialValue = 0f,
          targetValue = 360f,
          animationSpec = infiniteRepeatable(tween(durationMillis = SpinDurationMillis, easing = LinearEasing)),
          label = "rotation",
        ).value
    } else {
      remember { mutableFloatStateOf(0f) }.floatValue
    }
  val highlightedBlade = (pullProgress * BladeCount).toInt().coerceIn(0, BladeCount - 1)

  Canvas(
    modifier =
      modifier
        .size(size)
        .graphicsLayer {
          scaleX = scale
          scaleY = scale
          alpha = scale
          rotationZ = rotation
        },
  ) {
    val bladeLength = this.size.minDimension * 0.28f
    val bladeWidth = this.size.minDimension * 0.09f
    val radius = this.size.minDimension / 2f - bladeLength / 2f
    val center = Offset(this.size.width / 2f, this.size.height / 2f)

    for (i in 0 until BladeCount) {
      val angleDegrees = (360f / BladeCount) * i
      val angleRadians = Math.toRadians(angleDegrees.toDouble())
      val bladeCenter =
        Offset(
          x = center.x + radius * cos(angleRadians).toFloat(),
          y = center.y + radius * sin(angleRadians).toFloat(),
        )
      val bladeAlpha =
        if (isRefreshing) {
          MinBladeAlpha + (1f - MinBladeAlpha) * (i.toFloat() / BladeCount)
        } else {
          if (i <= highlightedBlade) {
            MinBladeAlpha + (1f - MinBladeAlpha) * (i.toFloat() / BladeCount)
          } else {
            MinBladeAlpha
          }
        }

      rotate(degrees = angleDegrees + 90f, pivot = bladeCenter) {
        drawRoundRect(
          color = color.copy(alpha = bladeAlpha),
          topLeft = Offset(bladeCenter.x - bladeWidth / 2f, bladeCenter.y - bladeLength / 2f),
          size = Size(bladeWidth, bladeLength),
          cornerRadius = CornerRadius(bladeWidth / 2f),
        )
      }
    }
  }
}

private const val MinBladeAlpha = 0.15f

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun IosStylePullToRefreshIndicatorIdlePreview() {
  AppTheme {
    Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
      IosStylePullToRefreshIndicator(
        isRefreshing = false,
        state = PreviewPullToRefreshState(distanceFraction = 0.6f),
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun IosStylePullToRefreshIndicatorRefreshingPreview() {
  AppTheme {
    Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
      IosStylePullToRefreshIndicator(
        isRefreshing = true,
        state = PreviewPullToRefreshState(distanceFraction = 1f),
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, heightDp = 400)
@Composable
private fun AppPullToRefreshBoxPreview() {
  AppTheme {
    AppPullToRefreshBox(
      isRefreshing = true,
      onRefresh = {},
    ) {
      LazyColumn(modifier = Modifier.size(width = 300.dp, height = 400.dp)) {
        items(10) { index -> Text(text = "Item $index", modifier = Modifier.padding(16.dp)) }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
private class PreviewPullToRefreshState(
  override val distanceFraction: Float,
) : PullToRefreshState {
  override val isAnimating: Boolean = false

  override suspend fun animateToThreshold() = Unit

  override suspend fun animateToHidden() = Unit

  override suspend fun snapTo(targetValue: Float) = Unit
}
