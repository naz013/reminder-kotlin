package com.github.naz013.feature.reminder.build.valuedialog.editor

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.github.naz013.feature.reminder.build.BuilderItem
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneOffset

/**
 * Date picker for [BuilderItem]s of type [LocalDate] (`DateBuilderItem`,
 * `LocationDelayDateBuilderItem`). Replaces `DateController`'s native `DatePicker` View with
 * Material 3's `DatePicker`, which operates in UTC millis - dates are converted at UTC midnight
 * on both ends to avoid timezone-shifted off-by-one-day bugs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateValueEditor(
  builderItem: BuilderItem<LocalDate>,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  val initialMillis = remember(builderItem) { builderItem.modifier.getValue()?.toUtcMillis() }
  val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

  LaunchedEffect(state.selectedDateMillis) {
    val millis = state.selectedDateMillis ?: return@LaunchedEffect
    val date = millis.toUtcLocalDate()
    if (date != builderItem.modifier.getValue()) {
      builderItem.modifier.update(date)
      onValueChange(builderItem)
    }
  }

  DatePicker(state = state, modifier = Modifier.fillMaxWidth(), showModeToggle = false)
}

/**
 * Time picker for [BuilderItem]s of type [LocalTime] (`TimeBuilderItem`,
 * `LocationDelayTimeBuilderItem`). Replaces `TimeController`'s native `TimePicker` View with
 * Material 3's clock-face `TimePicker`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeValueEditor(
  builderItem: BuilderItem<LocalTime>,
  is24HourFormat: Boolean,
  onValueChange: (BuilderItem<*>) -> Unit,
) {
  val initialValue = remember(builderItem) { builderItem.modifier.getValue() }
  val state = rememberTimePickerState(
    initialHour = initialValue?.hour ?: LocalTime.now().hour,
    initialMinute = initialValue?.minute ?: LocalTime.now().minute,
    is24Hour = is24HourFormat,
  )

  LaunchedEffect(state.hour, state.minute) {
    val time = LocalTime.of(state.hour, state.minute)
    if (time != builderItem.modifier.getValue()) {
      builderItem.modifier.update(time)
      onValueChange(builderItem)
    }
  }

  TimePicker(state = state, modifier = Modifier.fillMaxWidth())
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toUtcLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
