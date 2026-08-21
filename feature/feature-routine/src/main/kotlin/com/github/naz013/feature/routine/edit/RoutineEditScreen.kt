package com.github.naz013.feature.routine.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.TopAppbarColor
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.MenuTextButton
import com.github.naz013.ui.common.datetime.rememberDateTimePicker
import com.github.naz013.ui.routine.RoutineColorPicker
import com.github.naz013.ui.tag.TagChipPicker
import com.github.naz013.ui.tag.TagChipState
import org.threeten.bp.LocalTime

private val DURATION_PRESETS_SECONDS = listOf(0, 300, 600, 900, 1800)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutineEditScreen(
  state: RoutineEditState,
  onBackClick: () -> Unit,
  onTitleChange: (String) -> Unit,
  onDescriptionChange: (String) -> Unit,
  onColorSelected: (Int) -> Unit,
  onPinToggleClick: () -> Unit,
  onRepeatsDailyChange: (Boolean) -> Unit,
  onAddStepClick: () -> Unit,
  onStepTitleChange: (stepId: String, title: String) -> Unit,
  onStepDurationSelected: (stepId: String, durationSeconds: Int) -> Unit,
  onStepTimeSelected: (stepId: String, time: LocalTime?) -> Unit,
  onRemoveStepClick: (stepId: String) -> Unit,
  onMoveStepUp: (stepId: String) -> Unit,
  onMoveStepDown: (stepId: String) -> Unit,
  onTagToggle: (TagChipState) -> Unit,
  onManageTagsClick: () -> Unit,
  onSaveClick: () -> Unit,
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(if (state.id == null) R.string.new_routine else R.string.routines)) },
        navigationIcon = {
          MenuIconButton(
            icon = AppIcons.Builder.ArrowLeft,
            contentDescription = null,
            onClick = onBackClick,
          )
        },
        actions = {
          MenuIconButton(
            icon = if (state.isPinned) AppIcons.Fluent.Pin else AppIcons.Fluent.PinOff,
            contentDescription = stringResource(if (state.isPinned) R.string.unpin else R.string.pin),
            onClick = onPinToggleClick,
          )
          if (state.canDelete) {
            MenuIconButton(
              icon = AppIcons.Fluent.Delete,
              contentDescription = stringResource(R.string.delete),
              onClick = onDeleteClick,
            )
          }
          MenuTextButton(
            text = stringResource(R.string.save),
            enabled = state.canSave,
            onClick = onSaveClick,
          )
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
    ) {
      OutlinedTextField(
        value = state.title,
        onValueChange = onTitleChange,
        label = { Text(stringResource(R.string.title)) },
        isError = !state.canSave,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )
      OutlinedTextField(
        value = state.description,
        onValueChange = onDescriptionChange,
        label = { Text(stringResource(R.string.routine_description_hint)) },
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
      )

      RoutineColorPicker(
        colors = state.sliderColors,
        selectedIndex = state.colorPosition,
        onColorSelected = onColorSelected,
        hapticFeedbackEnabled = state.hapticFeedbackEnabled,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
      )

      SectionHeader(stringResource(R.string.routine_steps))

      state.steps.forEachIndexed { index, step ->
        RoutineStepRow(
          step = step,
          isFirst = index == 0,
          isLast = index == state.steps.lastIndex,
          onTitleChange = { onStepTitleChange(step.id, it) },
          onDurationSelected = { onStepDurationSelected(step.id, it) },
          onTimeSelected = { onStepTimeSelected(step.id, it) },
          onRemoveClick = { onRemoveStepClick(step.id) },
          onMoveUpClick = { onMoveStepUp(step.id) },
          onMoveDownClick = { onMoveStepDown(step.id) },
          modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
      }
      TextButton(onClick = onAddStepClick, modifier = Modifier.padding(top = 4.dp)) {
        Icon(AppIcons.Fluent.Add, contentDescription = null)
        Text(stringResource(R.string.add_step), modifier = Modifier.padding(start = 4.dp))
      }

      SectionHeader(stringResource(R.string.repeat))
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(text = stringResource(R.string.repeat_daily), modifier = Modifier.weight(1f))
        Switch(checked = state.repeatsDaily, onCheckedChange = onRepeatsDailyChange)
      }

      SectionHeader(stringResource(R.string.tags))
      TagChipPicker(
        allTags = state.allTags,
        selectedTagIds = state.selectedTagIds,
        onToggle = onTagToggle,
        onManageTagsClick = onManageTagsClick,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun SectionHeader(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleSmall,
    color = MaterialTheme.colorScheme.tertiary,
    modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp),
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoutineStepRow(
  step: RoutineStepUiState,
  isFirst: Boolean,
  isLast: Boolean,
  onTitleChange: (String) -> Unit,
  onDurationSelected: (Int) -> Unit,
  onTimeSelected: (LocalTime?) -> Unit,
  onRemoveClick: () -> Unit,
  onMoveUpClick: () -> Unit,
  onMoveDownClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val dateTimePicker = rememberDateTimePicker()
  val timeLabel = step.scheduledTime ?: stringResource(R.string.no_time)

  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
          value = step.title,
          onValueChange = onTitleChange,
          label = { Text(stringResource(R.string.step_title_hint)) },
          singleLine = true,
          modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onMoveUpClick, enabled = !isFirst) {
          Icon(
            AppIcons.Builder.ChevronDown,
            contentDescription = stringResource(R.string.move_step_up),
            modifier = Modifier.graphicsLayer { rotationZ = 180f },
          )
        }
        IconButton(onClick = onMoveDownClick, enabled = !isLast) {
          Icon(AppIcons.Builder.ChevronDown, contentDescription = stringResource(R.string.move_step_down))
        }
        IconButton(onClick = onRemoveClick) {
          Icon(AppIcons.Fluent.Dismiss, contentDescription = stringResource(R.string.delete))
        }
      }
      FlowRow(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        DURATION_PRESETS_SECONDS.forEach { seconds ->
          FilterChip(
            selected = step.durationSeconds == seconds,
            onClick = { onDurationSelected(seconds) },
            label = { Text(durationPresetLabel(seconds)) },
          )
        }
        FilterChip(
          selected = step.scheduledTime != null,
          onClick = {
            val initialTime = step.scheduledTime?.let(::parseTime) ?: LocalTime.now()
            dateTimePicker.showTimePicker(
              time = initialTime,
              title = timeLabel,
              onTimeSelected = onTimeSelected,
            )
          },
          label = { Text(timeLabel) },
          leadingIcon = {
            Icon(
              AppIcons.Builder.Time,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
          },
        )
      }
    }
  }
}

@Composable
private fun durationPresetLabel(seconds: Int): String = when (seconds) {
  0 -> stringResource(R.string.duration_none)
  else -> if (seconds % 60 == 0) "${seconds / 60}m" else "${seconds}s"
}

private fun parseTime(value: String): LocalTime = runCatching {
  val (hour, minute) = value.split(":").map(String::toInt)
  LocalTime.of(hour, minute)
}.getOrDefault(LocalTime.now())
