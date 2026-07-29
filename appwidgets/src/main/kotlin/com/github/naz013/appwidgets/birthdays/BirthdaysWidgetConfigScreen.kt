package com.github.naz013.appwidgets.birthdays

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
internal fun BirthdaysWidgetConfigScreen(
  state: BirthdaysWidgetConfigState,
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit,
  onHeaderColorSelected: (Int) -> Unit,
  onItemColorSelected: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val hapticFeedback = LocalHapticFeedback.current

  WidgetConfigScaffold(
    title = stringResource(R.string.birthdays),
    onBackClick = onBackClick,
    onSaveClick = onSaveClick,
    modifier = modifier,
  ) {
    BirthdaysWidgetMockPreview(
      headerColor = state.headerColor,
      headerContentColor = state.headerContentColor,
      itemColor = state.itemColor,
      itemContentColor = state.itemContentColor,
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
      text = stringResource(R.string.list_item_background),
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
        selectedIndex = state.itemBackgroundIndex,
        onColorSelected = { index ->
          if (state.hapticFeedbackEnabled && index != state.itemBackgroundIndex) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          }
          onItemColorSelected(index)
        },
        modifier = Modifier.fillMaxWidth().height(40.dp).padding(8.dp),
      )
    }
  }
}

@Composable
private fun BirthdaysWidgetMockPreview(
  headerColor: Color,
  headerContentColor: Color,
  itemColor: Color,
  itemContentColor: Color,
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
        Text(
          text = stringResource(R.string.birthdays),
          color = headerContentColor,
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.weight(1f).padding(start = 16.dp),
        )
        Icon(
          painter = painterResource(R.drawable.ic_fluent_settings),
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
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(itemColor, cornerShape)
          .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          painter = painterResource(R.drawable.ic_fluent_food_cake),
          contentDescription = null,
          tint = itemContentColor,
          modifier = Modifier.size(40.dp),
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
          Text(text = "User Name", color = itemContentColor)
          Text(text = "25 years", color = itemContentColor, modifier = Modifier.padding(top = 4.dp))
          Text(text = "5 days", color = itemContentColor, modifier = Modifier.padding(top = 4.dp))
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun BirthdaysWidgetConfigScreenPreview() {
  AppTheme {
    BirthdaysWidgetConfigScreen(
      state = BirthdaysWidgetConfigState(headerBackgroundIndex = 9, itemBackgroundIndex = 4),
      onBackClick = {},
      onSaveClick = {},
      onHeaderColorSelected = {},
      onItemColorSelected = {},
    )
  }
}
