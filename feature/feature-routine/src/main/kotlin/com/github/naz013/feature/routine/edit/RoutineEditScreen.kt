package com.github.naz013.feature.routine.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.TopAppbarColor
import com.github.naz013.ui.common.compose.foundation.MenuIconButton
import com.github.naz013.ui.common.compose.foundation.MenuTextButton
import com.github.naz013.ui.common.compose.foundation.component.AppDropdownMenu
import com.github.naz013.ui.common.compose.foundation.component.PopupMenuItem
import com.github.naz013.ui.common.compose.foundation.component.WheelPicker
import com.github.naz013.ui.common.datetime.rememberDateTimePicker
import com.github.naz013.ui.common.icon.DrawableCatalog
import com.github.naz013.ui.routine.RoutineColorPicker
import com.github.naz013.ui.routine.RoutineIconPicker
import com.github.naz013.ui.tag.TagChipPicker
import com.github.naz013.ui.tag.TagChipState
import org.threeten.bp.LocalTime

private val DURATION_PRESETS_SECONDS = listOf(0, 300, 600, 900, 1800)
private const val MIN_DAY_OF_MONTH = 1
private const val MAX_DAY_OF_MONTH = 28
private const val MAX_TITLE_LENGTH = 100
private val DAY_OF_MONTH_LABELS = (MIN_DAY_OF_MONTH..MAX_DAY_OF_MONTH).map { it.toString() }

/** [step.id][RoutineStepUiState.id] is only ever known once a step has already been added (it's a
 * random UUID minted by the ViewModel, see `RoutineEditViewModel.onAddStepClick`), so - like
 * `shopItemCheckTestTag`/`shopItemRemoveTestTag` in `SubTasksValueEditor.kt` - an instrumented test
 * can't know this tag ahead of time either. It locates a freshly-added row via this prefix (rows
 * stay fully composed in a plain `Column.verticalScroll`, never a lazy layout, so every row's tag
 * is always present in the semantics tree regardless of scroll position) ordered top-to-bottom to
 * match [RoutineEditState.steps]'s order, then scopes every further interaction with that row - its
 * title field, duration/time chips, move/remove buttons - via `hasAnyAncestor(hasTestTag(...))`
 * rather than continuing to rely on position, since duration/time chip labels repeat identically
 * across untouched rows (e.g. every fresh row shows the same "Duration: none"/"No time" text). */
fun routineStepCardTestTag(stepId: String): String = "routine_step_card_$stepId"

/** 0=Sunday..6=Saturday, matching the app-wide weekday convention (see
 * [com.github.naz013.domain.reminder.v2.RecurrenceRule.RelativeMonthly]'s kdoc). */
private val WEEKDAY_LABELS = listOf(
  0 to R.string.sun,
  1 to R.string.mon,
  2 to R.string.tue,
  3 to R.string.wed,
  4 to R.string.thu,
  5 to R.string.fri,
  6 to R.string.sat,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutineEditScreen(
  state: RoutineEditState,
  onBackClick: () -> Unit,
  onTitleChange: (String) -> Unit,
  onDescriptionChange: (String) -> Unit,
  onColorSelected: (Int) -> Unit,
  onIconSelected: (Int?) -> Unit,
  onRecurrenceOptionChange: (RoutineRecurrenceOption) -> Unit,
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
          MenuTextButton(
            text = stringResource(R.string.save),
            enabled = state.canSave,
            onClick = onSaveClick,
          )
          if (state.canDelete) {
            EditOverflowMenu(onDeleteClick = onDeleteClick)
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
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
          value = state.title,
          onValueChange = { if (it.length <= MAX_TITLE_LENGTH) onTitleChange(it) },
          label = { Text(stringResource(R.string.title)) },
          isError = state.title.isBlank(),
          singleLine = true,
          modifier = Modifier.weight(1f),
        )
        RoutineIconPicker(
          selectedIndex = state.iconIndex,
          onIconSelected = onIconSelected,
          modifier = Modifier.padding(start = 12.dp),
        )
      }
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

      if (state.steps.isEmpty()) {
        Text(
          text = stringResource(R.string.routine_steps_required),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
        )
      }

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
      RecurrenceOptionPicker(
        option = state.recurrenceOption,
        onOptionChange = onRecurrenceOptionChange,
        modifier = Modifier.fillMaxWidth(),
      )

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
private fun EditOverflowMenu(onDeleteClick: () -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  Box {
    MenuIconButton(
      icon = AppIcons.Fluent.MoreVertical,
      contentDescription = stringResource(R.string.more_options),
      onClick = { expanded = true },
    )
    AppDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      items = listOf(
        PopupMenuItem(id = 0, title = stringResource(R.string.delete), iconRes = DrawableCatalog.Fluent.Delete),
      ),
      onItemClick = {
        expanded = false
        onDeleteClick()
      },
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecurrenceOptionPicker(
  option: RoutineRecurrenceOption,
  onOptionChange: (RoutineRecurrenceOption) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      FilterChip(
        selected = option is RoutineRecurrenceOption.None,
        onClick = { onOptionChange(RoutineRecurrenceOption.None) },
        label = { Text(stringResource(R.string.repeat_none)) },
      )
      FilterChip(
        selected = option is RoutineRecurrenceOption.Daily,
        onClick = { onOptionChange(RoutineRecurrenceOption.Daily) },
        label = { Text(stringResource(R.string.repeat_daily)) },
      )
      FilterChip(
        selected = option is RoutineRecurrenceOption.Weekly,
        onClick = { onOptionChange(RoutineRecurrenceOption.Weekly()) },
        label = { Text(stringResource(R.string.repeat_weekly)) },
      )
      FilterChip(
        selected = option is RoutineRecurrenceOption.Monthly,
        onClick = { onOptionChange(RoutineRecurrenceOption.Monthly()) },
        label = { Text(stringResource(R.string.repeat_monthly)) },
      )
    }
    when (option) {
      is RoutineRecurrenceOption.Weekly -> {
        WeekdaySelector(
          selectedWeekdays = option.weekdays,
          onToggle = { day ->
            val updated = if (day in option.weekdays) option.weekdays - day else option.weekdays + day
            onOptionChange(option.copy(weekdays = updated))
          },
          modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        if (option.weekdays.isEmpty()) {
          Text(
            text = stringResource(R.string.repeat_weekdays_required),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 4.dp),
          )
        }
      }

      is RoutineRecurrenceOption.Monthly -> {
        DayOfMonthPicker(
          dayOfMonth = option.dayOfMonth,
          onDayChange = { onOptionChange(option.copy(dayOfMonth = it)) },
          modifier = Modifier.padding(top = 8.dp),
        )
      }

      else -> Unit
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeekdaySelector(
  selectedWeekdays: Set<Int>,
  onToggle: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  FlowRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
    WEEKDAY_LABELS.forEach { (day, labelRes) ->
      FilterChip(
        selected = day in selectedWeekdays,
        onClick = { onToggle(day) },
        label = { Text(stringResource(labelRes)) },
      )
    }
  }
}

@Composable
private fun DayOfMonthPicker(
  dayOfMonth: Int,
  onDayChange: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
    Text(text = stringResource(R.string.day_of_month), modifier = Modifier.weight(1f))
    WheelPicker(
      items = DAY_OF_MONTH_LABELS,
      selectedIndex = (dayOfMonth - MIN_DAY_OF_MONTH).coerceIn(DAY_OF_MONTH_LABELS.indices),
      onSelectedIndexChange = { onDayChange(it + MIN_DAY_OF_MONTH) },
      modifier = Modifier.width(96.dp),
    )
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
    modifier = modifier.testTag(routineStepCardTestTag(step.id)),
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
