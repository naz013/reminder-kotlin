package com.github.naz013.feature.reminder.settings.reminders

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.github.naz013.ui.common.R
import com.github.naz013.ui.notification.settings.VibrationPlayer
import com.github.naz013.ui.notification.settings.VibrationPresets
import com.github.naz013.logic.reminder.ReminderPreferences
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureGateTappedEvent
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.platform.SystemInfo
import com.github.naz013.domain.reminder.v2.LockScreenVisibility
import com.github.naz013.domain.reminder.v2.ReminderNotificationCategory
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.threeten.bp.LocalTime

class RemindersSettingsViewModel(
  private val reminderPreferences: ReminderPreferences,
  private val textProvider: TextProvider,
  private val dateTimeManager: DateTimeManager,
  private val analyticsEventSender: AnalyticsEventSender,
  private val systemInfo: SystemInfo,
  private val buildInfo: BuildInfo,
  private val vibrationPlayer: VibrationPlayer,
) : ViewModel() {

  val state: StateFlow<RemindersSettingsState> field = MutableStateFlow(buildState())
  val navigationEvent: LiveData<Event<RemindersSettingsEvent>> field = mutableLiveEventOf()

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.REMINDERS_SETTINGS))
  }

  fun onPresetsClick() {
    navigationEvent.value = Event(RemindersSettingsEvent.OpenPresets)
  }

  fun onLocationClick() {
    navigationEvent.value = Event(RemindersSettingsEvent.OpenLocationSettings)
  }

  fun onWorkflowRulesClick() {
    navigationEvent.value = Event(RemindersSettingsEvent.OpenWorkflowRules)
  }

  fun onInsightsLockedClick() {
    analyticsEventSender.send(FeatureGateTappedEvent(Feature.INSIGHTS))
  }

  fun onPriorityClick() {
    showChoiceDialog(
      kind = ChoiceDialogKind.PRIORITY,
      title = textProvider.getString(R.string.reminder_default_priority),
      options = priorityOptions(),
      selectedIndex = reminderPreferences.defaultPriority,
    )
  }

  fun onCompletedToggle() {
    reminderPreferences.moveCompleted = !reminderPreferences.moveCompleted
    refreshState()
  }

  fun onWearToggle() {
    reminderPreferences.isWearEnabled = !reminderPreferences.isWearEnabled
    refreshState()
  }

  fun onSnoozeClick() {
    showSeekDialog(
      kind = SeekDialogKind.SNOOZE,
      title = textProvider.getString(R.string.default_reminder_snooze_time),
      value = reminderPreferences.snoozeTime,
    )
  }

  fun onRepeatToggle() {
    reminderPreferences.isNotificationRepeatEnabled = !reminderPreferences.isNotificationRepeatEnabled
    refreshState()
  }

  fun onRepeatIntervalClick() {
    showSeekDialog(
      kind = SeekDialogKind.REPEAT_INTERVAL,
      title = textProvider.getString(R.string.reminder_notification_repeat_interval),
      value = reminderPreferences.notificationRepeatTime,
    )
  }

  fun onSeekValueChange(value: Int) {
    val dialog = state.value.dialog as? RemindersSettingsDialog.Seek ?: return
    if (dialog.previewValue != value && reminderPreferences.hapticsEnabled) {
      navigationEvent.value = Event(RemindersSettingsEvent.HapticFeedback)
    }
    state.update { current ->
      val currentDialog = current.dialog as? RemindersSettingsDialog.Seek ?: return@update current
      current.copy(dialog = currentDialog.copy(previewValue = value, formattedValue = minutesText(value)))
    }
  }

  fun onSeekConfirm() {
    val dialog = state.value.dialog as? RemindersSettingsDialog.Seek ?: return
    when (dialog.kind) {
      SeekDialogKind.SNOOZE -> reminderPreferences.snoozeTime = dialog.previewValue
      SeekDialogKind.REPEAT_INTERVAL -> reminderPreferences.notificationRepeatTime = dialog.previewValue
    }
    dismissDialog()
  }

  fun onLedToggle() {
    reminderPreferences.isLedEnabled = !reminderPreferences.isLedEnabled
    refreshState()
  }

  fun onLedColorClick() {
    showChoiceDialog(
      kind = ChoiceDialogKind.LED_COLOR,
      title = textProvider.getString(R.string.led_indication_color),
      options = ledColorOptions(),
      selectedIndex = reminderPreferences.ledColor,
    )
  }

  fun onChoiceOptionSelected(index: Int) {
    val dialog = state.value.dialog as? RemindersSettingsDialog.Choice ?: return
    when (dialog.kind) {
      ChoiceDialogKind.PRIORITY -> reminderPreferences.defaultPriority = index
      ChoiceDialogKind.LED_COLOR -> reminderPreferences.ledColor = index
      ChoiceDialogKind.DND_ACTION -> reminderPreferences.doNotDisturbAction = index
      ChoiceDialogKind.DND_IGNORE -> reminderPreferences.doNotDisturbIgnore = index
      ChoiceDialogKind.CATEGORY -> reminderPreferences.defaultNotificationCategory = categoryValues()[index]
      ChoiceDialogKind.LOCK_SCREEN_VISIBILITY ->
        reminderPreferences.defaultLockScreenVisibility = lockScreenVisibilityValues()[index]
      ChoiceDialogKind.VIBRATION_PATTERN -> {
        val pattern = VibrationPresets.ALL[index].pattern
        reminderPreferences.defaultVibrationPattern = pattern
        vibrationPlayer.play(pattern)
      }
    }
    dismissDialog()
  }

  /** Called only once any required notification permission has already been granted. */
  fun onPermanentNotificationToggle() {
    val newValue = !reminderPreferences.isSbNotificationEnabled
    reminderPreferences.isSbNotificationEnabled = newValue
    refreshState()
    val event =
      if (newValue) {
        RemindersSettingsEvent.ShowPermanentNotification
      } else {
        RemindersSettingsEvent.HidePermanentNotification
      }
    navigationEvent.value = Event(event)
  }

  fun onStatusIconToggle() {
    reminderPreferences.isSbIconEnabled = !reminderPreferences.isSbIconEnabled
    refreshState()
    navigationEvent.value = Event(RemindersSettingsEvent.ShowPermanentNotification)
  }

  fun onDoNotDisturbToggle() {
    reminderPreferences.isDoNotDisturbEnabled = !reminderPreferences.isDoNotDisturbEnabled
    refreshState()
  }

  fun onDndFromClick() {
    val time = dateTimeManager.toLocalTime(reminderPreferences.doNotDisturbFrom) ?: LocalTime.now()
    navigationEvent.value = Event(
      RemindersSettingsEvent.ShowTimePicker(
        DndTimeTarget.FROM,
        time,
        textProvider.getString(R.string.from),
        reminderPreferences.is24HourFormat,
      )
    )
  }

  fun onDndToClick() {
    val time = dateTimeManager.toLocalTime(reminderPreferences.doNotDisturbTo) ?: LocalTime.now()
    navigationEvent.value = Event(
      RemindersSettingsEvent.ShowTimePicker(
        DndTimeTarget.TO,
        time,
        textProvider.getString(R.string.to),
        reminderPreferences.is24HourFormat,
      )
    )
  }

  fun onTimeSelected(
    target: DndTimeTarget,
    time: LocalTime,
  ) {
    when (target) {
      DndTimeTarget.FROM -> reminderPreferences.doNotDisturbFrom = dateTimeManager.to24HourString(time)
      DndTimeTarget.TO -> reminderPreferences.doNotDisturbTo = dateTimeManager.to24HourString(time)
    }
    refreshState()
  }

  fun onDndActionClick() {
    showChoiceDialog(
      kind = ChoiceDialogKind.DND_ACTION,
      title = textProvider.getString(R.string.events_that_occured_during),
      options = actionOptions(),
      selectedIndex = reminderPreferences.doNotDisturbAction,
    )
  }

  fun onDndIgnoreClick() {
    showChoiceDialog(
      kind = ChoiceDialogKind.DND_IGNORE,
      title = textProvider.getString(R.string.priority),
      options = ignoreOptions(),
      selectedIndex = reminderPreferences.doNotDisturbIgnore,
    )
  }

  fun onDefaultVibrateToggle() {
    reminderPreferences.isDefaultVibrateEnabled = !reminderPreferences.isDefaultVibrateEnabled
    refreshState()
  }

  fun onDefaultBypassDoNotDisturbToggle() {
    reminderPreferences.isDefaultBypassDoNotDisturbEnabled = !reminderPreferences.isDefaultBypassDoNotDisturbEnabled
    refreshState()
  }

  fun onDefaultWakeScreenToggle() {
    reminderPreferences.isDefaultWakeScreenEnabled = !reminderPreferences.isDefaultWakeScreenEnabled
    refreshState()
  }

  fun onDefaultCategoryClick() {
    showChoiceDialog(
      kind = ChoiceDialogKind.CATEGORY,
      title = textProvider.getString(R.string.notification_category),
      options = categoryOptions(),
      selectedIndex = categoryValues().indexOf(reminderPreferences.defaultNotificationCategory).coerceAtLeast(0),
    )
  }

  fun onDefaultLockScreenVisibilityClick() {
    showChoiceDialog(
      kind = ChoiceDialogKind.LOCK_SCREEN_VISIBILITY,
      title = textProvider.getString(R.string.lock_screen_visibility),
      options = lockScreenVisibilityOptions(),
      selectedIndex = lockScreenVisibilityValues().indexOf(reminderPreferences.defaultLockScreenVisibility).coerceAtLeast(0),
    )
  }

  fun onDefaultVibrationPatternClick() {
    showChoiceDialog(
      kind = ChoiceDialogKind.VIBRATION_PATTERN,
      title = textProvider.getString(R.string.vibration_pattern),
      options = vibrationPatternOptions(),
      selectedIndex = selectedVibrationPatternIndex(),
    )
  }

  fun onDialogDismiss() {
    dismissDialog()
  }

  private fun showChoiceDialog(
    kind: ChoiceDialogKind,
    title: String,
    options: List<String>,
    selectedIndex: Int,
  ) {
    state.update {
      it.copy(
        dialog =
          RemindersSettingsDialog.Choice(
            kind = kind,
            title = title,
            options = options,
            selectedIndex = selectedIndex.coerceIn(options.indices),
          ),
      )
    }
  }

  private fun showSeekDialog(
    kind: SeekDialogKind,
    title: String,
    value: Int,
  ) {
    state.update {
      it.copy(
        dialog =
          RemindersSettingsDialog.Seek(
            kind = kind,
            title = title,
            previewValue = value,
            formattedValue = minutesText(value),
          ),
      )
    }
  }

  private fun dismissDialog() {
    state.update { buildState().copy(dialog = null) }
  }

  private fun refreshState() {
    state.update { buildState().copy(dialog = it.dialog) }
  }

  private fun buildState(): RemindersSettingsState {
    val isDoNotDisturbChecked = reminderPreferences.isDoNotDisturbEnabled
    val isLedChecked = reminderPreferences.isLedEnabled
    val isRepeatChecked = reminderPreferences.isNotificationRepeatEnabled
    val isPermanentNotificationChecked = reminderPreferences.isSbNotificationEnabled
    return RemindersSettingsState(
      priorityName = priorityOptions()[reminderPreferences.defaultPriority.coerceIn(0, 4)],
      isCompletedChecked = reminderPreferences.moveCompleted,
      isWearChecked = reminderPreferences.isWearEnabled,
      snoozeText = minutesText(reminderPreferences.snoozeTime),
      isRepeatChecked = isRepeatChecked,
      repeatIntervalText = minutesText(reminderPreferences.notificationRepeatTime),
      isRepeatIntervalRowEnabled = isRepeatChecked,
      isLedVisible = buildInfo.isPro && systemInfo.hasLedIndication,
      isLedChecked = isLedChecked,
      ledColorName = ledColorOptions()[reminderPreferences.ledColor.coerceIn(0, 6)],
      isLedColorRowEnabled = isLedChecked,
      isPermanentNotificationChecked = isPermanentNotificationChecked,
      isStatusIconChecked = reminderPreferences.isSbIconEnabled,
      isStatusIconRowEnabled = isPermanentNotificationChecked,
      isDoNotDisturbChecked = isDoNotDisturbChecked,
      doNotDisturbFromText =
        dateTimeManager.getTime(
          dateTimeManager.toLocalTime(reminderPreferences.doNotDisturbFrom) ?: LocalTime.now(),
        ),
      doNotDisturbToText =
        dateTimeManager.getTime(
          dateTimeManager.toLocalTime(reminderPreferences.doNotDisturbTo) ?: LocalTime.now(),
        ),
      doNotDisturbActionName = actionOptions()[reminderPreferences.doNotDisturbAction.coerceIn(0, 1)],
      doNotDisturbIgnoreName = ignoreOptions()[reminderPreferences.doNotDisturbIgnore.coerceIn(0, 5)],
      isDoNotDisturbDependentEnabled = isDoNotDisturbChecked,
      hasLocation = systemInfo.hasLocation,
      isDefaultVibrateChecked = reminderPreferences.isDefaultVibrateEnabled,
      isDefaultBypassDoNotDisturbChecked = reminderPreferences.isDefaultBypassDoNotDisturbEnabled,
      isDefaultWakeScreenChecked = reminderPreferences.isDefaultWakeScreenEnabled,
      defaultCategoryName = categoryOptions()[
        categoryValues().indexOf(reminderPreferences.defaultNotificationCategory).coerceAtLeast(0)
      ],
      defaultLockScreenVisibilityName = lockScreenVisibilityOptions()[
        lockScreenVisibilityValues().indexOf(reminderPreferences.defaultLockScreenVisibility).coerceAtLeast(0)
      ],
      defaultVibrationPatternName = vibrationPatternOptions()[selectedVibrationPatternIndex()],
      isInsightsLocked = !buildInfo.isPro,
    )
  }

  private fun minutesText(minutes: Int): String =
    textProvider.getString(R.string.x_minutes, minutes.toString())

  private fun priorityOptions(): List<String> =
    listOf(
      textProvider.getString(R.string.priority_lowest),
      textProvider.getString(R.string.priority_low),
      textProvider.getString(R.string.priority_normal),
      textProvider.getString(R.string.priority_high),
      textProvider.getString(R.string.priority_highest),
    )

  private fun ledColorOptions(): List<String> =
    listOf(
      textProvider.getString(R.string.red),
      textProvider.getString(R.string.green),
      textProvider.getString(R.string.blue),
      textProvider.getString(R.string.yellow),
      textProvider.getString(R.string.pink),
      textProvider.getString(R.string.dark_orange),
      textProvider.getString(R.string.teal),
    )

  private fun actionOptions(): List<String> =
    listOf(
      textProvider.getString(R.string.schedule_for_later),
      textProvider.getString(R.string.ignore),
    )

  private fun categoryOptions(): List<String> =
    listOf(
      textProvider.getString(R.string.notification_category_default),
      textProvider.getString(R.string.notification_category_alarm),
      textProvider.getString(R.string.notification_category_event),
      textProvider.getString(R.string.notification_category_call),
    )

  private fun categoryValues(): List<String> = ReminderNotificationCategory.entries.map { it.name }

  private fun lockScreenVisibilityOptions(): List<String> =
    listOf(
      textProvider.getString(R.string.lock_screen_visibility_public),
      textProvider.getString(R.string.lock_screen_visibility_private),
      textProvider.getString(R.string.lock_screen_visibility_secret),
    )

  private fun lockScreenVisibilityValues(): List<String> = LockScreenVisibility.entries.map { it.name }

  private fun vibrationPatternOptions(): List<String> = VibrationPresets.ALL.map { textProvider.getString(it.nameRes) }

  private fun selectedVibrationPatternIndex(): Int =
    VibrationPresets.ALL.indexOfFirst { it.pattern == reminderPreferences.defaultVibrationPattern }.coerceAtLeast(0)

  private fun ignoreOptions(): List<String> {
    val andAbove = textProvider.getString(R.string.and_above)
    return listOf(
      "${textProvider.getString(R.string.priority_lowest)} $andAbove",
      "${textProvider.getString(R.string.priority_low)} $andAbove",
      "${textProvider.getString(R.string.priority_normal)} $andAbove",
      "${textProvider.getString(R.string.priority_high)} $andAbove",
      textProvider.getString(R.string.priority_highest),
      textProvider.getString(R.string.do_not_allow),
    )
  }
}
