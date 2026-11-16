package com.github.naz013.appwidgets.habit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.compose.WidgetConfigScaffold
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.component.ColorSlider
import com.github.naz013.ui.common.icon.DrawableCatalog

@Composable
internal fun HabitWidgetConfigScreen(
  modifier: Modifier = Modifier,
  state: HabitWidgetConfigState,
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit,
  onBackgroundColorSelected: (Int) -> Unit
) {
  WidgetConfigScaffold(
    title = stringResource(R.string.habits),
    onBackClick = onBackClick,
    onSaveClick = onSaveClick,
    modifier = modifier,
  ) {
    HabitWidgetMockPreview(
      backgroundColor = state.backgroundColor,
      foregroundColor = state.foregroundColor
    )

    Text(
      text = stringResource(R.string.background),
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.padding(top = 16.dp),
    )
    Card(
      modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
      ColorSlider(
        colors = state.palette,
        selectedIndex = state.backgroundIndex,
        onColorSelected = onBackgroundColorSelected,
        contentDescription = stringResource(R.string.background),
        modifier = Modifier.fillMaxWidth().height(48.dp).padding(8.dp),
        hapticFeedbackEnabled = state.hapticFeedbackEnabled,
      )
    }
  }
}

@Composable
private fun HabitWidgetMockPreview(
  modifier: Modifier = Modifier,
  backgroundColor: Color,
  foregroundColor: Color,
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(
        brush = Brush.horizontalGradient(
          listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
        ),
      )
      .padding(16.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          backgroundColor,
          RoundedCornerShape(dimensionResource(R.dimen.home_screen_widget_corner_radius))
        )
        .padding(8.dp),
    ) {
      Text(
        text = stringResource(R.string.habits),
        color = foregroundColor,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp),
      )
      listOf(
        Triple("Drink water", "3/3", DrawableCatalog.Fluent.CheckboxChecked),
        Triple("Read", "0/1", DrawableCatalog.Fluent.CheckboxUnchecked)
      ).forEach { (name, progress, icon) ->
        Row(
          modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = foregroundColor,
            modifier = Modifier.size(24.dp),
          )
          Text(
            text = name,
            color = foregroundColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp).weight(1f),
          )
          Text(
            text = progress,
            color = foregroundColor,
            style = MaterialTheme.typography.bodySmall,
          )
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun HabitWidgetConfigScreenPreview() {
  AppTheme {
    HabitWidgetConfigScreen(
      state = HabitWidgetConfigState(),
      onBackClick = {},
      onSaveClick = {},
      onBackgroundColorSelected = {}
    )
  }
}
