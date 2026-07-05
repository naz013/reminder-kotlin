package com.elementary.tasks.settings.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.compose.foundation.component.SettingsItem

@Composable
fun TestsSettingsScreen(
  state: TestsSettingsState,
  onBirthdayDialogClick: () -> Unit,
  onReminderDialogClick: () -> Unit,
  onObjectExportClick: () -> Unit,
  onReviewDialogClick: () -> Unit,
  onDeveloperOptionsClick: () -> Unit,
  onDialogOptionSelected: (Int) -> Unit,
  onDialogConfirm: () -> Unit,
  onDialogDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState()),
  ) {
    SettingsItem(title = "Open Birthday Screen", dividerBottom = true, onClick = onBirthdayDialogClick)
    SettingsItem(title = "Open Reminder Screen", dividerBottom = true, onClick = onReminderDialogClick)
    SettingsItem(title = "Save object to File", dividerBottom = true, onClick = onObjectExportClick)
    SettingsItem(title = "Show Review Dialog", dividerBottom = true, onClick = onReviewDialogClick)
    SettingsItem(title = "Developer", dividerBottom = true, onClick = onDeveloperOptionsClick)
  }

  val dialog = state.dialog
  if (dialog != null) {
    AlertDialog(
      onDismissRequest = onDialogDismiss,
      title = { Text("Select action to test") },
      text = {
        Column(modifier = Modifier.selectableGroup()) {
          dialog.options.forEachIndexed { index, option ->
            val selected = index == dialog.selectedIndex
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .selectable(selected = selected, onClick = { onDialogOptionSelected(index) }, role = Role.RadioButton)
                .padding(vertical = 8.dp),
            ) {
              RadioButton(selected = selected, onClick = null)
              Text(
                text = option,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp),
              )
            }
          }
        }
      },
      confirmButton = { TextButton(onClick = onDialogConfirm) { Text("Run") } },
      dismissButton = { TextButton(onClick = onDialogDismiss) { Text("Cancel") } },
    )
  }
}
