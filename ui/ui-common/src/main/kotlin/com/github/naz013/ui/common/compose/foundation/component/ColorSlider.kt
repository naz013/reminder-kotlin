package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppTheme
import kotlin.math.roundToInt

private val SelectorStrokeWidth = 2.dp
private const val UnselectedItemVerticalInset = 0.1f
private const val SelectorContrastLuminanceThreshold = 0.5f

@Composable
fun ColorSlider(
  colors: List<Color>,
  selectedIndex: Int,
  onColorSelected: (Int) -> Unit,
  modifier: Modifier = Modifier,
  contentDescription: String = stringResource(R.string.acc_select_color),
  selectorColor: Color = MaterialTheme.colorScheme.onSurface,
  enabled: Boolean = true,
  hapticFeedbackEnabled: Boolean = true,
) {
  val hapticFeedback = LocalHapticFeedback.current

  val currentOnColorSelected by rememberUpdatedState(onColorSelected)
  val currentSelectedIndex by rememberUpdatedState(selectedIndex)
  val gestureModifier = if (enabled && colors.isNotEmpty()) {
    Modifier.pointerInput(colors.size) {
      fun selectAt(x: Float) {
        val itemWidth = size.width / colors.size.toFloat()
        val index = (x / itemWidth).toInt().coerceIn(0, colors.size - 1)
        if (hapticFeedbackEnabled && currentSelectedIndex != index) {
          hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
        }
        currentOnColorSelected(index)
      }
      awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        selectAt(down.position.x)
        val pointerId = down.id
        while (true) {
          val event = awaitPointerEvent()
          val change = event.changes.firstOrNull { it.id == pointerId } ?: break
          if (!change.pressed) break
          change.consume()
          selectAt(change.position.x)
        }
      }
    }
  } else {
    Modifier
  }

  // Gives TalkBack/switch-access an adjustable ("slider-equivalent") role: focusing the strip
  // announces the content description and current position, and swiping up/down invokes
  // setProgress to move between swatches - there's otherwise no clickable/scrollable semantics
  // at all on a bare Canvas + pointerInput like this one.
  val semanticsModifier = Modifier.semantics {
    this.contentDescription = contentDescription
    if (colors.isNotEmpty()) {
      progressBarRangeInfo = ProgressBarRangeInfo(
        current = currentSelectedIndex.toFloat(),
        range = 0f..(colors.size - 1).coerceAtLeast(0).toFloat(),
        steps = (colors.size - 2).coerceAtLeast(0),
      )
    }
    if (enabled && colors.isNotEmpty()) {
      setProgress { targetValue ->
        val newIndex = targetValue.roundToInt().coerceIn(0, colors.size - 1)
        if (newIndex != currentSelectedIndex) {
          currentOnColorSelected(newIndex)
        }
        true
      }
    }
  }

  Canvas(modifier = modifier
    .then(gestureModifier)
    .then(semanticsModifier)) {
    if (colors.isEmpty()) return@Canvas
    val itemWidth = size.width / colors.size
    val verticalInset = size.height * UnselectedItemVerticalInset
    val strokeWidthPx = SelectorStrokeWidth.toPx()
    colors.forEachIndexed { index, color ->
      val left = itemWidth * index
      if (index == selectedIndex) {
        drawRect(
          color = color,
          topLeft = Offset(left, 0f),
          size = Size(itemWidth, size.height),
        )
        // selectorColor can vanish against a swatch of that exact same color (e.g. a pure
        // black/white swatch with the usual theme-based black/white selectorColor), so fall
        // back to a color contrasting with the swatch's own luminance in that case only -
        // keeps the same single-ring geometry everywhere else instead of risking a second,
        // nested ring that won't fit inside a narrow swatch on a strip with many colors.
        val ringColor = if (color == selectorColor) {
          if (color.luminance() > SelectorContrastLuminanceThreshold) Color.Black else Color.White
        } else {
          selectorColor
        }
        drawRect(
          color = ringColor,
          topLeft = Offset(left + strokeWidthPx / 2f, strokeWidthPx / 2f),
          size = Size(itemWidth - strokeWidthPx, size.height - strokeWidthPx),
          style = Stroke(width = strokeWidthPx),
        )
      } else {
        drawRect(
          color = color,
          topLeft = Offset(left, verticalInset),
          size = Size(itemWidth, size.height - verticalInset * 2f),
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun ColorSliderPreview() {
  val colors = listOf(
    Color(0xFFF44336),
    Color(0xFFE91E63),
    Color(0xFF9C27B0),
    Color(0xFF673AB7),
    Color(0xFF3F51B5),
    Color(0xFF2196F3),
    Color(0xFF4CAF50),
    Color(0xFFFFEB3B),
    Color(0xFFFF9800),
  )
  AppTheme {
    ColorSlider(
      colors = colors,
      selectedIndex = 3,
      onColorSelected = {},
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp),
    )
  }
}
