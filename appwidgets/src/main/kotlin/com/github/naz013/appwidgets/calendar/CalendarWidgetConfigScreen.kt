package com.github.naz013.appwidgets.calendar

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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.compose.WidgetConfigScaffold
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.component.ColorSlider

@Composable
internal fun CalendarWidgetConfigScreen(
  state: CalendarWidgetConfigState,
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit,
  onHeaderColorSelected: (Int) -> Unit,
  onBackgroundColorSelected: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val hapticFeedback = LocalHapticFeedback.current

  WidgetConfigScaffold(
    title = stringResource(R.string.calendar),
    onBackClick = onBackClick,
    onSaveClick = onSaveClick,
    modifier = modifier,
  ) {
    CalendarWidgetMockPreview(
      headerColor = state.headerColor,
      headerContentColor = state.headerContentColor,
      backgroundColor = state.backgroundColor,
    )

    Text(
      text = stringResource(R.string.header_background),
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
        selectedIndex = state.headerBackgroundIndex,
        onColorSelected = { index ->
          if (state.hapticFeedbackEnabled && index != state.headerBackgroundIndex) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          }
          onHeaderColorSelected(index)
        },
        modifier = Modifier.fillMaxWidth().height(40.dp).padding(8.dp),
      )
    }

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
        onColorSelected = { index ->
          if (state.hapticFeedbackEnabled && index != state.backgroundIndex) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          }
          onBackgroundColorSelected(index)
        },
        modifier = Modifier.fillMaxWidth().height(40.dp).padding(8.dp),
      )
    }
  }
}

@Composable
private fun CalendarWidgetMockPreview(
  headerColor: Color,
  headerContentColor: Color,
  backgroundColor: Color,
  modifier: Modifier = Modifier,
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
    val cornerShape = RoundedCornerShape(dimensionResource(R.dimen.home_screen_widget_corner_radius))
    Column {
      Row(
        modifier = Modifier.fillMaxWidth().height(50.dp).background(headerColor, cornerShape),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          painter = painterResource(R.drawable.ic_fluent_chevron_left),
          contentDescription = null,
          tint = headerContentColor,
          modifier = Modifier.size(50.dp).padding(12.dp),
        )
        Text(
          text = "January",
          color = headerContentColor,
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.weight(1f).padding(start = 16.dp),
        )
        Icon(
          painter = painterResource(R.drawable.ic_fluent_chevron_right),
          contentDescription = null,
          tint = headerContentColor,
          modifier = Modifier.size(50.dp).padding(12.dp),
        )
        Icon(
          painter = painterResource(R.drawable.ic_fluent_settings),
          contentDescription = null,
          tint = headerContentColor,
          modifier = Modifier.size(50.dp).padding(12.dp),
        )
        Icon(
          painter = painterResource(R.drawable.ic_builder_mic_on),
          contentDescription = null,
          tint = headerContentColor,
          modifier = Modifier.size(50.dp).padding(12.dp),
        )
        Icon(
          painter = painterResource(R.drawable.ic_fluent_add),
          contentDescription = null,
          tint = headerContentColor,
          modifier = Modifier.size(50.dp).padding(12.dp),
        )
      }
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(90.dp)
          .background(backgroundColor, cornerShape),
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun CalendarWidgetConfigScreenPreview() {
  AppTheme {
    CalendarWidgetConfigScreen(
      state = CalendarWidgetConfigState(headerBackgroundIndex = 9, backgroundIndex = 4),
      onBackClick = {},
      onSaveClick = {},
      onHeaderColorSelected = {},
      onBackgroundColorSelected = {},
    )
  }
}
