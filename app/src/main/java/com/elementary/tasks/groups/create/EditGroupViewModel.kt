package com.elementary.tasks.groups.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.VibrationPlayer
import com.elementary.tasks.core.utils.VibrationPresets
import com.elementary.tasks.core.utils.params.Prefs
import com.elementary.tasks.groups.NotificationOverrideSubtitleFormatter
import com.elementary.tasks.groups.usecase.DeleteGroupUseCase
import com.elementary.tasks.groups.usecase.SaveGroupUseCase
import com.elementary.tasks.reminder.build.formatter.CategoryFormatter
import com.elementary.tasks.reminder.build.formatter.LockScreenVisibilityFormatter
import com.elementary.tasks.reminder.build.formatter.PriorityFormatter
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.reminder.v2.LockScreenVisibility
import com.github.naz013.domain.reminder.v2.NotificationSettingsOverride
import com.github.naz013.domain.reminder.v2.ReminderNotificationCategory
import com.github.naz013.domain.reminder.v2.ReminderPriority
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.repository.ReminderSettingsRepository
import com.github.naz013.ui.common.theme.ThemeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class EditGroupViewModel(
  private val id: String,
  private val fromIntentData: Boolean,
  private val dispatcherProvider: DispatcherProvider,
  private val groupV2Repository: GroupV2Repository,
  private val dateTimeManager: DateTimeManager,
  private val analyticsEventSender: AnalyticsEventSender,
  private val intentDataReader: IntentDataReader,
  private val contextProvider: ContextProvider,
  private val deleteGroupUseCase: DeleteGroupUseCase,
  private val saveGroupUseCase: SaveGroupUseCase,
  private val reminderSettingsRepository: ReminderSettingsRepository,
  private val vibrationPlayer: VibrationPlayer,
  private val prefs: Prefs,
  private val notificationOverrideSubtitleFormatter: NotificationOverrideSubtitleFormatter,
  private val themeProvider: ThemeProvider,
) : ViewModel() {

  private val context get() = contextProvider.themedContext

  private val _state = MutableStateFlow(EditGroupState())
  val state = _state.stateInWhileSubscribed(EditGroupState())
    .onStart { load() }

  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  init {
    _state.update {
      it.copy(
        hapticFeedbackEnabled = prefs.hapticsEnabled,
        sliderColors = themeProvider.colorsForSliderThemed(),
      )
    }
  }

  fun onNameChanged(text: String) {
    _state.update { it.copy(title = text, titleError = false) }
  }

  fun onColorSelected(position: Int) {
    _state.update { it.copy(colorPosition = position) }
  }

  fun onDefaultCheckChanged(isDefault: Boolean) {
    _state.update { it.copy(isDefault = isDefault) }
  }

  fun onSaveClick() {
    val title = _state.value.title.trim()
    if (title.isEmpty()) {
      _state.update { it.copy(titleError = true) }
      return
    }
    if (_state.value.isFromFile && _state.value.hasSameInDb) {
      _state.update { it.copy(dialog = EditGroupDialog.CopyConflict) }
      return
    }
    performSave(false)
  }

  fun onCopyKeepClick() {
    dismissDialog()
    performSave(true)
  }

  fun onCopyReplaceClick() {
    dismissDialog()
    performSave(false)
  }

  fun onVibrateClick() {
    showBooleanChoiceDialog(
      GroupNotificationDialogKind.VIBRATE,
      context.getString(R.string.default_vibrate),
      _state.value.notification.vibrate,
    )
  }

  fun onRepeatNotificationClick() {
    showBooleanChoiceDialog(
      GroupNotificationDialogKind.REPEAT_NOTIFICATION,
      context.getString(R.string.repeat_notification),
      _state.value.notification.repeatNotification,
    )
  }

  fun onBypassDndClick() {
    showBooleanChoiceDialog(
      GroupNotificationDialogKind.BYPASS_DND,
      context.getString(R.string.bypass_do_not_disturb),
      _state.value.notification.bypassDoNotDisturb,
    )
  }

  fun onWakeScreenClick() {
    showBooleanChoiceDialog(
      GroupNotificationDialogKind.WAKE_SCREEN,
      context.getString(R.string.wake_screen),
      _state.value.notification.wakeScreen,
    )
  }

  fun onPriorityClick() {
    val options = listOf(inheritLabel()) + priorityOptions()
    val selectedIndex = _state.value.notification.priority?.let { it.ordinal + 1 } ?: 0
    showChoiceDialog(
      kind = GroupNotificationDialogKind.PRIORITY,
      title = context.getString(R.string.reminder_default_priority),
      options = options,
      selectedIndex = selectedIndex
    )
  }

  fun onCategoryClick() {
    val options = listOf(inheritLabel()) + categoryOptions()
    val selectedIndex = _state.value.notification.category?.let { it.ordinal + 1 } ?: 0
    showChoiceDialog(
      kind = GroupNotificationDialogKind.CATEGORY,
      title = context.getString(R.string.notification_category),
      options = options,
      selectedIndex = selectedIndex
    )
  }

  fun onLockScreenVisibilityClick() {
    val options = listOf(inheritLabel()) + lockScreenVisibilityOptions()
    val selectedIndex = _state.value.notification.lockScreenVisibility?.let { it.ordinal + 1 } ?: 0
    showChoiceDialog(
      kind = GroupNotificationDialogKind.LOCK_SCREEN_VISIBILITY,
      title = context.getString(R.string.lock_screen_visibility),
      options = options,
      selectedIndex = selectedIndex,
    )
  }

  fun onVibrationPatternClick() {
    val options = listOf(inheritLabel()) + vibrationPatternOptions()
    val currentPattern = _state.value.notification.vibrationPattern
    val selectedIndex = currentPattern
      ?.let { pattern -> VibrationPresets.ALL.indexOfFirst { it.pattern == pattern } }
      ?.takeIf { it >= 0 }
      ?.let { it + 1 } ?: 0
    showChoiceDialog(
      kind = GroupNotificationDialogKind.VIBRATION_PATTERN,
      title = context.getString(R.string.vibration_pattern),
      options = options,
      selectedIndex = selectedIndex
    )
  }

  fun onNotificationChoiceSelected(index: Int) {
    val dialog = _state.value.dialog as? EditGroupDialog.NotificationChoice ?: return
    if (dialog.kind == GroupNotificationDialogKind.VIBRATION_PATTERN && index > 0) {
      VibrationPresets.ALL.getOrNull(index - 1)?.pattern?.let { vibrationPlayer.play(it) }
    }
    _state.update { current ->
      current.copy(notification = applyNotificationChoice(current.notification, dialog.kind, index), dialog = null)
    }
    refreshNotificationSubtitles()
  }

  private fun applyNotificationChoice(
    n: NotificationSettingsOverride,
    kind: GroupNotificationDialogKind,
    index: Int,
  ): NotificationSettingsOverride =
    when (kind) {
      GroupNotificationDialogKind.VIBRATE -> n.copy(vibrate = booleanFromIndex(index))
      GroupNotificationDialogKind.REPEAT_NOTIFICATION -> n.copy(repeatNotification = booleanFromIndex(index))
      GroupNotificationDialogKind.BYPASS_DND -> n.copy(bypassDoNotDisturb = booleanFromIndex(index))
      GroupNotificationDialogKind.WAKE_SCREEN -> n.copy(wakeScreen = booleanFromIndex(index))
      GroupNotificationDialogKind.PRIORITY ->
        n.copy(priority = if (index == 0) null else ReminderPriority.entries.getOrNull(index - 1))
      GroupNotificationDialogKind.CATEGORY ->
        n.copy(category = if (index == 0) null else ReminderNotificationCategory.entries.getOrNull(index - 1))
      GroupNotificationDialogKind.LOCK_SCREEN_VISIBILITY ->
        n.copy(lockScreenVisibility = if (index == 0) null else LockScreenVisibility.entries.getOrNull(index - 1))
      GroupNotificationDialogKind.VIBRATION_PATTERN ->
        n.copy(vibrationPattern = if (index == 0) null else VibrationPresets.ALL.getOrNull(index - 1)?.pattern)
    }

  fun onDelayMinutesClick() {
    val current = _state.value.notification.delayMinutes
    _state.update {
      it.copy(
        dialog = EditGroupDialog.DelayMinutes(
          isOverridden = current != null,
          previewValue = current ?: reminderSettingsRepository.getNotificationDefaults().delayMinutes,
        ),
      )
    }
  }

  fun onDelayMinutesOverrideToggle(isOverridden: Boolean) {
    _state.update { current ->
      val dialog = current.dialog as? EditGroupDialog.DelayMinutes ?: return@update current
      current.copy(dialog = dialog.copy(isOverridden = isOverridden))
    }
  }

  fun onDelayMinutesPreviewChange(value: Int) {
    _state.update { current ->
      val dialog = current.dialog as? EditGroupDialog.DelayMinutes ?: return@update current
      current.copy(dialog = dialog.copy(previewValue = value))
    }
  }

  fun onDelayMinutesConfirm() {
    val dialog = _state.value.dialog as? EditGroupDialog.DelayMinutes ?: return
    _state.update {
      it.copy(
        notification = it.notification.copy(delayMinutes = if (dialog.isOverridden) dialog.previewValue else null),
        dialog = null,
      )
    }
    refreshNotificationSubtitles()
  }

  fun onDeleteMenuClick() {
    _state.update { it.copy(dialog = EditGroupDialog.DeleteConfirm) }
  }

  fun onDeleteConfirmed() {
    dismissDialog()
    if (!_state.value.canDelete) {
      Logger.e(TAG, "Can't delete group, id: ${_state.value.id}")
      return
    }
    Logger.i(TAG, "Deleting group, id: $id")
    viewModelScope.launch(dispatcherProvider.io()) {
      deleteGroupUseCase(id)
      Logger.i(TAG, "Deleted group, id: $id")

      withContext(dispatcherProvider.main()) {
        navigationEvent.emit(NavigationEvent.Back)
      }
    }
  }

  fun onDialogDismiss() {
    dismissDialog()
  }

  private fun dismissDialog() {
    _state.update { it.copy(dialog = null) }
  }

  private fun booleanFromIndex(index: Int): Boolean? =
    when (index) {
      1 -> true
      2 -> false
      else -> null
    }

  private fun showBooleanChoiceDialog(
    kind: GroupNotificationDialogKind,
    title: String,
    current: Boolean?,
  ) {
    val options = listOf(inheritLabel(), context.getString(R.string.on), context.getString(R.string.off))
    val selectedIndex = when (current) {
      true -> 1
      false -> 2
      null -> 0
    }
    showChoiceDialog(kind, title, options, selectedIndex)
  }

  private fun showChoiceDialog(
    kind: GroupNotificationDialogKind,
    title: String,
    options: List<String>,
    selectedIndex: Int,
  ) {
    _state.update {
      it.copy(
        dialog = EditGroupDialog.NotificationChoice(
          kind = kind,
          title = title,
          options = options,
          selectedIndex = selectedIndex.coerceIn(options.indices),
        ),
      )
    }
  }

  private fun inheritLabel(): String = context.getString(R.string.inherit_from_settings)

  private fun priorityOptions(): List<String> = ReminderPriority.entries.map {
    PriorityFormatter(context).format(it.ordinal)
  }

  private fun categoryOptions(): List<String> =
    ReminderNotificationCategory.entries.map { CategoryFormatter(context).format(it.ordinal) }

  private fun lockScreenVisibilityOptions(): List<String> =
    LockScreenVisibility.entries.map { LockScreenVisibilityFormatter(context).format(it.ordinal) }

  private fun vibrationPatternOptions(): List<String> = VibrationPresets.ALL.map { context.getString(it.nameRes) }

  private fun refreshNotificationSubtitles() {
    val subtitles = notificationOverrideSubtitleFormatter.format(_state.value.notification)
    _state.update { current ->
      current.copy(
        vibrateSubtitle = subtitles.vibrate ?: "",
        repeatNotificationSubtitle = subtitles.repeatNotification ?: "",
        bypassDndSubtitle = subtitles.bypassDnd ?: "",
        wakeScreenSubtitle = subtitles.wakeScreen ?: "",
        prioritySubtitle = subtitles.priority ?: "",
        categorySubtitle = subtitles.category ?: "",
        lockScreenVisibilitySubtitle = subtitles.lockScreenVisibility ?: "",
        vibrationPatternSubtitle = subtitles.vibrationPattern ?: "",
        delayMinutesSubtitle = subtitles.delayMinutes ?: "",
      )
    }
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.io()) {
      val groupFromFile = getFromIntentIfAvailable()
      if (groupFromFile != null) {
        val groupInDb = groupV2Repository.getById(id)
        withContext(dispatcherProvider.main()) {
          _state.update {
            it.copy(
              title = groupFromFile.title,
              colorPosition = groupFromFile.color,
              isDefault = groupFromFile.isDefault,
              defaultCheckEnabled = !groupFromFile.isDefault,
              canDelete = false,
              id = groupFromFile.uuId,
              isEdited = false,
              isFromFile = true,
              hasSameInDb = groupInDb != null,
              notification = groupFromFile.notification,
            )
          }
        }
        refreshNotificationSubtitles()
        Logger.i(TAG, "Editing group from file, id: ${groupFromFile.uuId}")
        return@launch
      }

      val group =
        groupV2Repository.getById(id) ?: run {
          Logger.w(TAG, "Group not found, id: $id")
          return@launch
        }
      val canBeDeleted = groupV2Repository.countAll() > 1 && !group.isDefault
      withContext(dispatcherProvider.main()) {
        _state.update {
          it.copy(
            title = group.title,
            colorPosition = group.color,
            isDefault = group.isDefault,
            defaultCheckEnabled = !group.isDefault,
            canDelete = canBeDeleted,
            id = group.uuId,
            isEdited = true,
            isFromFile = false,
            hasSameInDb = false,
            notification = group.notification,
          )
        }
      }
      refreshNotificationSubtitles()
      Logger.i(TAG, "Editing group, id: ${group.uuId}")
    }
  }

  private fun getFromIntentIfAvailable(): GroupV2? {
    if (!fromIntentData) return null
    return intentDataReader.get(IntentKeys.INTENT_ITEM, GroupV2::class.java)
  }

  private fun performSave(newId: Boolean = false) {
    val editState = _state.value
    val uuid = editState.id?.takeIf { !newId } ?: UUID.randomUUID().toString()
    viewModelScope.launch(dispatcherProvider.io()) {
      val oldGroup = groupV2Repository.getById(uuid)
      val group =
        oldGroup?.copy(
          title = editState.title,
          uuId = uuid,
          color = editState.colorPosition,
          isDefault = editState.isDefault,
          notification = editState.notification,
          syncState = SyncState.WaitingForUpload,
        ) ?: GroupV2(
          uuId = uuid,
          title = editState.title,
          color = editState.colorPosition,
          isDefault = editState.isDefault,
          notification = editState.notification,
          createdAt = dateTimeManager.getCurrentDateTime(),
          syncState = SyncState.WaitingForUpload,
        )
      analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_GROUP))
      saveGroupUseCase(group)
      Logger.i(TAG, "Saved group, id: ${group.uuId}")

      withContext(dispatcherProvider.main()) {
        navigationEvent.emit(NavigationEvent.Back)
      }
    }
  }

  sealed interface NavigationEvent {
    data object Back : NavigationEvent
  }

  companion object {
    private const val TAG = "EditGroupViewModel"
  }
}
