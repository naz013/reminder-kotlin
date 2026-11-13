package com.github.naz013.feature.reminder.snooze

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.domain.reminder.v2.ReminderV2
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.logic.reminder.snooze.SnoozePresetType
import com.github.naz013.logic.reminder.usecase.SnoozeReminderToPresetUseCase
import com.github.naz013.logic.reminder.usecase.SnoozeReminderUseCase
import com.github.naz013.repository.ReminderV2Repository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class SnoozePickerViewModel(
  private val reminderId: String,
  private val reminderV2Repository: ReminderV2Repository,
  private val snoozeReminderToPresetUseCase: SnoozeReminderToPresetUseCase,
  private val snoozeReminderUseCase: SnoozeReminderUseCase,
  private val reminderPreferences: ReminderPreferences,
  private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {
  private val _state =
    MutableStateFlow(
      SnoozePickerState(
        presets = reminderPreferences.enabledSnoozePresets,
        customOptions = CUSTOM_OPTIONS,
      ),
    )
  val state: StateFlow<SnoozePickerState> = _state.asStateFlow()

  private val _events = Channel<SnoozePickerEvent>(Channel.BUFFERED)
  val events: Flow<SnoozePickerEvent> = _events.receiveAsFlow()

  fun onPresetClick(preset: SnoozePresetType) {
    Logger.i(TAG, "Preset $preset selected for reminder id=$reminderId")
    snoozeAndFinish { reminder -> snoozeReminderToPresetUseCase(reminder, preset) }
  }

  fun onPickTimeClick() {
    _state.value = _state.value.copy(step = SnoozePickerStep.CustomTime)
  }

  fun onCustomMinutesClick(minutes: Int) {
    Logger.i(TAG, "Custom snooze of $minutes minutes selected for reminder id=$reminderId")
    snoozeAndFinish { reminder -> snoozeReminderUseCase(reminder, minutes) }
  }

  fun onBackClick() {
    _state.value = _state.value.copy(step = SnoozePickerStep.Presets)
  }

  fun onDismiss() {
    Logger.i(TAG, "Snooze picker dismissed for reminder id=$reminderId")
    _events.trySend(SnoozePickerEvent.Finish)
  }

  private fun snoozeAndFinish(snooze: suspend (ReminderV2) -> Unit) {
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminder = reminderV2Repository.getById(reminderId)
      if (reminder == null) {
        Logger.w(TAG, "Reminder id=$reminderId not found, closing picker")
        _events.send(SnoozePickerEvent.Finish)
        return@launch
      }
      snooze(reminder)
      _events.send(SnoozePickerEvent.Snoozed)
    }
  }

  companion object {
    private const val TAG = "SnoozePickerViewModel"
    private val CUSTOM_OPTIONS =
      listOf(
        CustomSnoozeOption(5, CustomSnoozeUnit.Minutes, 5),
        CustomSnoozeOption(10, CustomSnoozeUnit.Minutes, 10),
        CustomSnoozeOption(15, CustomSnoozeUnit.Minutes, 15),
        CustomSnoozeOption(30, CustomSnoozeUnit.Minutes, 30),
        CustomSnoozeOption(60, CustomSnoozeUnit.Hours, 1),
        CustomSnoozeOption(120, CustomSnoozeUnit.Hours, 2),
        CustomSnoozeOption(180, CustomSnoozeUnit.Hours, 3),
        CustomSnoozeOption(1440, CustomSnoozeUnit.Days, 1),
      )
  }
}

internal sealed interface SnoozePickerEvent {
  data object Snoozed : SnoozePickerEvent

  data object Finish : SnoozePickerEvent
}
