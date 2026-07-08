package com.elementary.tasks.groups.create

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.groups.usecase.DeleteReminderGroupUseCase
import com.elementary.tasks.groups.usecase.SaveReminderGroupUseCase
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.ReminderGroupRepository
import com.github.naz013.ui.common.compose.toColor
import com.github.naz013.ui.common.theme.ThemeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class EditGroupViewModel(
  private val id: String,
  private val arguments: Bundle?,
  private val dispatcherProvider: DispatcherProvider,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val dateTimeManager: DateTimeManager,
  private val analyticsEventSender: AnalyticsEventSender,
  private val intentDataReader: IntentDataReader,
  private val contextProvider: ContextProvider,
  private val deleteReminderGroupUseCase: DeleteReminderGroupUseCase,
  private val saveReminderGroupUseCase: SaveReminderGroupUseCase,
) : ViewModel() {
  private val _state =
    MutableStateFlow(
      EditGroupState(
        sliderColors = ThemeProvider.colorsForSliderThemed(contextProvider.themedContext).map { color -> color.toColor() },
      ),
    )
  val state: StateFlow<EditGroupState> = _state.asStateFlow()
  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  init {
    load()
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
      deleteReminderGroupUseCase(id)
      Logger.i(TAG, "Deleted group, id: $id")

      withContext(dispatcherProvider.main()) {
        navigationEvent.value = Event(NavigationEvent.Back)
      }
    }
  }

  fun onDialogDismiss() {
    dismissDialog()
  }

  private fun dismissDialog() {
    _state.update { it.copy(dialog = null) }
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.io()) {
      val reminderGroupFromFile = getFromIntentIfAvailable()
      if (reminderGroupFromFile != null) {
        val reminderGroupInDb = reminderGroupRepository.getById(id)
        withContext(dispatcherProvider.main()) {
          _state.update {
            it.copy(
              title = reminderGroupFromFile.groupTitle,
              colorPosition = reminderGroupFromFile.groupColor,
              isDefault = reminderGroupFromFile.isDefaultGroup,
              defaultCheckEnabled = !reminderGroupFromFile.isDefaultGroup,
              canDelete = false,
              isFromFile = true,
              hasSameInDb = reminderGroupInDb != null,
              id = reminderGroupFromFile.groupUuId,
              isEdited = false,
            )
          }
        }
        Logger.i(TAG, "Editing group from file, id: ${reminderGroupFromFile.groupUuId}")
        return@launch
      }

      val reminderGroup =
        reminderGroupRepository.getById(id) ?: run {
          Logger.w(TAG, "Group not found, id: $id")
          return@launch
        }
      val canBeDeleted = reminderGroupRepository.countAll() > 1 && !reminderGroup.isDefaultGroup
      withContext(dispatcherProvider.main()) {
        _state.update {
          it.copy(
            title = reminderGroup.groupTitle,
            colorPosition = reminderGroup.groupColor,
            isDefault = reminderGroup.isDefaultGroup,
            defaultCheckEnabled = !reminderGroup.isDefaultGroup,
            canDelete = canBeDeleted,
            id = reminderGroup.groupUuId,
            isEdited = true,
            isFromFile = false,
            hasSameInDb = false,
          )
        }
      }
      Logger.i(TAG, "Editing group, id: ${reminderGroup.groupUuId}")
    }
  }

  private fun getFromIntentIfAvailable(): ReminderGroup? {
    val bundle = arguments ?: return null
    if (!bundle.getBoolean(IntentKeys.INTENT_ITEM, false)) return null
    return intentDataReader.get(IntentKeys.INTENT_ITEM, ReminderGroup::class.java)
  }

  private fun performSave(newId: Boolean = false) {
    val editState = _state.value
    val uuid = editState.id?.takeIf { !newId } ?: UUID.randomUUID().toString()
    viewModelScope.launch(dispatcherProvider.io()) {
      val oldReminderGroup = reminderGroupRepository.getById(uuid)
      val reminderGroup =
        oldReminderGroup?.copy(
          groupTitle = editState.title,
          groupUuId = uuid,
          groupColor = editState.colorPosition,
          groupDateTime = dateTimeManager.getNowGmtDateTime(),
          isDefaultGroup = editState.isDefault,
          syncState = SyncState.WaitingForUpload,
          version = oldReminderGroup.version + 1L,
        ) ?: ReminderGroup(
          groupTitle = editState.title,
          groupUuId = uuid,
          groupColor = editState.colorPosition,
          groupDateTime = dateTimeManager.getNowGmtDateTime(),
          isDefaultGroup = editState.isDefault,
          syncState = SyncState.WaitingForUpload,
        )
      analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_GROUP))
      saveReminderGroupUseCase(reminderGroup)
      Logger.i(TAG, "Saved group, id: ${reminderGroup.groupUuId}")
    }
  }

  sealed interface NavigationEvent {
    data object Back : NavigationEvent
  }

  companion object {
    private const val TAG = "EditGroupViewModel"
  }
}
