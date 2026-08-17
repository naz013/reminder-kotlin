package com.github.naz013.feature.reminder.build.valuedialog.editor

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.BypassDndBuilderItem
import com.github.naz013.feature.reminder.build.WakeScreenBuilderItem
import com.github.naz013.ui.common.compose.foundation.component.SettingsSwitchItem

/** Single "bypass Do Not Disturb" toggle - presence of the item in the reminder is itself the
 *  override signal, matching how [com.elementary.tasks.reminder.build.LedColorBuilderItem] and
 *  [com.elementary.tasks.reminder.build.PriorityBuilderItem] work rather than the deprecated
 *  [com.elementary.tasks.reminder.build.OtherParamsBuilderItem] "use global" blanket-flag design. */
@Composable
internal fun BypassDndValueEditor(
  builderItem: BypassDndBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  var checked by remember(builderItem) {
    mutableStateOf(builderItem.modifier.getValue() ?: false)
  }
  SettingsSwitchItem(
    title = stringResource(R.string.bypass_do_not_disturb),
    checked = checked,
    onCheckedChange = {
      checked = it
      builderItem.modifier.update(it)
      onValueChange(builderItem)
    },
    subtitleOn = stringResource(R.string.bypass_do_not_disturb_enabled),
    subtitleOff = stringResource(R.string.bypass_do_not_disturb_disabled),
    modifier = Modifier.fillMaxWidth(),
  )
}

/** Single "wake screen" toggle, same presence-is-the-override pattern as [BypassDndValueEditor]. */
@Composable
internal fun WakeScreenValueEditor(
  builderItem: WakeScreenBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  var checked by remember(builderItem) {
    mutableStateOf(builderItem.modifier.getValue() ?: false)
  }
  SettingsSwitchItem(
    title = stringResource(R.string.wake_screen),
    checked = checked,
    onCheckedChange = {
      checked = it
      builderItem.modifier.update(it)
      onValueChange(builderItem)
    },
    subtitleOn = stringResource(R.string.wake_screen_enabled),
    subtitleOff = stringResource(R.string.wake_screen_disabled),
    modifier = Modifier.fillMaxWidth(),
  )
}
