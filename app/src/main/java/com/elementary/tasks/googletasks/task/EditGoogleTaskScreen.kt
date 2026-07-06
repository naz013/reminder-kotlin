package com.elementary.tasks.googletasks.task

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R

/**
 * Body content only - the title/back-arrow/menu chrome is the native Toolbar owned by
 * [com.elementary.tasks.navigation.toolbarfragment.BaseComposeToolbarFragment].
 */
@Composable
fun EditGoogleTaskScreen(
  state: EditGoogleTaskState,
  onTitleChange: (String) -> Unit,
  onNotesChange: (String) -> Unit,
  onDateFieldClick: () -> Unit,
  onTimeFieldClick: () -> Unit,
  onListFieldClick: () -> Unit,
  onDateTypeSelected: (Boolean) -> Unit,
  onTimeTypeSelected: (Boolean) -> Unit,
  onListPicked: (String) -> Unit,
  onDeleteConfirmed: () -> Unit,
  onDialogDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
  ) {
    OutlinedTextField(
      value = state.title,
      onValueChange = onTitleChange,
      label = { Text(stringResource(R.string.task)) },
      isError = state.titleError,
      supportingText = {
        if (state.titleError) Text(stringResource(R.string.must_be_not_empty))
      },
      enabled = !state.isLoading,
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
    )

    OutlinedTextField(
      value = state.notes,
      onValueChange = onNotesChange,
      label = { Text(stringResource(R.string.details)) },
      enabled = !state.isLoading,
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(top = 16.dp),
    )

    FieldCard(
      label = stringResource(R.string.select_date),
      value = state.dateText ?: stringResource(R.string.no_date),
      enabled = !state.isLoading,
      onClick = onDateFieldClick,
    )

    if (state.isDateSelected) {
      FieldCard(
        label = stringResource(R.string.select_time),
        value = state.timeText ?: stringResource(R.string.no_time),
        enabled = !state.isLoading,
        onClick = onTimeFieldClick,
      )
    }

    FieldCard(
      label = stringResource(R.string.choose_list),
      value = state.listName,
      enabled = !state.isLoading,
      onClick = onListFieldClick,
    )
  }

  when (val dialog = state.dialog) {
    EditGoogleTaskDialog.DateTypeChooser -> {
      TwoOptionDialog(
        firstOptionText = stringResource(R.string.no_date),
        secondOptionText = stringResource(R.string.select_date),
        onFirstOptionClick = { onDateTypeSelected(false) },
        onSecondOptionClick = { onDateTypeSelected(true) },
        onDismiss = onDialogDismiss,
      )
    }

    EditGoogleTaskDialog.TimeTypeChooser -> {
      TwoOptionDialog(
        firstOptionText = stringResource(R.string.no_time),
        secondOptionText = stringResource(R.string.select_time),
        onFirstOptionClick = { onTimeTypeSelected(false) },
        onSecondOptionClick = { onTimeTypeSelected(true) },
        onDismiss = onDialogDismiss,
      )
    }

    is EditGoogleTaskDialog.ListPicker -> {
      ListPickerDialog(
        dialog = dialog,
        onOptionSelected = onListPicked,
        onDismiss = onDialogDismiss,
      )
    }

    EditGoogleTaskDialog.DeleteConfirm -> {
      AlertDialog(
        onDismissRequest = onDialogDismiss,
        text = { Text(stringResource(R.string.delete_this_task)) },
        confirmButton = {
          TextButton(onClick = onDeleteConfirmed) { Text(stringResource(R.string.yes)) }
        },
        dismissButton = {
          TextButton(onClick = onDialogDismiss) { Text(stringResource(R.string.no)) }
        },
      )
    }

    null -> Unit
  }
}

@Composable
private fun FieldCard(
  label: String,
  value: String,
  enabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(top = 16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .clickable(enabled = enabled, onClick = onClick)
          .padding(12.dp),
    ) {
      Text(text = label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
      Text(
        text = value,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(top = 8.dp),
      )
    }
  }
}

@Composable
private fun TwoOptionDialog(
  firstOptionText: String,
  secondOptionText: String,
  onFirstOptionClick: () -> Unit,
  onSecondOptionClick: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    text = {
      Column {
        Text(
          text = firstOptionText,
          modifier =
            Modifier
              .fillMaxWidth()
              .clickable(onClick = onFirstOptionClick)
              .padding(vertical = 12.dp),
        )
        Text(
          text = secondOptionText,
          modifier =
            Modifier
              .fillMaxWidth()
              .clickable(onClick = onSecondOptionClick)
              .padding(vertical = 12.dp),
        )
      }
    },
    confirmButton = {},
  )
}

@Composable
private fun ListPickerDialog(
  dialog: EditGoogleTaskDialog.ListPicker,
  onOptionSelected: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.choose_list)) },
    text = {
      Column(modifier = Modifier.selectableGroup()) {
        dialog.options.forEach { option ->
          val selected = option.id == dialog.selectedId
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
              Modifier
                .fillMaxWidth()
                .selectable(
                  selected = selected,
                  onClick = { onOptionSelected(option.id) },
                  role = Role.RadioButton,
                ).padding(vertical = 8.dp),
          ) {
            RadioButton(selected = selected, onClick = null)
            Text(text = option.title, modifier = Modifier.padding(start = 8.dp))
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
    },
  )
}
