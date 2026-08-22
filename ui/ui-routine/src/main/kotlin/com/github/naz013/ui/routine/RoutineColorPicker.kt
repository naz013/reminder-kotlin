package com.github.naz013.ui.routine

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.component.ColorPickerCard

/** [com.github.naz013.ui.common.compose.foundation.component.ColorSlider] renders on a bare
 * `Canvas` with a raw `pointerInput` gesture (no `clickable` modifier, so it has no semantics
 * `OnClick` action at all) and no text/content-description of its own - an instrumented test can't
 * `performClick()` it, but can locate it by this tag and drive a `performTouchInput { click(...) }`
 * at a position computed from the node's own reported width. */
const val routineColorSliderTestTag = "routine_color_slider"

/**
 * Routine-flavored entry point into `ui-common`'s shared [ColorPickerCard] for picking a routine's
 * solid card color. Deliberately takes [colors] as a plain `List<Color>` rather than reading a
 * specific palette itself - `ui-routine` can't depend on `ui-note` (see `docs/architecture.md`'s
 * `ui-*` dependency rule), so the caller supplies whichever palette it wants, e.g.
 * `ThemeProvider.colorsForSliderThemed()` from `ui-common`.
 */
@Composable
fun RoutineColorPicker(
  colors: List<Color>,
  selectedIndex: Int,
  onColorSelected: (Int) -> Unit,
  modifier: Modifier = Modifier,
  title: String = stringResource(R.string.color),
  enabled: Boolean = true,
  hapticFeedbackEnabled: Boolean = true,
) {
  ColorPickerCard(
    colors = colors,
    selectedIndex = selectedIndex,
    onColorSelected = onColorSelected,
    modifier = modifier,
    title = title,
    enabled = enabled,
    hapticFeedbackEnabled = hapticFeedbackEnabled,
    sliderTestTag = routineColorSliderTestTag,
  )
}

@Preview(showBackground = true)
@Composable
private fun RoutineColorPickerPreview() {
  val colors = listOf(
    Color(0xFF86E3CE),
    Color(0xFFD0E6A5),
    Color(0xFFFFDD94),
    Color(0xFFFA897B),
    Color(0xFFCCABD8),
  )
  AppTheme {
    RoutineColorPicker(
      colors = colors,
      selectedIndex = 1,
      onColorSelected = {},
    )
  }
}
