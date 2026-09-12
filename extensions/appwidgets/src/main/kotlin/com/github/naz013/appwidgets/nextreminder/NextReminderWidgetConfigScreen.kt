package com.github.naz013.appwidgets.nextreminder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
internal fun NextReminderWidgetConfigScreen(
  modifier: Modifier = Modifier,
  state: NextReminderWidgetConfigState,
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit,
  onBackgroundColorSelected: (Int) -> Unit,
) {
  WidgetConfigScaffold(
    title = stringResource(R.string.next_reminder),
    onBackClick = onBackClick,
    onSaveClick = onSaveClick,
    modifier = modifier,
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(152.dp)
        .background(
          brush = Brush.horizontalGradient(
            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
          ),
        ),
      contentAlignment = Alignment.Center,
    ) {
      Row(
        modifier = Modifier
          .width(220.dp)
          .height(56.dp)
          .background(
            state.backgroundColor,
            RoundedCornerShape(dimensionResource(R.dimen.home_screen_widget_corner_radius))
          )
          .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          painter = painterResource(R.drawable.ic_fluent_clock_alarm),
          contentDescription = null,
          tint = state.contentColor,
          modifier = Modifier.size(28.dp),
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
          Text(
            text = stringResource(R.string.next_reminder),
            color = state.contentColor,
            style = MaterialTheme.typography.bodyMedium,
          )
        }
      }
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
          onBackgroundColorSelected(index)
        },
        contentDescription = stringResource(R.string.background),
        modifier = Modifier.fillMaxWidth().height(48.dp).padding(8.dp),
        hapticFeedbackEnabled = state.hapticFeedbackEnabled,
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun NextReminderWidgetConfigScreenPreview() {
  AppTheme {
    NextReminderWidgetConfigScreen(
      state = NextReminderWidgetConfigState(backgroundIndex = 4),
      onBackClick = {},
      onSaveClick = {},
      onBackgroundColorSelected = {},
    )
  }
}
