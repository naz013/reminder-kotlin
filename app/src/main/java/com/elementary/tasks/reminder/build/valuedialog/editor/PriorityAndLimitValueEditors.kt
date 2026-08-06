package com.elementary.tasks.reminder.build.valuedialog.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.style.TextAlign
import com.elementary.tasks.R
import com.elementary.tasks.reminder.build.BuilderItem
import com.elementary.tasks.reminder.build.PriorityBuilderItem
import com.elementary.tasks.reminder.build.RepeatLimitBuilderItem
import com.github.naz013.ui.common.compose.foundation.component.WheelPicker

private const val DEFAULT_PRIORITY_INDEX = 2
private const val REPEAT_LIMIT_MIN = -1f
private const val REPEAT_LIMIT_MAX = 365f
private const val REPEAT_LIMIT_STEPS = 365

/** Priority wheel (Lowest..Highest, `@array/priorities`). Replaces `PriorityController`'s
 *  `VerticalWheelSelector` with [WheelPicker]. */
@Composable
fun PriorityValueEditor(
  builderItem: PriorityBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  val items = stringArrayResource(R.array.priorities).toList()
  var selectedIndex by remember(builderItem) {
    mutableIntStateOf(builderItem.modifier.getValue() ?: DEFAULT_PRIORITY_INDEX)
  }
  WheelPicker(
    items = items,
    selectedIndex = selectedIndex,
    onSelectedIndexChange = { index ->
      selectedIndex = index
      builderItem.modifier.update(index)
      onValueChange(builderItem)
    },
    modifier = Modifier.fillMaxWidth(),
    hapticFeedbackEnabled = hapticFeedbackEnabled,
  )
}

/** Repeat-limit slider (-1 = no limit, 0..365 = number of repeats). Replaces
 *  `RepeatLimitController`'s custom `ValueSliderView` with a Material 3 [Slider]. */
@Composable
fun RepeatLimitValueEditor(
  builderItem: RepeatLimitBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  var value by remember(builderItem) {
    mutableFloatStateOf((builderItem.modifier.getValue() ?: 0).toFloat())
  }
  val hapticFeedback = LocalHapticFeedback.current
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = builderItem.repeatLimitFormatter.format(value.toInt()),
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
    )
    Slider(
      value = value,
      onValueChange = { newValue ->
        if (hapticFeedbackEnabled && newValue != value) {
          hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        value = newValue
        builderItem.modifier.update(newValue.toInt())
        onValueChange(builderItem)
      },
      valueRange = REPEAT_LIMIT_MIN..REPEAT_LIMIT_MAX,
      steps = REPEAT_LIMIT_STEPS,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}
