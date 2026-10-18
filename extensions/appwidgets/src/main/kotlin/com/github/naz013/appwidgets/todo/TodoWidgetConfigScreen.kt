package com.github.naz013.appwidgets.todo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
internal fun TodoWidgetConfigScreen(
  modifier: Modifier = Modifier,
  state: TodoWidgetConfigState,
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit,
  onReminderSelected: (String) -> Unit,
  onBackgroundColorSelected: (Int) -> Unit
) {
  WidgetConfigScaffold(
    title = stringResource(R.string.todo),
    onBackClick = onBackClick,
    onSaveClick = onSaveClick,
    modifier = modifier,
  ) {
    TodoWidgetMockPreview(
      title = state.reminders.firstOrNull { it.id == state.selectedReminderId }?.summary
        ?: stringResource(R.string.todo),
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

    if (state.reminders.isEmpty()) {
      Text(
        text = stringResource(R.string.widget_todo_no_lists),
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(top = 16.dp)
      )
    } else {
      Column(modifier = Modifier.fillMaxWidth().selectableGroup()) {
        state.reminders.forEach { reminder ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .selectable(
                selected = reminder.id == state.selectedReminderId,
                onClick = { onReminderSelected(reminder.id) }
              )
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = reminder.id == state.selectedReminderId, onClick = null)
            Text(
              text = reminder.summary,
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.padding(start = 16.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TodoWidgetMockPreview(
  modifier: Modifier = Modifier,
  title: String,
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
        text = title,
        color = foregroundColor,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp),
      )
      listOf(
        "Milk" to DrawableCatalog.Fluent.CheckboxChecked,
        "Bread" to DrawableCatalog.Fluent.CheckboxUnchecked
      ).forEach { (text, icon) ->
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
            text = text,
            color = foregroundColor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp),
          )
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun TodoWidgetConfigScreenPreview() {
  AppTheme {
    TodoWidgetConfigScreen(
      state = TodoWidgetConfigState(
        reminders = listOf(TodoReminderItem("1", "Groceries", 3)),
        selectedReminderId = "1"
      ),
      onBackClick = {},
      onSaveClick = {},
      onReminderSelected = {},
      onBackgroundColorSelected = {}
    )
  }
}
