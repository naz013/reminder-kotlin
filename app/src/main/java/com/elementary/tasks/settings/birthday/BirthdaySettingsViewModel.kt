package com.elementary.tasks.settings.birthday

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.settings.birthday.work.CheckBirthdaysTask
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.common.TextProvider
import com.github.naz013.platform.SystemInfo
import com.github.naz013.datecalc.DateTimeManager
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.scheduler.JobSchedulerApi
import com.github.naz013.workapi.WorkRequest
import com.github.naz013.workapi.WorkScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.threeten.bp.LocalTime

class BirthdaySettingsViewModel(
  private val prefs: Prefs,
  private val textProvider: TextProvider,
  private val jobScheduler: JobSchedulerApi,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val dateTimeManager: DateTimeManager,
  analyticsEventSender: AnalyticsEventSender,
  private val workScheduler: WorkScheduler,
  private val systemInfo: SystemInfo,
) : ViewModel() {
  val state: StateFlow<BirthdaySettingsState> field = MutableStateFlow(buildState())
  val navigationEvent: LiveData<Event<BirthdaySettingsEvent>> field = mutableLiveEventOf()

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.BIRTHDAY_SETTINGS))
  }

  fun onReminderToggle() {
    val newValue = !prefs.isBirthdayReminderEnabled
    prefs.isBirthdayReminderEnabled = newValue
    if (newValue) {
      jobScheduler.scheduleDailyBirthday()
    } else {
      jobScheduler.cancelDailyBirthday()
    }
    refreshState()
  }

  fun onDaysToBirthdayClick() {
    state.update {
      it.copy(
        dialog = BirthdayDialog.DaysToBirthday(
          previewValue = prefs.daysToBirthday,
          hapticFeedbackEnabled = prefs.hapticsEnabled,
        )
      )
    }
  }

  fun onDaysToBirthdayPreviewChange(value: Int) {
    updateDialog<BirthdayDialog.DaysToBirthday> { it.copy(previewValue = value) }
  }

  fun onDaysToBirthdayConfirm() {
    val dialog = state.value.dialog as? BirthdayDialog.DaysToBirthday ?: return
    prefs.daysToBirthday = dialog.previewValue
    dismissDialog()
  }

  fun onPriorityClick() {
    val options = priorityOptions()
    state.update {
      it.copy(
        dialog =
          BirthdayDialog.Priority(
            title = textProvider.getString(R.string.birthday_notification_priority),
            options = options,
            selectedIndex = prefs.birthdayPriority.coerceIn(options.indices),
          ),
      )
    }
  }

  fun onPriorityOptionSelected(index: Int) {
    prefs.birthdayPriority = index
    dismissDialog()
  }

  fun onReminderTimeClick() {
    navigationEvent.value =
      Event(
        BirthdaySettingsEvent.ShowTimePicker(
          time = dateTimeManager.getBirthdayLocalTime() ?: LocalTime.now(),
          is24Hour = prefs.is24HourFormat,
          title = textProvider.getString(R.string.remind_at),
        )
      )
  }

  fun onTimeSelected(time: LocalTime) {
    prefs.birthdayTime = dateTimeManager.to24HourString(time)
    if (prefs.isBirthdayReminderEnabled) {
      jobScheduler.scheduleDailyBirthday()
    }
    refreshState()
  }

  fun onWidgetToggle() {
    prefs.isBirthdayInWidgetEnabled = !prefs.isBirthdayInWidgetEnabled
    appWidgetUpdater.updateCalendarWidget()
    appWidgetUpdater.updateAllWidgets()
    refreshState()
  }

  fun onHomeDaysClick() {
    state.update {
      it.copy(
        dialog = BirthdayDialog.HomeDays(
          previewValue = prefs.birthdayDurationInDays,
          hapticFeedbackEnabled = prefs.hapticsEnabled,
        )
      )
    }
  }

  fun onHomeDaysPreviewChange(value: Int) {
    updateDialog<BirthdayDialog.HomeDays> { it.copy(previewValue = value) }
  }

  fun onHomeDaysConfirm() {
    val dialog = state.value.dialog as? BirthdayDialog.HomeDays ?: return
    prefs.birthdayDurationInDays = dialog.previewValue
    dismissDialog()
  }

  fun onPermanentToggle() {
    val newValue = !prefs.isBirthdayPermanentEnabled
    prefs.isBirthdayPermanentEnabled = newValue
    if (newValue) {
      jobScheduler.scheduleBirthdayPermanent()
    } else {
      jobScheduler.cancelBirthdayPermanent()
    }
    refreshState()
    navigationEvent.value = Event(BirthdaySettingsEvent.UpdatePermanentNotificationVisibility(newValue))
  }

  fun onGlobalToggle() {
    prefs.isBirthdayGlobalEnabled = !prefs.isBirthdayGlobalEnabled
    refreshState()
  }

  fun onLedToggle() {
    prefs.isBirthdayLedEnabled = !prefs.isBirthdayLedEnabled
    refreshState()
  }

  fun onLedColorClick() {
    val options = ledColorOptions()
    state.update {
      it.copy(
        dialog =
          BirthdayDialog.LedColor(
            title = textProvider.getString(R.string.led_indication_color),
            options = options,
            selectedIndex = prefs.birthdayLedColor.coerceIn(options.indices),
          ),
      )
    }
  }

  fun onLedColorOptionSelected(index: Int) {
    prefs.birthdayLedColor = index
    dismissDialog()
  }

  /** Only called once contact-read permission has already been granted by the Fragment. */
  fun onUseContactsToggle() {
    val newValue = !prefs.isContactBirthdaysEnabled
    prefs.isContactBirthdaysEnabled = newValue
    if (newValue) {
      workScheduler.enqueue(WorkRequest(taskKey = CheckBirthdaysTask.TASK_KEY, tag = CheckBirthdaysTask.TASK_KEY))
    } else {
      jobScheduler.cancelBirthdaysCheck()
    }
    refreshState()
  }

  fun onAutoScanToggle() {
    val newValue = !prefs.isContactAutoCheckEnabled
    prefs.isContactAutoCheckEnabled = newValue
    if (!newValue) {
      jobScheduler.scheduleBirthdaysCheck()
    } else {
      jobScheduler.cancelBirthdaysCheck()
    }
    refreshState()
  }

  fun onDialogDismiss() {
    dismissDialog()
  }

  private inline fun <reified T : BirthdayDialog> updateDialog(crossinline transform: (T) -> T) {
    state.update { current ->
      val dialog = current.dialog as? T ?: return@update current
      current.copy(dialog = transform(dialog))
    }
  }

  private fun dismissDialog() {
    state.update { buildState().copy(dialog = null) }
  }

  private fun refreshState() {
    state.update { buildState().copy(dialog = it.dialog) }
  }

  private fun buildState(): BirthdaySettingsState {
    val isReminderChecked = prefs.isBirthdayReminderEnabled
    val isGlobalChecked = prefs.isBirthdayGlobalEnabled
    val isLedChecked = prefs.isBirthdayLedEnabled
    val isUseContactsChecked = prefs.isContactBirthdaysEnabled
    val ledColorOptions = ledColorOptions()
    val isLedRowEnabled = isReminderChecked && !isGlobalChecked
    return BirthdaySettingsState(
      isReminderChecked = isReminderChecked,
      isDependentEnabled = isReminderChecked,
      daysToBirthday = prefs.daysToBirthday,
      priorityName = priorityOptions()[prefs.birthdayPriority.coerceIn(0, 4)],
      reminderTime = dateTimeManager.getBirthdayVisualTime(),
      isWidgetChecked = prefs.isBirthdayInWidgetEnabled,
      homeDaysText = homeDaysText(prefs.birthdayDurationInDays),
      isPermanentChecked = prefs.isBirthdayPermanentEnabled,
      isGlobalChecked = isGlobalChecked,
      isLedChecked = isLedChecked,
      isLedRowEnabled = isLedRowEnabled,
      ledColorName = ledColorOptions[prefs.birthdayLedColor.coerceIn(ledColorOptions.indices)],
      isLedColorRowEnabled = isLedRowEnabled && isLedChecked,
      isUseContactsChecked = isUseContactsChecked,
      isAutoScanChecked = prefs.isContactAutoCheckEnabled,
      isAutoScanRowEnabled = isReminderChecked && isUseContactsChecked,
      isLedIndicationVisible = systemInfo.hasLedIndication,
    )
  }

  private fun homeDaysText(days: Int): String =
    if (days <= 0) {
      textProvider.getString(R.string.x_day, "1")
    } else {
      textProvider.getString(R.string.x_days, (days + 1).toString())
    }

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
}
