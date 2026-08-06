package com.elementary.tasks.settings.reminders

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.VibrationPlayer
import com.elementary.tasks.core.utils.VibrationPresets
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.TextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.common.system.SystemInfo
import com.github.naz013.domain.reminder.v2.LockScreenVisibility
import com.github.naz013.domain.reminder.v2.ReminderNotificationCategory
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.threeten.bp.LocalTime

class RemindersSettingsViewModel(
  private val prefs: Prefs,
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

  fun onPriorityClick() {
    showChoiceDialog(
      kind = ChoiceDialogKind.PRIORITY,
      title = textProvider.getString(R.string.reminder_default_priority),
      options = priorityOptions(),
      selectedIndex = prefs.defaultPriority,
    )
  }

  fun onCompletedToggle() {
    prefs.moveCompleted = !prefs.moveCompleted
    refreshState()
  }

  fun onWearToggle() {
    prefs.isWearEnabled = !prefs.isWearEnabled
    refreshState()
  }

  fun onSnoozeClick() {
    showSeekDialog(
      kind = SeekDialogKind.SNOOZE,
      title = textProvider.getString(R.string.default_reminder_snooze_time),
      value = prefs.snoozeTime,
    )
  }

  fun onRepeatToggle() {
    prefs.isNotificationRepeatEnabled = !prefs.isNotificationRepeatEnabled
    refreshState()
  }

  fun onRepeatIntervalClick() {
    showSeekDialog(
      kind = SeekDialogKind.REPEAT_INTERVAL,
      title = textProvider.getString(R.string.reminder_notification_repeat_interval),
      value = prefs.notificationRepeatTime,
    )
  }

  fun onSeekValueChange(value: Int) {
    val dialog = state.value.dialog as? RemindersSettingsDialog.Seek ?: return
    if (dialog.previewValue != value && prefs.hapticsEnabled) {
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
      SeekDialogKind.SNOOZE -> prefs.snoozeTime = dialog.previewValue
      SeekDialogKind.REPEAT_INTERVAL -> prefs.notificationRepeatTime = dialog.previewValue
    }
    dismissDialog()
  }

  fun onLedToggle() {
    prefs.isLedEnabled = !prefs.isLedEnabled
    refreshState()
  }

  fun onLedColorClick() {
    showChoiceDialog(
      kind = ChoiceDialogKind.LED_COLOR,
      title = textProvider.getString(R.string.led_indication_color),
      options = ledColorOptions(),
      selectedIndex = prefs.ledColor,
    )
  }

  fun onChoiceOptionSelected(index: Int) {
    val dialog = state.value.dialog as? RemindersSettingsDialog.Choice ?: return
    when (dialog.kind) {
      ChoiceDialogKind.PRIORITY -> prefs.defaultPriority = index
      ChoiceDialogKind.LED_COLOR -> prefs.ledColor = index
      ChoiceDialogKind.DND_ACTION -> prefs.doNotDisturbAction = index
      ChoiceDialogKind.DND_IGNORE -> prefs.doNotDisturbIgnore = index
      ChoiceDialogKind.CATEGORY -> prefs.defaultNotificationCategory = categoryValues()[index]
      ChoiceDialogKind.LOCK_SCREEN_VISIBILITY ->
        prefs.defaultLockScreenVisibility = lockScreenVisibilityValues()[index]
      ChoiceDialogKind.VIBRATION_PATTERN -> {
        val pattern = VibrationPresets.ALL[index].pattern
        prefs.defaultVibrationPattern = pattern
        vibrationPlayer.play(pattern)
      }
    }
    dismissDialog()
  }

  /** Called only once any required notification permission has already been granted. */
  fun onPermanentNotificationToggle() {
    val newValue = !prefs.isSbNotificationEnabled
    prefs.isSbNotificationEnabled = newValue
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
    prefs.isSbIconEnabled = !prefs.isSbIconEnabled
    refreshState()
    navigationEvent.value = Event(RemindersSettingsEvent.ShowPermanentNotification)
  }

  fun onDoNotDisturbToggle() {
    prefs.isDoNotDisturbEnabled = !prefs.isDoNotDisturbEnabled
    refreshState()
  }

  fun onDndFromClick() {
    val time = dateTimeManager.toLocalTime(prefs.doNotDisturbFrom) ?: LocalTime.now()
    navigationEvent.value = Event(
      RemindersSettingsEvent.ShowTimePicker(
        DndTimeTarget.FROM,
        time,
        textProvider.getString(R.string.from),
        prefs.is24HourFormat,
      )
    )
  }

  fun onDndToClick() {
    val time = dateTimeManager.toLocalTime(prefs.doNotDisturbTo) ?: LocalTime.now()
    navigationEvent.value = Event(
      RemindersSettingsEvent.ShowTimePicker(
        DndTimeTarget.TO,
        time,
        textProvider.getString(R.string.to),
        prefs.is24HourFormat,
      )
    )
  }

  fun onTimeSelected(
    target: DndTimeTarget,
    time: LocalTime,
  ) {
    when (target) {
      DndTimeTarget.FROM -> prefs.doNotDisturbFrom = dateTimeManager.to24HourString(time)
      DndTimeTarget.TO -> prefs.doNotDisturbTo = dateTimeManager.to24HourString(time)
    }
    refreshState()
  }

  fun onDndActionClick() {
    showChoiceDialog(
      kind = ChoiceDialogKind.DND_ACTION,
      title = textProvider.getString(R.string.events_that_occured_during),
      options = actionOptions(),
      selectedIndex = prefs.doNotDisturbAction,
    )
  }

  fun onDndIgnoreClick() {
    showChoiceDialog(
      kind = ChoiceDialogKind.DND_IGNORE,
      title = textProvider.getString(R.string.priority),
      options = ignoreOptions(),
      selectedIndex = prefs.doNotDisturbIgnore,
    )
  }

  fun onDefaultVibrateToggle() {
    prefs.isDefaultVibrateEnabled = !prefs.isDefaultVibrateEnabled
    refreshState()
  }

  fun onDefaultBypassDoNotDisturbToggle() {
    prefs.isDefaultBypassDoNotDisturbEnabled = !prefs.isDefaultBypassDoNotDisturbEnabled
    refreshState()
  }

  fun onDefaultWakeScreenToggle() {
    prefs.isDefaultWakeScreenEnabled = !prefs.isDefaultWakeScreenEnabled
    refreshState()
  }

  fun onDefaultCategoryClick() {
    showChoiceDialog(
      kind = ChoiceDialogKind.CATEGORY,
      title = textProvider.getString(R.string.notification_category),
      options = categoryOptions(),
      selectedIndex = categoryValues().indexOf(prefs.defaultNotificationCategory).coerceAtLeast(0),
    )
  }

  fun onDefaultLockScreenVisibilityClick() {
    showChoiceDialog(
      kind = ChoiceDialogKind.LOCK_SCREEN_VISIBILITY,
      title = textProvider.getString(R.string.lock_screen_visibility),
      options = lockScreenVisibilityOptions(),
      selectedIndex = lockScreenVisibilityValues().indexOf(prefs.defaultLockScreenVisibility).coerceAtLeast(0),
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
    val isDoNotDisturbChecked = prefs.isDoNotDisturbEnabled
    val isLedChecked = prefs.isLedEnabled
    val isRepeatChecked = prefs.isNotificationRepeatEnabled
    val isPermanentNotificationChecked = prefs.isSbNotificationEnabled
    return RemindersSettingsState(
      priorityName = priorityOptions()[prefs.defaultPriority.coerceIn(0, 4)],
      isCompletedChecked = prefs.moveCompleted,
      isWearChecked = prefs.isWearEnabled,
      snoozeText = minutesText(prefs.snoozeTime),
      isRepeatChecked = isRepeatChecked,
      repeatIntervalText = minutesText(prefs.notificationRepeatTime),
      isRepeatIntervalRowEnabled = isRepeatChecked,
      isLedVisible = buildInfo.isPro && systemInfo.hasLedIndication,
      isLedChecked = isLedChecked,
      ledColorName = ledColorOptions()[prefs.ledColor.coerceIn(0, 6)],
      isLedColorRowEnabled = isLedChecked,
      isPermanentNotificationChecked = isPermanentNotificationChecked,
      isStatusIconChecked = prefs.isSbIconEnabled,
      isStatusIconRowEnabled = isPermanentNotificationChecked,
      isDoNotDisturbChecked = isDoNotDisturbChecked,
      doNotDisturbFromText =
        dateTimeManager.getTime(
          dateTimeManager.toLocalTime(prefs.doNotDisturbFrom) ?: LocalTime.now(),
        ),
      doNotDisturbToText =
        dateTimeManager.getTime(
          dateTimeManager.toLocalTime(prefs.doNotDisturbTo) ?: LocalTime.now(),
        ),
      doNotDisturbActionName = actionOptions()[prefs.doNotDisturbAction.coerceIn(0, 1)],
      doNotDisturbIgnoreName = ignoreOptions()[prefs.doNotDisturbIgnore.coerceIn(0, 5)],
      isDoNotDisturbDependentEnabled = isDoNotDisturbChecked,
      hasLocation = systemInfo.hasLocation,
      isDefaultVibrateChecked = prefs.isDefaultVibrateEnabled,
      isDefaultBypassDoNotDisturbChecked = prefs.isDefaultBypassDoNotDisturbEnabled,
      isDefaultWakeScreenChecked = prefs.isDefaultWakeScreenEnabled,
      defaultCategoryName = categoryOptions()[
        categoryValues().indexOf(prefs.defaultNotificationCategory).coerceAtLeast(0)
      ],
      defaultLockScreenVisibilityName = lockScreenVisibilityOptions()[
        lockScreenVisibilityValues().indexOf(prefs.defaultLockScreenVisibility).coerceAtLeast(0)
      ],
      defaultVibrationPatternName = vibrationPatternOptions()[selectedVibrationPatternIndex()],
      isInsightsVisible = buildInfo.isPro,
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
    VibrationPresets.ALL.indexOfFirst { it.pattern == prefs.defaultVibrationPattern }.coerceAtLeast(0)

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
