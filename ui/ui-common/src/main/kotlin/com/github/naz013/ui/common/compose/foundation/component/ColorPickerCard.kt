package com.github.naz013.ui.common.compose.foundation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppTheme

private val SelectedColorDotSize = 16.dp

/**
 * Labeled, card-wrapped [ColorSlider] - the "surfaceContainer card + title label + slider" shell
 * previously duplicated inline by every solid-color-picking edit screen (group, tag, Google Task
 * list, routine). A small dot showing the currently selected color sits next to [title] so the
 * choice reads at a glance without scanning the slider strip for the outlined swatch.
 */
@Composable
fun ColorPickerCard(
  colors: List<Color>,
  selectedIndex: Int,
  onColorSelected: (Int) -> Unit,
  modifier: Modifier = Modifier,
  title: String = stringResource(R.string.color),
  enabled: Boolean = true,
  hapticFeedbackEnabled: Boolean = true,
  sliderTestTag: String? = null,
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.primary,
        )
        colors.getOrNull(selectedIndex)?.let { selectedColor ->
          SelectedColorDot(
            color = selectedColor,
            modifier = Modifier.padding(start = 8.dp),
          )
        }
      }
      ColorSlider(
        colors = colors,
        selectedIndex = selectedIndex,
        onColorSelected = onColorSelected,
        enabled = enabled,
        hapticFeedbackEnabled = hapticFeedbackEnabled,
        modifier =
          Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(top = 8.dp)
            .let { if (sliderTestTag != null) it.testTag(sliderTestTag) else it },
      )
    }
  }
}

@Composable
private fun SelectedColorDot(
  color: Color,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .size(SelectedColorDotSize)
        .clip(CircleShape)
        .background(color)
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
  )
}

@Preview(showBackground = true)
@Composable
private fun ColorPickerCardPreview() {
  val colors =
    listOf(
      Color(0xFF86E3CE),
      Color(0xFFD0E6A5),
      Color(0xFFFFDD94),
      Color(0xFFFA897B),
      Color(0xFFCCABD8),
    )
  AppTheme {
    ColorPickerCard(
      colors = colors,
      selectedIndex = 1,
      onColorSelected = {},
    )
  }
}
