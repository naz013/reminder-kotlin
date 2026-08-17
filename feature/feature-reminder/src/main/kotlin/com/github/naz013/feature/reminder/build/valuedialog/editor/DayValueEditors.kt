package com.github.naz013.feature.reminder.build.valuedialog.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.datecalc.WeekDaysProtocol
import com.github.naz013.ui.common.compose.foundation.component.SelectableChipGrid
import com.github.naz013.ui.common.compose.foundation.component.WheelPicker

private val GRID_MAX_HEIGHT = 320.dp

/**
 * "Last day" is represented by the sentinel value 0 (see `DayOfMonthController`) since day
 * numbers otherwise run 1..28.
 */
private const val LAST_DAY_VALUE = 0

/** Single-select day-of-month wheel (1..28, plus "Last day"). Replaces `DayOfMonthController`. */
@Composable
internal fun DayOfMonthValueEditor(
  builderItem: BuilderItem<Int>,
  onValueChange: (BuilderItem<*>) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  val lastDayLabel = stringResource(R.string.last_day)
  val days = remember(lastDayLabel) { (1..28).toList() + LAST_DAY_VALUE }
  var selectedIndex by remember(builderItem) {
    mutableIntStateOf(days.indexOf(builderItem.modifier.getValue()).coerceAtLeast(0))
  }

  WheelPicker(
    items = days.map { if (it == LAST_DAY_VALUE) lastDayLabel else it.toString() },
    selectedIndex = selectedIndex,
    onSelectedIndexChange = { index ->
      val day = days.getOrNull(index)
      if (day != null) {
        selectedIndex = index
        builderItem.modifier.update(day)
        onValueChange(builderItem)
      }
    },
    modifier = Modifier.fillMaxWidth(),
    hapticFeedbackEnabled = hapticFeedbackEnabled,
  )
}

/** Single-select day-of-year grid (1..365). Replaces `DayOfYearController`. */
@Composable
internal fun DayOfYearValueEditor(
  builderItem: BuilderItem<Int>,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  var selected by remember(builderItem) { mutableStateOf(builderItem.modifier.getValue()) }
  val days = remember { (1..365).toList() }

  SelectableChipGrid(
    items = days,
    selectedItems = setOfNotNull(selected),
    onItemToggle = { day ->
      selected = day
      builderItem.modifier.update(day)
      onValueChange(builderItem)
    },
    itemLabel = { it.toString() },
    columns = 8,
    modifier = Modifier.fillMaxWidth().heightIn(max = GRID_MAX_HEIGHT),
  )
}

/**
 * Multi-select weekday grid + "Working days"/"All days" quick-select buttons. Replaces
 * `DaysOfWeekController`'s 7-checkbox layout.
 *
 * The stored `List<Int>` (0/1 flags, see `IntervalUtil.getWeekRepeat`/`WeekDaysProtocol`) is
 * ordered Sunday-first ([sun, mon, tue, ... sat]), but the grid displays Monday-first to match
 * the legacy toggle-button row - [DISPLAY_TO_STORAGE_INDEX] converts between the two.
 */
@Composable
internal fun DaysOfWeekValueEditor(
  builderItem: BuilderItem<List<Int>>,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  val labels = listOf(
    stringResource(R.string.mon),
    stringResource(R.string.tue),
    stringResource(R.string.wed),
    stringResource(R.string.thu),
    stringResource(R.string.fri),
    stringResource(R.string.sat),
    stringResource(R.string.sun),
  )
  val days = remember(labels) { labels.mapIndexed { index, label -> WeekdayOption(index, label) } }
  val workDaysIndices = remember { storageToDisplayIndices(WeekDaysProtocol.getWorkDays()) }

  var selectedIndices by remember(builderItem) {
    mutableStateOf(storageToDisplayIndices(builderItem.modifier.getValue()))
  }

  fun commit(indices: Set<Int>) {
    selectedIndices = indices
    builderItem.modifier.update(displayIndicesToStorage(indices))
    onValueChange(builderItem)
  }

  Column(modifier = Modifier.fillMaxWidth()) {
    Row {
      TextButton(onClick = { commit(workDaysIndices) }) {
        Text(stringResource(R.string.builder_working_days))
      }
      TextButton(onClick = { commit((0..6).toSet()) }) {
        Text(stringResource(R.string.builder_all_days))
      }
    }
    SelectableChipGrid(
      items = days,
      selectedItems = days.filterTo(HashSet()) { it.index in selectedIndices },
      onItemToggle = { day ->
        commit(
          if (day.index in selectedIndices) selectedIndices - day.index else selectedIndices + day.index,
        )
      },
      itemLabel = WeekdayOption::label,
      columns = 7,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

private data class WeekdayOption(val index: Int, val label: String)

/** display index (0=Mon..6=Sun) -> storage index (0=Sun..6=Sat). */
private val DISPLAY_TO_STORAGE_INDEX = intArrayOf(1, 2, 3, 4, 5, 6, 0)

private fun storageToDisplayIndices(storage: List<Int>?): Set<Int> {
  if (storage == null || storage.size != 7) return emptySet()
  return (0..6).filterTo(HashSet()) { displayIndex -> storage[DISPLAY_TO_STORAGE_INDEX[displayIndex]] == 1 }
}

private fun displayIndicesToStorage(displayIndices: Set<Int>): List<Int> {
  val storage = MutableList(7) { 0 }
  displayIndices.forEach { displayIndex -> storage[DISPLAY_TO_STORAGE_INDEX[displayIndex]] = 1 }
  return storage
}
