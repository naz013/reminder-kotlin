package com.github.naz013.ui.routine

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.component.ColorSlider

/**
 * Labeled, card-wrapped [ColorSlider] for picking a routine's solid card color - the same
 * "surfaceContainer card + title label + slider" shell `feature-group`'s `EditGroupScreen` builds
 * inline for `GroupV2.color` (also a solid, no-opacity color), extracted here so
 * `feature-routine`'s editor doesn't have to re-author it. Deliberately takes [colors] as a plain
 * `List<Color>` rather than reading a specific palette itself - `ui-routine` can't depend on
 * `ui-note` (see `docs/architecture.md`'s `ui-*` dependency rule), so the caller supplies whichever
 * palette it wants, e.g. `ThemeProvider.colorsForSliderThemed()` from `ui-common`.
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
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
      )
      ColorSlider(
        colors = colors,
        selectedIndex = selectedIndex,
        onColorSelected = onColorSelected,
        enabled = enabled,
        hapticFeedbackEnabled = hapticFeedbackEnabled,
        modifier = Modifier.fillMaxWidth().height(40.dp).padding(top = 8.dp),
      )
    }
  }
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
