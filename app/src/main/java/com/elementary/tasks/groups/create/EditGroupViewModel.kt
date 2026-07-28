package com.elementary.tasks.groups.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.groups.usecase.DeleteGroupUseCase
import com.elementary.tasks.groups.usecase.SaveGroupUseCase
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.reminder.v2.GroupV2
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.GroupV2Repository
import com.github.naz013.ui.common.compose.toColor
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
) : ViewModel() {

  private val _state = MutableStateFlow(EditGroupState())
  val state = _state.stateInWhileSubscribed(EditGroupState())
    .onStart { load() }

  val navigationEvent: LiveData<Event<NavigationEvent>> field = mutableLiveEventOf()

  init {
    _state.update {
      it.copy(
        sliderColors = ThemeProvider.colorsForSliderThemed(contextProvider.themedContext).map { color -> color.toColor() },
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
            )
          }
        }
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
          )
        }
      }
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
          syncState = SyncState.WaitingForUpload,
        ) ?: GroupV2(
          uuId = uuid,
          title = editState.title,
          color = editState.colorPosition,
          isDefault = editState.isDefault,
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
