package com.github.naz013.feature.reminder.build.valuedialog.editor

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.naz013.ui.notification.settings.VibrationPlayer
import com.github.naz013.ui.notification.settings.VibrationPresets
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.VibrationPatternBuilderItem
import com.github.naz013.ui.common.compose.foundation.component.WheelPicker

private val DEFAULT_PRESET_INDEX = VibrationPresets.ALL.indexOfFirst { it.pattern.size > 4 }.coerceAtLeast(0)

/** Vibration-pattern preset wheel. There's no free-form millisecond-array editor anywhere in the
 *  app, so this picks from [VibrationPresets.ALL] the same way [PriorityValueEditor] picks from a
 *  fixed option list. */
@Composable
internal fun VibrationPatternValueEditor(
  builderItem: VibrationPatternBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  val presets = VibrationPresets.ALL
  val items = presets.map { stringResource(it.nameRes) }
  val initialIndex = remember(builderItem) {
    val value = builderItem.modifier.getValue()
    presets.indexOfFirst { it.pattern == value }.let { if (it >= 0) it else DEFAULT_PRESET_INDEX }
  }
  var selectedIndex by remember(builderItem) { mutableIntStateOf(initialIndex) }
  val context = LocalContext.current
  val vibrationPlayer = remember { VibrationPlayer(context) }
  WheelPicker(
    items = items,
    selectedIndex = selectedIndex,
    onSelectedIndexChange = { index ->
      selectedIndex = index
      builderItem.modifier.update(presets[index].pattern)
      onValueChange(builderItem)
      vibrationPlayer.play(presets[index].pattern)
    },
    modifier = Modifier.fillMaxWidth(),
    hapticFeedbackEnabled = hapticFeedbackEnabled,
  )
}
