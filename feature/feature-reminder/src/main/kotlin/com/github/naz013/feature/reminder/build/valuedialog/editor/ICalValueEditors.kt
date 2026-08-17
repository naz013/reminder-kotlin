package com.github.naz013.feature.reminder.build.valuedialog.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.ICalIntBuilderItem
import com.github.naz013.feature.reminder.build.ICalListIntBuilderItem
import com.github.naz013.feature.reminder.build.adapter.ParamToTextAdapter
import com.github.naz013.icalendar.Day
import com.github.naz013.icalendar.DayValue
import com.github.naz013.icalendar.FreqType
import com.github.naz013.ui.common.compose.foundation.component.SelectableChipGrid
import com.github.naz013.ui.common.compose.foundation.component.WheelPicker

private val GRID_MAX_HEIGHT = 320.dp

/** RRULE frequency wheel (Daily/Weekly/Monthly/Yearly/Hourly/Minutely). Replaces
 *  `ICalFreqController`. */
@Composable
internal fun ICalFreqValueEditor(
  builderItem: BuilderItem<FreqType>,
  paramToTextAdapter: ParamToTextAdapter,
  onValueChange: (BuilderItem<*>) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  val entries = remember { FreqType.entries }
  val items = remember(entries) { entries.map { paramToTextAdapter.getFreqText(it) } }
  var selectedIndex by remember(builderItem) {
    mutableIntStateOf(builderItem.modifier.getValue()?.let { entries.indexOf(it) } ?: 0)
  }
  WheelPicker(
    items = items,
    selectedIndex = selectedIndex,
    onSelectedIndexChange = { index ->
      selectedIndex = index
      builderItem.modifier.update(entries[index])
      onValueChange(builderItem)
    },
    modifier = Modifier.fillMaxWidth(),
    hapticFeedbackEnabled = hapticFeedbackEnabled,
  )
}

/** RRULE week-start-day wheel (Mon..Sun). Replaces `ICalWeekStartController`. */
@Composable
internal fun ICalWeekStartValueEditor(
  builderItem: BuilderItem<DayValue>,
  paramToTextAdapter: ParamToTextAdapter,
  onValueChange: (BuilderItem<*>) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  val entries = remember { Day.entries }
  val items = remember(entries) { entries.map { paramToTextAdapter.getDayFullText(DayValue(it)) } }
  var selectedIndex by remember(builderItem) {
    mutableIntStateOf(builderItem.modifier.getValue()?.day?.let { entries.indexOf(it) } ?: 0)
  }
  WheelPicker(
    items = items,
    selectedIndex = selectedIndex,
    onSelectedIndexChange = { index ->
      selectedIndex = index
      builderItem.modifier.update(DayValue(entries[index]))
      onValueChange(builderItem)
    },
    modifier = Modifier.fillMaxWidth(),
    hapticFeedbackEnabled = hapticFeedbackEnabled,
  )
}

/** RRULE numeric parameter slider (e.g. interval, count), ranged per-item via [ICalIntBuilderItem]'s
 *  own `minValue`/`maxValue`. Replaces `ICalIntController`. */
@Composable
internal fun ICalIntValueEditor(
  builderItem: ICalIntBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  var value by remember(builderItem) {
    mutableFloatStateOf((builderItem.modifier.getValue() ?: builderItem.minValue).toFloat())
  }
  val hapticFeedback = LocalHapticFeedback.current
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = builderItem.formatter.format(value.toInt()),
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
    )
    val steps = (builderItem.maxValue - builderItem.minValue - 1).coerceAtLeast(0)
    Slider(
      value = value,
      onValueChange = {
        if (hapticFeedbackEnabled && it != value) {
          hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        value = it
        builderItem.modifier.update(it.toInt())
        onValueChange(builderItem)
      },
      valueRange = builderItem.minValue.toFloat()..builderItem.maxValue.toFloat(),
      steps = steps,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

/** RRULE numeric-list multi-select grid (e.g. by-month, by-month-day), ranged per-item via
 *  [ICalListIntBuilderItem]'s own `minValue`/`maxValue`/`excludedValues`. Replaces
 *  `ICalIntListController`. */
@Composable
internal fun ICalIntListValueEditor(
  builderItem: ICalListIntBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  val values = remember(builderItem) {
    (builderItem.minValue..builderItem.maxValue).filterNot { it in builderItem.excludedValues }
  }
  var selected by remember(builderItem) {
    mutableStateOf(builderItem.modifier.getValue()?.toSet() ?: emptySet())
  }
  SelectableChipGrid(
    items = values,
    selectedItems = selected,
    onItemToggle = { value ->
      selected = if (value in selected) selected - value else selected + value
      builderItem.modifier.update(selected.toList().ifEmpty { null })
      onValueChange(builderItem)
    },
    itemLabel = { it.toString() },
    columns = 8,
    modifier = Modifier.fillMaxWidth().heightIn(max = GRID_MAX_HEIGHT),
  )
}

/** RRULE by-day multi-select grid (Mon..Sun). Replaces `ICalDayValueListController`. */
@Composable
internal fun ICalDayValueListValueEditor(
  builderItem: BuilderItem<List<DayValue>>,
  paramToTextAdapter: ParamToTextAdapter,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  val days = remember { Day.entries.map { DayValue(it) } }
  val labels = remember(days) { days.associateWith { paramToTextAdapter.getDayFullText(it) } }
  var selected by remember(builderItem) {
    mutableStateOf(builderItem.modifier.getValue()?.toSet() ?: emptySet())
  }
  SelectableChipGrid(
    items = days,
    selectedItems = selected,
    onItemToggle = { day ->
      selected = if (day in selected) selected - day else selected + day
      builderItem.modifier.update(selected.toList().ifEmpty { null })
      onValueChange(builderItem)
    },
    itemLabel = { labels[it] ?: it.value },
    columns = 3,
    modifier = Modifier.fillMaxWidth(),
  )
}
