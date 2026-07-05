package com.elementary.tasks.reminder.build

import androidx.lifecycle.ViewModel
import com.elementary.tasks.core.data.platform.ReminderCreatorConfig
import com.elementary.tasks.core.utils.BuildParams
import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class BuilderConfigureViewModel(
  private val prefs: Prefs,
  private val googleTasksAuthManager: GoogleTasksAuthManager,
) : ViewModel() {

  private val config: ReminderCreatorConfig = prefs.reminderCreatorParams

  val state: StateFlow<BuilderConfigureState> field = MutableStateFlow(buildState())

  fun onSummaryToggle() = toggle(ReminderCreatorConfig::isAutoAddSummary, ReminderCreatorConfig::setAutoAddSummary)

  fun onBeforeToggle() = toggle(ReminderCreatorConfig::isBeforePickerEnabled, ReminderCreatorConfig::setBeforePickerEnabled)

  fun onRepeatToggle() = toggle(ReminderCreatorConfig::isRepeatPickerEnabled, ReminderCreatorConfig::setRepeatPickerEnabled)

  fun onRepeatLimitToggle() = toggle(
    ReminderCreatorConfig::isRepeatLimitPickerEnabled,
    ReminderCreatorConfig::setRepeatLimitPickerEnabled,
  )

  fun onPriorityToggle() = toggle(ReminderCreatorConfig::isPriorityPickerEnabled, ReminderCreatorConfig::setPriorityPickerEnabled)

  fun onAttachmentToggle() = toggle(
    ReminderCreatorConfig::isAttachmentPickerEnabled,
    ReminderCreatorConfig::setAttachmentPickerEnabled,
  )

  fun onCalendarToggle() = toggle(ReminderCreatorConfig::isCalendarPickerEnabled, ReminderCreatorConfig::setCalendarPickerEnabled)

  fun onTasksToggle() = toggle(
    ReminderCreatorConfig::isGoogleTasksPickerEnabled,
    ReminderCreatorConfig::setGoogleTasksPickerEnabled,
  )

  fun onExtraToggle() = toggle(ReminderCreatorConfig::isTuneExtraPickerEnabled, ReminderCreatorConfig::setTuneExtraPickerEnabled)

  fun onLedToggle() = toggle(ReminderCreatorConfig::isLedPickerEnabled, ReminderCreatorConfig::setLedPickerEnabled)

  fun onICalendarToggle() = toggle(ReminderCreatorConfig::isICalendarEnabled, ReminderCreatorConfig::setICalendarEnabled)

  fun onMakeCallToggle() = toggle(ReminderCreatorConfig::isPhoneCallEnabled, ReminderCreatorConfig::setPhoneCallEnabled)

  fun onSendSmsToggle() = toggle(ReminderCreatorConfig::isSendSmsEnabled, ReminderCreatorConfig::setSendSmsEnabled)

  fun onOpenAppToggle() = toggle(ReminderCreatorConfig::isOpenAppEnabled, ReminderCreatorConfig::setOpenAppEnabled)

  fun onOpenLinkToggle() = toggle(ReminderCreatorConfig::isOpenLinkEnabled, ReminderCreatorConfig::setOpenLinkEnabled)

  fun onSendEmailToggle() = toggle(ReminderCreatorConfig::isSendEmailEnabled, ReminderCreatorConfig::setSendEmailEnabled)

  private fun toggle(get: (ReminderCreatorConfig) -> Boolean, set: (ReminderCreatorConfig, Boolean) -> Unit) {
    set(config, !get(config))
    prefs.reminderCreatorParams = config
    Logger.d(TAG, "save: $config")
    state.update { buildState() }
  }

  private fun buildState(): BuilderConfigureState = BuilderConfigureState(
    isSummaryChecked = config.isAutoAddSummary(),
    isBeforeChecked = config.isBeforePickerEnabled(),
    isRepeatChecked = config.isRepeatPickerEnabled(),
    isRepeatLimitChecked = config.isRepeatLimitPickerEnabled(),
    isPriorityChecked = config.isPriorityPickerEnabled(),
    isAttachmentChecked = config.isAttachmentPickerEnabled(),
    isCalendarChecked = config.isCalendarPickerEnabled(),
    isTasksChecked = config.isGoogleTasksPickerEnabled(),
    isTasksRowVisible = googleTasksAuthManager.isAuthorized(),
    isExtraChecked = config.isTuneExtraPickerEnabled(),
    isLedChecked = config.isLedPickerEnabled(),
    isLedRowVisible = BuildParams.isPro,
    isICalendarChecked = config.isICalendarEnabled(),
    isICalendarRowVisible = BuildParams.isPro,
    isMakeCallChecked = config.isPhoneCallEnabled(),
    isSendSmsChecked = config.isSendSmsEnabled(),
    isOpenAppChecked = config.isOpenAppEnabled(),
    isOpenLinkChecked = config.isOpenLinkEnabled(),
    isSendEmailChecked = config.isSendEmailEnabled(),
  )

  companion object {
    private const val TAG = "BuilderConfigureViewModel"
  }
}
