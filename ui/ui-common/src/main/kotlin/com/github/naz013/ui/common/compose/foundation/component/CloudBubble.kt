package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

private val BUBBLE_SPACING = 8.dp
private val BUBBLE_MARGIN = 20.dp
private val TAIL_WIDTH = 16.dp
private val TAIL_HEIGHT = 8.dp
private val BUBBLE_CORNER_RADIUS = 16.dp

/** Which side of the bubble the anchor is on - and therefore which side the tail is drawn on. */
private enum class BubbleEdge {
  /** Anchor is below the bubble: tail at the bottom, pointing down. */
  ANCHOR_BELOW,

  /** Anchor is above the bubble: tail at the top, pointing up. */
  ANCHOR_ABOVE,
}

/**
 * A speech-bubble-shaped popup, anchored to whichever composable hosts it (same anchoring model
 * as [androidx.compose.ui.window.Popup]): floats above the anchor with a small triangular tail
 * pointing down at it by default, but flips to sit below the anchor with an upward-pointing tail
 * when there isn't enough room above (e.g. an anchor in a top app bar) - clamped to stay on-screen
 * either way, keeping the tail visually aligned with the anchor's center.
 */
@Composable
fun CloudBubble(
  onDismissRequest: () -> Unit,
  containerColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  val density = LocalDensity.current
  var arrowFraction by remember { mutableFloatStateOf(0.5f) }
  var edge by remember { mutableStateOf(BubbleEdge.ANCHOR_BELOW) }
  val positionProvider =
    remember(density) {
      CloudBubblePositionProvider(density, onArrowFraction = { arrowFraction = it }, onEdge = { edge = it })
    }

  Popup(
    popupPositionProvider = positionProvider,
    onDismissRequest = onDismissRequest,
    properties = PopupProperties(focusable = true),
  ) {
    Surface(
      modifier = modifier,
      shape = cloudBubbleShape(arrowFraction, edge, density),
      color = containerColor,
      contentColor = contentColor,
      shadowElevation = 6.dp,
      tonalElevation = 4.dp,
    ) {
      Box(
        modifier =
          Modifier.padding(
            when (edge) {
              BubbleEdge.ANCHOR_BELOW -> PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp + TAIL_HEIGHT)
              BubbleEdge.ANCHOR_ABOVE -> PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp + TAIL_HEIGHT, bottom = 12.dp)
            },
          ),
      ) {
        content()
      }
    }
  }
}

private fun cloudBubbleShape(
  arrowFraction: Float,
  edge: BubbleEdge,
  density: Density,
) = GenericShape { size, _ ->
  val tailWidthPx = with(density) { TAIL_WIDTH.toPx() }
  val tailHeightPx = with(density) { TAIL_HEIGHT.toPx() }
  val cornerPx = with(density) { BUBBLE_CORNER_RADIUS.toPx() }
  val bodyHeight = (size.height - tailHeightPx).coerceAtLeast(0f)
  val arrowX =
    (size.width * arrowFraction).coerceIn(
      tailWidthPx,
      (size.width - tailWidthPx).coerceAtLeast(tailWidthPx),
    )

  when (edge) {
    BubbleEdge.ANCHOR_BELOW -> {
      addRoundRect(
        RoundRect(
          left = 0f,
          top = 0f,
          right = size.width,
          bottom = bodyHeight,
          cornerRadius = CornerRadius(cornerPx, cornerPx),
        ),
      )
      moveTo(arrowX - tailWidthPx / 2, bodyHeight)
      lineTo(arrowX, size.height)
      lineTo(arrowX + tailWidthPx / 2, bodyHeight)
      close()
    }

    BubbleEdge.ANCHOR_ABOVE -> {
      addRoundRect(
        RoundRect(
          left = 0f,
          top = tailHeightPx,
          right = size.width,
          bottom = size.height,
          cornerRadius = CornerRadius(cornerPx, cornerPx),
        ),
      )
      moveTo(arrowX - tailWidthPx / 2, tailHeightPx)
      lineTo(arrowX, 0f)
      lineTo(arrowX + tailWidthPx / 2, tailHeightPx)
      close()
    }
  }
}

private class CloudBubblePositionProvider(
  private val density: Density,
  private val onArrowFraction: (Float) -> Unit,
  private val onEdge: (BubbleEdge) -> Unit,
) : PopupPositionProvider {
  override fun calculatePosition(
    anchorBounds: IntRect,
    windowSize: IntSize,
    layoutDirection: LayoutDirection,
    popupContentSize: IntSize,
  ): IntOffset {
    val marginPx = with(density) { BUBBLE_MARGIN.toPx() }.toInt()
    val spacingPx = with(density) { BUBBLE_SPACING.toPx() }.toInt()

    val anchorCenterX = anchorBounds.left + anchorBounds.width / 2
    val idealLeft = anchorCenterX - popupContentSize.width / 2
    val maxLeft = (windowSize.width - popupContentSize.width - marginPx).coerceAtLeast(marginPx)
    val clampedLeft = idealLeft.coerceIn(marginPx, maxLeft)

    val arrowX = anchorCenterX - clampedLeft
    val fraction =
      if (popupContentSize.width > 0) {
        arrowX.toFloat() / popupContentSize.width
      } else {
        0.5f
      }
    onArrowFraction(fraction.coerceIn(0.12f, 0.88f))

    // Prefer sitting above the anchor (tail pointing down at it); only flip to below (tail
    // pointing up) when there genuinely isn't room above - e.g. an anchor inside a top app bar -
    // so the bubble never has to be clamped on top of its own anchor.
    val spaceAbove = anchorBounds.top - marginPx
    val fitsAbove = spaceAbove - spacingPx >= popupContentSize.height
    val edge = if (fitsAbove) BubbleEdge.ANCHOR_BELOW else BubbleEdge.ANCHOR_ABOVE
    onEdge(edge)

    val idealTop =
      if (edge == BubbleEdge.ANCHOR_BELOW) {
        anchorBounds.top - popupContentSize.height - spacingPx
      } else {
        anchorBounds.bottom + spacingPx
      }
    val maxTop = (windowSize.height - popupContentSize.height - marginPx).coerceAtLeast(marginPx)
    val clampedTop = idealTop.coerceIn(marginPx, maxTop)

    return IntOffset(clampedLeft, clampedTop)
  }
}
