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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.AppTheme

private val SelectorStrokeWidth = 2.dp
private const val UnselectedItemVerticalInset = 0.1f

@Composable
fun ColorSlider(
  colors: List<Color>,
  selectedIndex: Int,
  onColorSelected: (Int) -> Unit,
  modifier: Modifier = Modifier,
  selectorColor: Color = MaterialTheme.colorScheme.onSurface,
  enabled: Boolean = true,
  hapticFeedbackEnabled: Boolean = true,
) {
  val hapticFeedback = LocalHapticFeedback.current

  val currentOnColorSelected by rememberUpdatedState(onColorSelected)
  val currentSelectedIndex by rememberUpdatedState(selectedIndex)
  val gestureModifier =
    if (enabled && colors.isNotEmpty()) {
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

  Canvas(modifier = modifier.then(gestureModifier)) {
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
        drawRect(
          color = selectorColor,
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
  val colors =
    listOf(
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
      modifier = Modifier.fillMaxWidth().height(40.dp),
    )
  }
}
