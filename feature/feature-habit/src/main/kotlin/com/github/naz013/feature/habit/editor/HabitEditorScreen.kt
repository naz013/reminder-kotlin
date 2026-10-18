package com.github.naz013.feature.habit.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.domain.habit.HabitFrequency
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.TopAppbarColor
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.MenuTextButton
import com.github.naz013.ui.routine.RoutineColorPicker
import com.github.naz013.ui.routine.RoutineIconPicker

private val MAX_CONTENT_WIDTH = 600.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HabitEditorScreen(
  modifier: Modifier = Modifier,
  state: HabitEditorState,
  onBackClick: () -> Unit,
  onNameChange: (String) -> Unit,
  onDescriptionChange: (String) -> Unit,
  onFrequencyChange: (HabitFrequency) -> Unit,
  onTargetDecrease: () -> Unit,
  onTargetIncrease: () -> Unit,
  onColorSelected: (Int) -> Unit,
  onIconSelected: (Int?) -> Unit,
  onSaveClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onDeleteConfirmed: () -> Unit,
  onDeleteDismissed: () -> Unit,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(if (state.id == null) R.string.new_habit else R.string.edit_habit)) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = stringResource(R.string.cd_back),
            onClick = onBackClick,
          )
        },
        actions = {
          MenuTextButton(
            text = stringResource(R.string.save),
            enabled = state.canSave,
            onClick = onSaveClick,
          )
          if (state.canDelete) {
            MenuIconButton(
              icon = AppIcons.Fluent.Delete,
              contentDescription = stringResource(R.string.delete),
              onClick = onDeleteClick,
            )
          }
        },
        colors = TopAppbarColor,
      )
    },
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Column(modifier = Modifier.widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth()) {
        NameRow(
          state = state,
          onNameChange = onNameChange,
          onIconSelected = onIconSelected,
        )
        OutlinedTextField(
          value = state.description,
          onValueChange = onDescriptionChange,
          label = { Text(stringResource(R.string.habit_description_hint)) },
          modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )
        FrequencyPicker(
          frequency = state.frequency,
          onFrequencyChange = onFrequencyChange,
          modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        )
        TargetStepper(
          state = state,
          onDecrease = onTargetDecrease,
          onIncrease = onTargetIncrease,
          modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        RoutineColorPicker(
          colors = state.sliderColors,
          selectedIndex = state.colorPosition,
          onColorSelected = onColorSelected,
          hapticFeedbackEnabled = state.hapticFeedbackEnabled,
          modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        )
      }
    }
  }

  if (state.isDeleteConfirmationVisible) {
    AlertDialog(
      onDismissRequest = onDeleteDismissed,
      title = { Text(stringResource(R.string.delete_habit_title)) },
      text = { Text(stringResource(R.string.delete_habit_message)) },
      confirmButton = {
        TextButton(onClick = onDeleteConfirmed, modifier = Modifier.testTag("habit_delete_confirm")) {
          Text(stringResource(R.string.delete))
        }
      },
      dismissButton = {
        TextButton(onClick = onDeleteDismissed) {
          Text(stringResource(R.string.cancel))
        }
      },
    )
  }
}

@Composable
private fun NameRow(
  modifier: Modifier = Modifier,
  state: HabitEditorState,
  onNameChange: (String) -> Unit,
  onIconSelected: (Int?) -> Unit,
) {
  Row(modifier = modifier, verticalAlignment = Alignment.Top) {
    OutlinedTextField(
      value = state.name,
      onValueChange = onNameChange,
      label = { Text(stringResource(R.string.name)) },
      isError = state.nameError != null,
      supportingText = state.nameError?.let { error -> { Text(error) } },
      singleLine = true,
      modifier = Modifier.weight(1f).testTag("habit_name_field"),
    )
    RoutineIconPicker(
      selectedIndex = state.iconIndex,
      onIconSelected = onIconSelected,
      modifier = Modifier.padding(start = 12.dp, top = 8.dp),
    )
  }
}

@Composable
private fun FrequencyPicker(
  modifier: Modifier = Modifier,
  frequency: HabitFrequency,
  onFrequencyChange: (HabitFrequency) -> Unit,
) {
  Column(modifier = modifier) {
    Text(text = stringResource(R.string.habit_frequency), style = MaterialTheme.typography.titleSmall)
    Row(
      modifier = Modifier.padding(top = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      FilterChip(
        selected = frequency == HabitFrequency.DAILY,
        onClick = { onFrequencyChange(HabitFrequency.DAILY) },
        label = { Text(stringResource(R.string.habit_frequency_daily)) },
      )
      FilterChip(
        selected = frequency == HabitFrequency.WEEKLY,
        onClick = { onFrequencyChange(HabitFrequency.WEEKLY) },
        label = { Text(stringResource(R.string.habit_frequency_weekly)) },
      )
    }
  }
}

@Composable
private fun TargetStepper(
  modifier: Modifier = Modifier,
  state: HabitEditorState,
  onDecrease: () -> Unit,
  onIncrease: () -> Unit,
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    IconButton(
      onClick = onDecrease,
      enabled = state.canDecreaseTarget,
      modifier = Modifier.testTag("habit_target_decrease"),
    ) {
      Icon(painter = AppIcons.Fluent.Remove, contentDescription = stringResource(R.string.habit_target_decrease))
    }
    Text(text = state.targetLabel, style = MaterialTheme.typography.titleMedium)
    IconButton(
      onClick = onIncrease,
      enabled = state.canIncreaseTarget,
      modifier = Modifier.testTag("habit_target_increase"),
    ) {
      Icon(painter = AppIcons.Fluent.Add, contentDescription = stringResource(R.string.habit_target_increase))
    }
  }
}
