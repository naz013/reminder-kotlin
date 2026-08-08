package com.elementary.tasks.reminder.build.valuedialog.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.bi.TimerExclusion
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.ui.common.compose.foundation.component.SelectableChipGrid
import org.threeten.bp.LocalTime

private val GRID_MAX_HEIGHT = 200.dp
private val DEFAULT_INTERVAL_HOURS = 3L

private enum class ExclusionMode { HOURS, INTERVAL }

/**
 * "Don't remind me" quiet-hours picker: either a multi-select 0..23 hour grid, or a from/to time
 * range. Replaces `CountdownExclusionController`.
 */
@Composable
fun CountdownExclusionValueEditor(
  builderItem: BuilderItem<TimerExclusion>,
  dateTimeManager: DateTimeManager,
  is24HourFormat: Boolean,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  val initial = builderItem.modifier.getValue()
  var mode by remember(builderItem) {
    mutableStateOf(
      if (initial != null && initial.hours.isEmpty()) ExclusionMode.INTERVAL else ExclusionMode.HOURS,
    )
  }
  var selectedHours by remember(builderItem) { mutableStateOf(initial?.hours?.toSet() ?: emptySet()) }
  var fromTime by remember(builderItem) {
    mutableStateOf(dateTimeManager.toLocalTime(initial?.from) ?: LocalTime.now())
  }
  var toTime by remember(builderItem) {
    mutableStateOf(dateTimeManager.toLocalTime(initial?.to) ?: fromTime.plusHours(DEFAULT_INTERVAL_HOURS))
  }
  var editingField by remember { mutableStateOf<TimeField?>(null) }

  fun commit() {
    val exclusion = when (mode) {
      ExclusionMode.HOURS -> TimerExclusion(hours = selectedHours.toList(), from = "", to = "")
      ExclusionMode.INTERVAL -> TimerExclusion(
        hours = emptyList(),
        from = dateTimeManager.to24HourString(fromTime),
        to = dateTimeManager.to24HourString(toTime),
      )
    }
    builderItem.modifier.update(exclusion)
    onValueChange(builderItem)
  }

  Column(modifier = Modifier.fillMaxWidth()) {
    ModeRow(
      selected = mode == ExclusionMode.INTERVAL,
      label = stringResource(R.string.interval),
      onClick = { mode = ExclusionMode.INTERVAL; commit() },
    )
    if (mode == ExclusionMode.INTERVAL) {
      Row(modifier = Modifier.fillMaxWidth().padding(start = 32.dp, top = 4.dp)) {
        TextButton(onClick = { editingField = TimeField.FROM }) {
          Text("${stringResource(R.string.from)} ${dateTimeManager.getTime(fromTime)}")
        }
        TextButton(onClick = { editingField = TimeField.TO }) {
          Text("${stringResource(R.string.to)} ${dateTimeManager.getTime(toTime)}")
        }
      }
    }
    ModeRow(
      selected = mode == ExclusionMode.HOURS,
      label = stringResource(R.string.hours),
      onClick = { mode = ExclusionMode.HOURS; commit() },
      modifier = Modifier.padding(top = 8.dp),
    )
    if (mode == ExclusionMode.HOURS) {
      SelectableChipGrid(
        items = (0..23).toList(),
        selectedItems = selectedHours,
        onItemToggle = { hour ->
          selectedHours = if (hour in selectedHours) selectedHours - hour else selectedHours + hour
          commit()
        },
        itemLabel = { it.toString() },
        columns = 8,
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = GRID_MAX_HEIGHT)
          .padding(top = 4.dp),
      )
    }
  }

  val field = editingField
  if (field != null) {
    TimePickerDialog(
      initialTime = if (field == TimeField.FROM) fromTime else toTime,
      is24HourFormat = is24HourFormat,
      onDismissRequest = { editingField = null },
      onConfirm = { time ->
        if (field == TimeField.FROM) fromTime = time else toTime = time
        editingField = null
        commit()
      },
    )
  }
}

private enum class TimeField { FROM, TO }

@Composable
private fun ModeRow(
  selected: Boolean,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .selectable(selected = selected, onClick = onClick),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    RadioButton(selected = selected, onClick = onClick)
    Text(
      text = label,
      style = MaterialTheme.typography.titleSmall,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
  initialTime: LocalTime,
  is24HourFormat: Boolean,
  onDismissRequest: () -> Unit,
  onConfirm: (LocalTime) -> Unit,
) {
  val state = rememberTimePickerState(
    initialHour = initialTime.hour,
    initialMinute = initialTime.minute,
    is24Hour = is24HourFormat,
  )
  AlertDialog(
    onDismissRequest = onDismissRequest,
    confirmButton = {
      TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
        Text(stringResource(R.string.ok))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismissRequest) {
        Text(stringResource(R.string.cancel))
      }
    },
    text = { TimePicker(state = state) },
  )
}
