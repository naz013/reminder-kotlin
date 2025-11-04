package com.elementary.tasks.groups.create

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.group.UiGroupEditAdapter
import com.elementary.tasks.core.data.ui.group.UiGroupEdit
import com.elementary.tasks.groups.usecase.DeleteReminderGroupUseCase
import com.elementary.tasks.groups.usecase.SaveReminderGroupUseCase
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.common.datetime.DateTimeManager
import com.github.naz013.common.intent.IntentKeys
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.domain.sync.SyncState
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.logging.Logger
import com.github.naz013.navigation.intent.IntentDataReader
import com.github.naz013.repository.ReminderGroupRepository
import kotlinx.coroutines.launch
import java.util.UUID

class EditGroupViewModel(
  private val id: String,
  dispatcherProvider: DispatcherProvider,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val dateTimeManager: DateTimeManager,
  private val analyticsEventSender: AnalyticsEventSender,
  private val uiGroupEditAdapter: UiGroupEditAdapter,
  private val intentDataReader: IntentDataReader,
  private val deleteReminderGroupUseCase: DeleteReminderGroupUseCase,
  private val saveReminderGroupUseCase: SaveReminderGroupUseCase
) : BaseProgressViewModel(dispatcherProvider) {

  private val _reminderGroup = mutableLiveDataOf<UiGroupEdit>()
  val reminderGroup = _reminderGroup.toLiveData()

  private var isEdited = false
  var hasSameInDb: Boolean = false
    private set
  var isFromFile: Boolean = false
    private set
  var canBeDeleted: Boolean = false
    private set
  var sliderPosition: Int = 0
    private set

  private var localGroup: ReminderGroup? = null

  fun hasId(): Boolean {
    return id.isNotEmpty()
  }

  fun onPositionChanged(position: Int) {
    sliderPosition = position
  }

  fun loadFromIntent() {
    viewModelScope.launch(dispatcherProvider.default()) {
      intentDataReader.get(IntentKeys.INTENT_ITEM, ReminderGroup::class.java)?.run {
        onLoaded(this)
        findSame(this.groupUuId)
      }
    }
  }

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)
    load()
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val reminderGroup = reminderGroupRepository.getById(id) ?: return@launch
      canBeDeleted = reminderGroupRepository.getAll().size > 1 && !reminderGroup.isDefaultGroup
      onLoaded(reminderGroup)
    }
  }

  private fun onLoaded(group: ReminderGroup) {
    if (!isEdited) {
      localGroup = group
      _reminderGroup.postValue(uiGroupEditAdapter.convert(group))
      isEdited = true
    }
  }

  private fun findSame(id: String) {
    viewModelScope.launch(dispatcherProvider.default()) {
      val group = reminderGroupRepository.getById(id)
      hasSameInDb = group != null
    }
  }

  fun saveGroup(title: String, color: Int, isDefault: Boolean, newId: Boolean = false) {
    val reminderGroup = localGroup
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      val wasDefault = reminderGroup?.isDefaultGroup ?: false
      val group = reminderGroup?.copy(
        groupColor = color,
        groupDateTime = dateTimeManager.getNowGmtDateTime(),
        groupTitle = title,
        isDefaultGroup = isDefault,
        groupUuId = if (newId) {
          UUID.randomUUID().toString()
        } else {
          reminderGroup.groupUuId
        }
      ) ?: ReminderGroup(
        groupColor = color,
        groupDateTime = dateTimeManager.getNowGmtDateTime(),
        groupTitle = title,
        isDefaultGroup = isDefault,
        groupUuId = UUID.randomUUID().toString(),
        syncState = SyncState.WaitingForUpload
      )
      analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_GROUP))
      if (!wasDefault && group.isDefaultGroup) {
        val groups = reminderGroupRepository.getAll().map { it.copy(isDefaultGroup = false) }
        reminderGroupRepository.saveAll(groups)
      }
      saveReminderGroupUseCase(group)
      Logger.logEvent("Group saved")
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun deleteGroup() {
    val reminderGroup = localGroup ?: return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      deleteReminderGroupUseCase(reminderGroup.groupUuId)
      postInProgress(false)
      postCommand(Commands.DELETED)
      Logger.logEvent("Group deleted")
    }
  }
}
