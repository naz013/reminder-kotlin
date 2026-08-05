package com.github.naz013.appwidgets.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import kotlin.math.roundToInt

@Composable
internal fun EventsWidgetConfigScreen(
  state: EventsWidgetConfigState,
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit,
  onBackgroundColorSelected: (Int) -> Unit,
  onTextSizeChanged: (Int) -> Unit,
  onTextSizeDialogConfirm: () -> Unit,
  onTextSizeDialogDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val hapticFeedback = LocalHapticFeedback.current

  WidgetConfigScaffold(
    title = stringResource(R.string.events),
    onBackClick = onBackClick,
    onSaveClick = onSaveClick,
    modifier = modifier,
  ) {
    EventsWidgetMockPreview(
      backgroundColor = state.backgroundColor,
      foregroundColor = state.foregroundColor,
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
        onColorSelected = { index ->
          onBackgroundColorSelected(index)
        },
        modifier = Modifier.fillMaxWidth().height(40.dp).padding(8.dp),
        hapticFeedbackEnabled = state.hapticFeedbackEnabled,
      )
    }
  }

  if (state.isTextSizeDialogVisible) {
    AlertDialog(
      onDismissRequest = onTextSizeDialogDismiss,
      title = { Text(stringResource(R.string.text_size)) },
      text = {
        Column {
          Text(text = state.textSize.toString(), style = MaterialTheme.typography.titleLarge)
          Slider(
            value = state.textSize.toFloat(),
            onValueChange = {
              val newSize = it.roundToInt()
              if (state.hapticFeedbackEnabled && newSize != state.textSize) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              }
              onTextSizeChanged(newSize)
            },
            valueRange = 12f..25f,
            steps = 12,
          )
        }
      },
      confirmButton = {
        TextButton(onClick = onTextSizeDialogConfirm) { Text(stringResource(R.string.ok)) }
      },
      dismissButton = {
        TextButton(onClick = onTextSizeDialogDismiss) { Text(stringResource(R.string.cancel)) }
      },
    )
  }
}

@Composable
private fun EventsWidgetMockPreview(
  backgroundColor: Color,
  foregroundColor: Color,
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
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(backgroundColor, cornerShape)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = stringResource(R.string.events),
          color = foregroundColor,
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.weight(1f).padding(start = 16.dp),
        )
        Icon(
          painter = painterResource(R.drawable.ic_fluent_settings),
          contentDescription = null,
          tint = foregroundColor,
          modifier = Modifier.size(50.dp).padding(12.dp),
        )
        Icon(
          painter = painterResource(R.drawable.ic_fluent_add),
          contentDescription = null,
          tint = foregroundColor,
          modifier = Modifier.size(50.dp).padding(12.dp),
        )
      }
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          painter = painterResource(R.drawable.ic_fluent_clock_alarm),
          contentDescription = null,
          tint = foregroundColor,
          modifier = Modifier.size(40.dp),
        )
        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
          Row(modifier = Modifier.fillMaxWidth()) {
            Text(
              text = "Task",
              color = foregroundColor,
              modifier = Modifier.weight(1f),
            )
            Text(text = "July 25, 2019", color = foregroundColor)
          }
          Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text(
              text = "+1234567890",
              color = foregroundColor,
              modifier = Modifier.weight(1f),
            )
            Text(text = "10:00 AM", color = foregroundColor)
          }
          Text(
            text = "25 minutes",
            color = foregroundColor,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
          )
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun EventsWidgetConfigScreenPreview() {
  AppTheme {
    EventsWidgetConfigScreen(
      state = EventsWidgetConfigState(backgroundIndex = 9),
      onBackClick = {},
      onSaveClick = {},
      onBackgroundColorSelected = {},
      onTextSizeChanged = {},
      onTextSizeDialogConfirm = {},
      onTextSizeDialogDismiss = {},
    )
  }
}
