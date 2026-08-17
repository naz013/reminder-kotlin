package com.github.naz013.feature.reminder.build.valuedialog.editor

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.build.BeforeTimeBuilderItem
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.DelayMinutesBuilderItem
import com.github.naz013.feature.reminder.build.RepeatIntervalBuilderItem
import com.github.naz013.feature.reminder.build.RepeatTimeBuilderItem
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.ui.common.compose.foundation.component.NumberStepperField
import com.github.naz013.ui.common.compose.foundation.component.ValueAndTypePicker

/** "Before the reminder fires" duration (N seconds/minutes/hours/days/weeks/months). Replaces
 *  `BeforeTimeController` with [ValueAndTypePicker]. */
@Composable
internal fun BeforeTimeValueEditor(
  builderItem: BeforeTimeBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  val items = stringArrayResource(R.array.before_times).toList()
  val initial = remember(builderItem) { decomposeDuration(builderItem.modifier.getValue()) }
  var value by remember(builderItem) { mutableLongStateOf(initial.first) }
  var typeIndex by remember(builderItem) { mutableIntStateOf(initial.second) }

  ValueAndTypePicker(
    value = value,
    onValueChange = {
      value = it
      builderItem.modifier.update(it * durationMultiplier(typeIndex))
      onValueChange(builderItem)
    },
    typeItems = items,
    selectedTypeIndex = typeIndex,
    onTypeIndexChange = {
      typeIndex = it
      builderItem.modifier.update(value * durationMultiplier(it))
      onValueChange(builderItem)
    },
    hapticFeedbackEnabled = hapticFeedbackEnabled,
    modifier = Modifier.fillMaxWidth(),
  )
}

/** Repeat time duration (N seconds/minutes/hours/days/weeks/months). Replaces
 *  `RepeatTimeController` with [ValueAndTypePicker]. */
@Composable
internal fun RepeatTimeValueEditor(
  builderItem: RepeatTimeBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  val items = stringArrayResource(R.array.repeat_times).toList()
  val initial = remember(builderItem) { decomposeDuration(builderItem.modifier.getValue()) }
  var value by remember(builderItem) { mutableLongStateOf(initial.first) }
  var typeIndex by remember(builderItem) { mutableIntStateOf(initial.second) }

  ValueAndTypePicker(
    value = value,
    onValueChange = {
      value = it
      builderItem.modifier.update(it * durationMultiplier(typeIndex))
      onValueChange(builderItem)
    },
    typeItems = items,
    selectedTypeIndex = typeIndex,
    onTypeIndexChange = {
      typeIndex = it
      builderItem.modifier.update(value * durationMultiplier(it))
      onValueChange(builderItem)
    },
    hapticFeedbackEnabled = hapticFeedbackEnabled,
    modifier = Modifier.fillMaxWidth(),
  )
}

/** Plain repeat interval count (a unit-less number of repeats). Replaces
 *  `RepeatIntervalController`'s `NumberValuePickerView` with [NumberStepperField]. */
@Composable
internal fun RepeatIntervalValueEditor(
  builderItem: RepeatIntervalBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  var value by remember(builderItem) { mutableLongStateOf(builderItem.modifier.getValue() ?: 1L) }
  NumberStepperField(
    value = value,
    onValueChange = {
      value = it
      builderItem.modifier.update(it)
      onValueChange(builderItem)
    },
    hapticFeedbackEnabled = hapticFeedbackEnabled,
    modifier = Modifier.fillMaxWidth(),
  )
}

/** Notification delay, in plain minutes (unlike [BeforeTimeValueEditor], no unit picker - the
 *  domain field is already minutes). Reuses [NumberStepperField] the same way
 *  [RepeatIntervalValueEditor] does. */
@Composable
internal fun DelayMinutesValueEditor(
  builderItem: DelayMinutesBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  var value by remember(builderItem) {
    mutableLongStateOf((builderItem.modifier.getValue() ?: 0).toLong())
  }
  NumberStepperField(
    value = value,
    onValueChange = {
      value = it
      builderItem.modifier.update(it.toInt())
      onValueChange(builderItem)
    },
    minValue = 0,
    maxValue = 999,
    hapticFeedbackEnabled = hapticFeedbackEnabled,
    modifier = Modifier.fillMaxWidth(),
  )
}

/** Splits stored millis into (value, [DateTimeManager.MultiplierType] index) - mirrors
 *  `DateTimeManager.parseBeforeTime`/`parseRepeatTime`, which return the same shape under
 *  different type names. */
private fun decomposeDuration(millis: Long?): Pair<Long, Int> {
  if (millis == null) return 0L to 0
  val beforeTime = DateTimeManager.parseBeforeTime(millis)
  return beforeTime.value to beforeTime.type.index
}

internal fun durationMultiplier(typeIndex: Int): Long =
  when (typeIndex) {
    DateTimeManager.MultiplierType.MINUTE.index -> DateTimeManager.MINUTE
    DateTimeManager.MultiplierType.HOUR.index -> DateTimeManager.HOUR
    DateTimeManager.MultiplierType.DAY.index -> DateTimeManager.DAY
    DateTimeManager.MultiplierType.WEEK.index -> DateTimeManager.DAY * 7
    DateTimeManager.MultiplierType.MONTH.index -> DateTimeManager.DAY * 30
    else -> DateTimeManager.SECOND
  }
