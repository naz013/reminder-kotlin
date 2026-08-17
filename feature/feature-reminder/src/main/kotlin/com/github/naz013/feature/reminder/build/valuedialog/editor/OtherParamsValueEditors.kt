package com.github.naz013.feature.reminder.build.valuedialog.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.ui.common.R
import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.feature.reminder.build.GoogleCalendarDurationBuilderItem
import com.github.naz013.feature.reminder.build.bi.CalendarDuration
import com.github.naz013.feature.reminder.build.bi.OtherParams
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.ui.common.compose.foundation.component.SettingsSwitchItem
import com.github.naz013.ui.common.compose.foundation.component.ValueAndTypePicker

/** Notification override switches (use global defaults, or vibrate/voice/repeat individually).
 *  Replaces `OtherParamsController`. */
@Composable
internal fun OtherParamsValueEditor(
  builderItem: BuilderItem<OtherParams>,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  val initial = builderItem.modifier.getValue() ?: OtherParams()
  var useGlobal by remember(builderItem) { mutableStateOf(initial.useGlobal) }
  var vibrate by remember(builderItem) { mutableStateOf(initial.vibrate) }
  var notifyByVoice by remember(builderItem) { mutableStateOf(initial.notifyByVoice) }
  var repeatNotification by remember(builderItem) { mutableStateOf(initial.repeatNotification) }

  fun commit() {
    val params = if (useGlobal) {
      OtherParams(useGlobal = true)
    } else {
      OtherParams(
        useGlobal = false,
        vibrate = vibrate,
        notifyByVoice = notifyByVoice,
        repeatNotification = repeatNotification,
      )
    }
    builderItem.modifier.update(params)
    onValueChange(builderItem)
  }

  Column(modifier = Modifier.fillMaxWidth()) {
    SettingsSwitchItem(
      title = stringResource(R.string.default_string),
      checked = useGlobal,
      onCheckedChange = { useGlobal = it; commit() },
    )
    SettingsSwitchItem(
      title = stringResource(R.string.vibrate),
      checked = vibrate,
      onCheckedChange = { vibrate = it; commit() },
      enabled = !useGlobal,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.repeat_notification),
      checked = repeatNotification,
      onCheckedChange = { repeatNotification = it; commit() },
      enabled = !useGlobal,
    )
    SettingsSwitchItem(
      title = stringResource(R.string.voice_notification),
      checked = notifyByVoice,
      onCheckedChange = { notifyByVoice = it; commit() },
      enabled = !useGlobal,
    )
  }
}

/** Google Calendar event duration - a value+type wheel duration, or "all day" (which disables
 *  the wheel). Replaces `GoogleCalendarDurationController`. */
@Composable
internal fun GoogleCalendarDurationValueEditor(
  builderItem: GoogleCalendarDurationBuilderItem,
  onValueChange: (BuilderItem<*>) -> Unit,
  hapticFeedbackEnabled: Boolean = true,
) {
  val items = stringArrayResource(R.array.repeat_times).toList()
  val initial = builderItem.modifier.getValue()
  val initialParsed = remember(builderItem) {
    if (initial == null) {
      Triple(0L, 0, false)
    } else {
      val parsed = DateTimeManager.parseRepeatTime(initial.millis)
      Triple(parsed.value, parsed.type.index, initial.allDay)
    }
  }
  var value by remember(builderItem) { mutableStateOf(initialParsed.first) }
  var typeIndex by remember(builderItem) { mutableStateOf(initialParsed.second) }
  var allDay by remember(builderItem) { mutableStateOf(initialParsed.third) }

  fun commit() {
    builderItem.modifier.update(CalendarDuration(allDay = allDay, millis = value * durationMultiplier(typeIndex)))
    onValueChange(builderItem)
  }

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = stringResource(R.string.reminder_title_all_day),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
      )
      Switch(checked = allDay, onCheckedChange = { allDay = it; commit() })
    }
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
      ValueAndTypePicker(
        value = value,
        onValueChange = { value = it; commit() },
        typeItems = items,
        selectedTypeIndex = typeIndex,
        onTypeIndexChange = { typeIndex = it; commit() },
        enabled = !allDay,
        hapticFeedbackEnabled = hapticFeedbackEnabled,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}
