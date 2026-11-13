package com.github.naz013.feature.reminder.snooze

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.naz013.logic.reminder.snooze.SnoozePresetType
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.compose.AppIcons
import com.github.naz013.ui.common.compose.foundation.component.AppModalBottomSheet
import com.github.naz013.ui.common.compose.foundation.component.BottomSheetItem
import com.github.naz013.ui.common.compose.foundation.component.BottomSheetList
import com.github.naz013.ui.common.compose.foundation.PrimaryIconButton
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoozePickerScreen(
  modifier: Modifier = Modifier,
  id: String,
  onFinish: (snoozed: Boolean) -> Unit,
) {
  val viewModel: SnoozePickerViewModel = koinViewModel { parametersOf(id) }
  val state by viewModel.state.collectAsState()
  val context = LocalContext.current

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        SnoozePickerEvent.Snoozed -> {
          Toast.makeText(context.applicationContext, R.string.reminder_snoozed, Toast.LENGTH_SHORT).show()
          onFinish(true)
        }
        SnoozePickerEvent.Finish -> onFinish(false)
      }
    }
  }

  AppModalBottomSheet(
    modifier = modifier,
    onDismissRequest = { viewModel.onDismiss() },
    sheetState = rememberBottomSheetState(SheetValue.Hidden),
    dragHandle = null,
  ) {
    when (state.step) {
      SnoozePickerStep.Presets ->
        PresetsContent(
          presets = state.presets,
          onPresetClick = { viewModel.onPresetClick(it) },
          onPickTimeClick = { viewModel.onPickTimeClick() },
        )
      SnoozePickerStep.CustomTime ->
        CustomTimeContent(
          options = state.customOptions,
          onOptionClick = { viewModel.onCustomMinutesClick(it) },
          onBackClick = { viewModel.onBackClick() },
        )
    }
  }
}

@Composable
private fun PresetsContent(
  presets: List<SnoozePresetType>,
  onPresetClick: (SnoozePresetType) -> Unit,
  onPickTimeClick: () -> Unit,
) {
  Column(modifier = Modifier.padding(16.dp)) {
    SheetTitle(text = stringResource(R.string.snooze_picker_title))
    HorizontalDivider(
      modifier = Modifier.padding(vertical = 8.dp),
      color = MaterialTheme.colorScheme.outlineVariant,
    )
    val items =
      presets.map { BottomSheetItem(id = it.ordinal, title = stringResource(it.titleRes())) } +
        BottomSheetItem(id = PICK_TIME_ID, title = stringResource(R.string.snooze_pick_time))
    BottomSheetList(
      items = items,
      onItemClick = { itemId ->
        if (itemId == PICK_TIME_ID) {
          onPickTimeClick()
        } else {
          SnoozePresetType.entries.getOrNull(itemId)?.let(onPresetClick)
        }
      },
      modifier = Modifier.padding(bottom = 16.dp),
    )
  }
}

@Composable
private fun CustomTimeContent(
  options: List<CustomSnoozeOption>,
  onOptionClick: (Int) -> Unit,
  onBackClick: () -> Unit,
) {
  Column(modifier = Modifier.padding(16.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      PrimaryIconButton(
        icon = AppIcons.Fluent.ChevronLeft,
        contentDescription = stringResource(R.string.cancel),
        onClick = onBackClick,
      )
      Spacer(modifier = Modifier.width(8.dp))
      SheetTitle(text = stringResource(R.string.snooze_pick_time))
    }
    HorizontalDivider(
      modifier = Modifier.padding(vertical = 8.dp),
      color = MaterialTheme.colorScheme.outlineVariant,
    )
    BottomSheetList(
      items =
        options.map {
          BottomSheetItem(id = it.minutes, title = optionTitle(it))
        },
      onItemClick = onOptionClick,
      modifier = Modifier.padding(bottom = 16.dp),
    )
  }
}

@Composable
private fun SheetTitle(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleLarge,
    color = MaterialTheme.colorScheme.onSurface,
    maxLines = 2,
  )
}

@Composable
private fun optionTitle(option: CustomSnoozeOption): String {
  val res =
    when (option.unit) {
      CustomSnoozeUnit.Minutes -> R.string.x_minutes
      CustomSnoozeUnit.Hours -> R.string.x_hours
      CustomSnoozeUnit.Days -> R.string.x_days
    }
  return stringResource(res, option.amount.toString())
}

private fun SnoozePresetType.titleRes(): Int =
  when (this) {
    SnoozePresetType.MINUTES_15 -> R.string.snooze_preset_15_minutes
    SnoozePresetType.HOUR_1 -> R.string.snooze_preset_1_hour
    SnoozePresetType.HOURS_3 -> R.string.snooze_preset_3_hours
    SnoozePresetType.THIS_EVENING -> R.string.snooze_preset_this_evening
    SnoozePresetType.TOMORROW_MORNING -> R.string.snooze_preset_tomorrow_morning
  }

private const val PICK_TIME_ID = -1
