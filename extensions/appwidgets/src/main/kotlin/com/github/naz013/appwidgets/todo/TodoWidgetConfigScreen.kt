package com.github.naz013.appwidgets.todo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.appwidgets.R
import com.github.naz013.appwidgets.compose.WidgetConfigScaffold
import com.github.naz013.ui.common.compose.AppTheme

@Composable
internal fun TodoWidgetConfigScreen(
  modifier: Modifier = Modifier,
  state: TodoWidgetConfigState,
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit,
  onReminderSelected: (String) -> Unit
) {
  WidgetConfigScaffold(
    title = stringResource(R.string.todo),
    onBackClick = onBackClick,
    onSaveClick = onSaveClick,
    modifier = modifier,
  ) {
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
            Column(modifier = Modifier.padding(start = 16.dp)) {
              Text(text = reminder.summary, style = MaterialTheme.typography.bodyLarge)
              Text(
                text = reminder.itemsCount.toString(),
                style = MaterialTheme.typography.bodySmall
              )
            }
          }
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
      onReminderSelected = {}
    )
  }
}
