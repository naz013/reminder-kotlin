package com.github.naz013.feature.reminder.settings.snooze

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.logic.reminder.snooze.SnoozePresetType
import com.github.naz013.ui.common.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.threeten.bp.LocalTime

class SnoozePresetsSettingsViewModel(
  private val reminderPreferences: ReminderPreferences,
  private val textProvider: TextProvider,
  private val dateTimeManager: DateTimeManager,
) : ViewModel() {

  val state: StateFlow<SnoozePresetsSettingsState> field = MutableStateFlow(buildState())
  val events: LiveData<Event<SnoozePresetsSettingsEvent>> field = mutableLiveEventOf()

  fun onPresetToggle(preset: SnoozePresetType) {
    val enabled = reminderPreferences.enabledSnoozePresets
    reminderPreferences.enabledSnoozePresets =
      if (preset in enabled) {
        enabled - preset
      } else {
        SnoozePresetType.entries.filter { it in enabled || it == preset }
      }
    refreshState()
  }

  fun onEveningTimeClick() {
    showTimePicker(
      target = SnoozeTimeTarget.EVENING,
      value = reminderPreferences.snoozeEveningTime,
      title = textProvider.getString(R.string.snooze_evening_time),
    )
  }

  fun onMorningTimeClick() {
    showTimePicker(
      target = SnoozeTimeTarget.MORNING,
      value = reminderPreferences.snoozeMorningTime,
      title = textProvider.getString(R.string.snooze_morning_time),
    )
  }

  fun onTimeSelected(
    target: SnoozeTimeTarget,
    time: LocalTime,
  ) {
    val value = dateTimeManager.to24HourString(time)
    when (target) {
      SnoozeTimeTarget.EVENING -> reminderPreferences.snoozeEveningTime = value
      SnoozeTimeTarget.MORNING -> reminderPreferences.snoozeMorningTime = value
    }
    refreshState()
  }

  private fun showTimePicker(
    target: SnoozeTimeTarget,
    value: String,
    title: String,
  ) {
    events.value = Event(
      SnoozePresetsSettingsEvent.ShowTimePicker(
        target = target,
        time = dateTimeManager.toLocalTime(value) ?: LocalTime.now(),
        title = title,
        is24Hour = reminderPreferences.is24HourFormat,
      )
    )
  }

  private fun refreshState() {
    state.value = buildState()
  }

  private fun buildState() = SnoozePresetsSettingsState(
    enabledPresets = reminderPreferences.enabledSnoozePresets.toSet(),
    eveningTimeText = timeText(reminderPreferences.snoozeEveningTime),
    morningTimeText = timeText(reminderPreferences.snoozeMorningTime),
  )

  private fun timeText(value: String): String =
    dateTimeManager.getTime(dateTimeManager.toLocalTime(value) ?: LocalTime.now())
}
