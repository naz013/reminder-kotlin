package com.elementary.tasks.groups.create

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.cloud.FileConfig
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.group.UiGroupEditAdapter
import com.elementary.tasks.core.data.ui.group.UiGroupEdit
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.utils.Constants
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.IdProvider
import com.elementary.tasks.core.utils.datetime.DateTimeManager
import com.elementary.tasks.core.utils.io.MemoryUtil
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.elementary.tasks.core.utils.work.WorkerLauncher
import com.elementary.tasks.groups.work.GroupDeleteBackupWorker
import com.elementary.tasks.groups.work.GroupSingleBackupWorker
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.domain.ReminderGroup
import com.github.naz013.logging.Logger
import com.github.naz013.repository.ReminderGroupRepository
import kotlinx.coroutines.launch
import java.util.UUID

class CreateGroupViewModel(
  private val id: String,
  dispatcherProvider: DispatcherProvider,
  private val workerLauncher: WorkerLauncher,
  private val reminderGroupRepository: ReminderGroupRepository,
  private val dateTimeManager: DateTimeManager,
  private val contextProvider: ContextProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  private val uiGroupEditAdapter: UiGroupEditAdapter,
  private val idProvider: IdProvider
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

  fun onPositionChanged(position: Int) {
    sliderPosition = position
  }

  fun loadFromFile(uri: Uri) {
    viewModelScope.launch(dispatcherProvider.default()) {
      runCatching {
        if (ContentResolver.SCHEME_CONTENT != uri.scheme) {
          val any = MemoryUtil.readFromUri(contextProvider.context, uri, FileConfig.FILE_NAME_GROUP)
          if (any != null && any is ReminderGroup) {
            onLoaded(any)
            findSame(any.groupUuId)
          }
        }
      }
    }
  }

  fun loadFromIntent(group: ReminderGroup?) {
    if (group != null) {
      onLoaded(group)
      findSame(group.groupUuId)
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
        groupUuId = idProvider.generateUuid()
      )
      analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_GROUP))
      if (!wasDefault && group.isDefaultGroup) {
        val groups = reminderGroupRepository.getAll().map { it.copy(isDefaultGroup = false) }
        reminderGroupRepository.saveAll(groups)
      }
      reminderGroupRepository.save(group)
      workerLauncher.startWork(
        GroupSingleBackupWorker::class.java,
        Constants.INTENT_ID,
        group.groupUuId
      )
      Logger.logEvent("Group saved")
      postInProgress(false)
      postCommand(Commands.SAVED)
    }
  }

  fun deleteGroup() {
    val reminderGroup = localGroup ?: return
    postInProgress(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      reminderGroupRepository.delete(reminderGroup.groupUuId)
      postInProgress(false)
      postCommand(Commands.DELETED)
      Logger.logEvent("Group deleted")
      workerLauncher.startWork(
        GroupDeleteBackupWorker::class.java,
        Constants.INTENT_ID,
        reminderGroup.groupUuId
      )
    }
  }
}
