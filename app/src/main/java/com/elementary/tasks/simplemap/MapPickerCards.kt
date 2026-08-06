package com.elementary.tasks.simplemap

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.github.naz013.ui.common.compose.foundation.component.ColorSlider
import com.google.android.gms.maps.GoogleMap

private val PickerCornerRadius = 5.dp

@Composable
internal fun PickerCard(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(PickerCornerRadius),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    tonalElevation = 1.dp,
    shadowElevation = 1.dp,
  ) {
    Column(content = content)
  }
}

@Composable
internal fun LayerTypeCard(
  modifier: Modifier = Modifier,
  onTypeSelected: (Int) -> Unit,
) {
  PickerCard(modifier = modifier.width(112.dp)) {
    LayerTypeRow(stringResource(R.string.normal)) { onTypeSelected(GoogleMap.MAP_TYPE_NORMAL) }
    LayerTypeRow(stringResource(R.string.satellite)) { onTypeSelected(GoogleMap.MAP_TYPE_SATELLITE) }
    LayerTypeRow(stringResource(R.string.terrain)) { onTypeSelected(GoogleMap.MAP_TYPE_TERRAIN) }
    LayerTypeRow(stringResource(R.string.hybrid)) { onTypeSelected(GoogleMap.MAP_TYPE_HYBRID) }
  }
}

@Composable
private fun LayerTypeRow(
  text: String,
  onClick: () -> Unit,
) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    maxLines = 1,
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(8.dp),
  )
}

private data class MapStyleOption(
  val style: Int,
  @DrawableRes val preview: Int,
  val descriptionRes: Int,
)

private val MapStyleOptions = listOf(
  MapStyleOption(0, R.drawable.preview_map_day, R.string.day),
  MapStyleOption(1, R.drawable.preview_map_retro, R.string.retro),
  MapStyleOption(2, R.drawable.preview_map_silver, R.string.silver),
  MapStyleOption(3, R.drawable.preview_map_night, R.string.night),
  MapStyleOption(4, R.drawable.preview_map_dark, R.string.dark),
  MapStyleOption(5, R.drawable.preview_map_aubergine, R.string.aubergine),
)

@Composable
internal fun MapStyleCard(
  modifier: Modifier = Modifier,
  onStyleSelected: (Int) -> Unit,
) {
  PickerCard(modifier = modifier) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        MapStyleOptions.take(3).forEach { MapStyleThumbnail(it, onStyleSelected) }
      }
      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(top = 16.dp),
      ) {
        MapStyleOptions.drop(3).forEach { MapStyleThumbnail(it, onStyleSelected) }
      }
    }
  }
}

@Composable
private fun MapStyleThumbnail(
  option: MapStyleOption,
  onStyleSelected: (Int) -> Unit,
) {
  Image(
    painter = painterResource(option.preview),
    contentDescription = stringResource(option.descriptionRes),
    modifier = Modifier
      .size(56.dp)
      .clip(RoundedCornerShape(PickerCornerRadius))
      .clickable { onStyleSelected(option.style) },
  )
}

@Composable
internal fun MarkerStyleCard(
  modifier: Modifier = Modifier,
  colors: List<Color>,
  selectedIndex: Int,
  selectorColor: Color = MaterialTheme.colorScheme.onSurface,
  onStyleSelected: (Int) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  PickerCard(modifier = modifier) {
    // padding() must come before height() - applied the other way around, the 16dp padding on
    // each side eats into the fixed 40dp height, leaving only 8dp for the slider itself.
    ColorSlider(
      colors = colors,
      selectedIndex = selectedIndex,
      onColorSelected = onStyleSelected,
      selectorColor = selectorColor,
      modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()
        .height(40.dp),
      hapticFeedbackEnabled = hapticFeedbackEnabled,
    )
  }
}

@Composable
internal fun MarkerRadiusCard(
  radius: Int,
  valueTo: Float,
  formattedRadius: String,
  onValueChange: (Float) -> Unit,
  onValueChangeFinished: () -> Unit,
  modifier: Modifier = Modifier,
) {
  PickerCard(modifier = modifier) {
    Column(modifier = Modifier.padding(8.dp)) {
      Text(
        text = formattedRadius,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.align(Alignment.CenterHorizontally),
      )
      Slider(
        value = radius.toFloat().coerceIn(0f, valueTo),
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = 0f..valueTo,
      )
    }
  }
}
