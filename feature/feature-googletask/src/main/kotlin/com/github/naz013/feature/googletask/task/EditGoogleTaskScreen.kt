package com.github.naz013.feature.googletask.task

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.AppTheme
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.MenuTextButton
import com.github.naz013.ui.common.compose.foundation.component.SettingsSectionHeader
import com.github.naz013.ui.tag.TagChipPicker
import com.github.naz013.ui.tag.TagChipState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditGoogleTaskScreen(
  modifier: Modifier = Modifier,
  state: EditGoogleTaskState,
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit,
  onDeleteMenuClick: () -> Unit,
  onMoveMenuClick: () -> Unit,
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
  onTagToggle: (TagChipState) -> Unit,
  onManageTagsClick: () -> Unit,
  adsContent: @Composable () -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(state.screenTitleRes)) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = null,
            enabled = !state.isLoading,
            onClick = onBackClick,
          )
        },
        actions = {
          if (state.canMove) {
            MenuIconButton(
              icon = painterResource(R.drawable.ic_fluent_arrow_move),
              contentDescription = stringResource(R.string.move_to_another_list),
              enabled = !state.isLoading,
              onClick = onMoveMenuClick,
            )
          }
          if (state.canDelete) {
            MenuIconButton(
              icon = painterResource(R.drawable.ic_fluent_delete),
              contentDescription = stringResource(R.string.delete),
              enabled = !state.isLoading,
              onClick = onDeleteMenuClick,
            )
          }
          MenuTextButton(
            text = stringResource(R.string.save),
            enabled = !state.isLoading,
            onClick = onSaveClick,
          )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
      )
    },
  ) { padding ->
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.background)
          .padding(padding)
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
            .fillMaxWidth(),
      )

      OutlinedTextField(
        value = state.notes,
        onValueChange = onNotesChange,
        label = { Text(stringResource(R.string.details)) },
        enabled = !state.isLoading,
        modifier =
          Modifier
            .fillMaxWidth(),
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

      SettingsSectionHeader(stringResource(R.string.tags))

      TagChipPicker(
        allTags = state.allTags,
        selectedTagIds = state.selectedTagIds,
        onToggle = onTagToggle,
        onManageTagsClick = onManageTagsClick,
        modifier = Modifier.fillMaxWidth(),
      )

      adsContent()
    }
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

@Preview(showBackground = true)
@Composable
private fun EditGoogleTaskScreenPreview() {
  AppTheme {
    EditGoogleTaskScreen(
      state =
        EditGoogleTaskState(
          title = "Buy milk",
          notes = "2 liters, whole",
          dateText = "Tomorrow",
          isDateSelected = true,
          timeText = "10:00",
          isTimeSelected = true,
          listName = "Groceries",
          canMove = true,
          canDelete = true,
        ),
      onBackClick = {},
      onSaveClick = {},
      onDeleteMenuClick = {},
      onMoveMenuClick = {},
      onTitleChange = {},
      onNotesChange = {},
      onDateFieldClick = {},
      onTimeFieldClick = {},
      onListFieldClick = {},
      onDateTypeSelected = {},
      onTimeTypeSelected = {},
      onListPicked = {},
      onDeleteConfirmed = {},
      onDialogDismiss = {},
      onTagToggle = {},
      onManageTagsClick = {},
      adsContent = {},
    )
  }
}
