package com.github.naz013.feature.googletask.tasklist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.feature.googletask.GoogleTasksPreferences
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.ui.common.R
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.usecase.googletasks.GetGoogleTaskListByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class EditGoogleTaskListViewModel(
  private val listId: String?,
  private val googleTasksApi: GoogleTasksApi,
  private val dispatcherProvider: DispatcherProvider,
  private val googleTaskRepository: GoogleTaskRepository,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val analyticsEventSender: AnalyticsEventSender,
  private val getGoogleTaskListByIdUseCase: GetGoogleTaskListByIdUseCase,
  private val textProvider: TextProvider,
  private val themeProvider: ThemeProvider,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val preferences: GoogleTasksPreferences,
  private val tagAssignmentRepository: TagAssignmentRepository,
) : ViewModel() {

  private val _state = MutableStateFlow(EditGoogleTaskListState())
  val state = _state.stateInWhileSubscribed(EditGoogleTaskListState())
    .onStart { loadInternal() }

  val navigationEvent: LiveData<Event<EditGoogleTaskListEvent>> field = mutableLiveEventOf()

  init {
    _state.update {
      it.copy(
        hapticFeedbackEnabled = preferences.hapticsEnabled,
        sliderColors = themeProvider.colorsForSliderThemed(),
      )
    }
  }

  fun onNameChange(name: String) {
    _state.update { it.copy(name = name, nameError = false) }
  }

  fun onColorSelected(index: Int) {
    _state.update { it.copy(colorIndex = index) }
  }

  fun onDefaultToggle() {
    if (_state.value.isDefaultLocked) return
    _state.update { it.copy(isDefault = !it.isDefault) }
  }

  fun onDeleteClick() {
    _state.update { it.copy(showDeleteConfirm = true) }
  }

  fun onDeleteDismiss() {
    _state.update { it.copy(showDeleteConfirm = false) }
  }

  fun deleteGoogleTaskList() {
    if (!_state.value.canDelete) {
      Logger.w(TAG, "Cannot delete the Google Task List")
    }
    _state.update { it.copy(showDeleteConfirm = false) }
    val listId = _state.value.id
    Logger.i(TAG, "Deleting Google Task List ($listId)")
    viewModelScope.launch(dispatcherProvider.io()) {
      val taskIds = googleTaskRepository.getAllByList(listId).map { it.taskId }
      if (googleTasksApi.deleteTaskList(listId)) {
        googleTaskListRepository.delete(listId)
        googleTaskRepository.deleteAll(listId)
        taskIds.forEach { taskId -> tagAssignmentRepository.detachAll(taskId, TaggedItemType.GOOGLE_TASK) }
        if (_state.value.wasDefault) {
          googleTaskListRepository.getAll().firstOrNull()?.also {
            it.def = 1
            googleTaskListRepository.save(it)
          }
        }
        appWidgetUpdater.updateScheduleWidget()
        withContext(dispatcherProvider.main()) {
          navigationEvent.emit(EditGoogleTaskListEvent.MoveBack)
        }
      } else {
        withContext(dispatcherProvider.main()) {
          navigationEvent.emit(
            EditGoogleTaskListEvent.ShowError(
              textProvider.getString(R.string.failed_to_update_task)
            )
          )
        }
      }
    }
  }

  fun save() {
    val listName = _state.value.name.trim()
    if (listName.isEmpty()) {
      _state.update { it.copy(nameError = true) }
      return
    }
    val color = _state.value.colorIndex
    val isDefault = _state.value.isDefault
    var isNew = false
    viewModelScope.launch(dispatcherProvider.io()) {
      val editedTaskList = googleTaskListRepository.getById(_state.value.id)
      val item = (editedTaskList ?: GoogleTaskList().also { isNew = true }).apply {
        this.title = listName
        this.color = color
        this.updated = System.currentTimeMillis()
      }
      if (isDefault) {
        item.def = 1
      }

      if (isNew) {
        newGoogleTaskList(item)
      } else {
        updateGoogleTaskList(item)
      }
    }
  }

  private suspend fun newGoogleTaskList(googleTaskList: GoogleTaskList) {
    Logger.i(
      TAG,
      "Creating Google Task List (${googleTaskList.listId}), default=${googleTaskList.isDefault()}",
    )
    if (googleTaskList.isDefault()) {
      googleTaskListRepository.getDefault().forEach {
        it.def = 0
        googleTaskListRepository.save(it)
      }
    }
    googleTasksApi.saveTasksList(googleTaskList.title, googleTaskList.color)
      ?.apply { this.def = googleTaskList.def }
      ?.let {
        googleTaskListRepository.save(it)
        analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_GOOGLE_TASK_LIST))
        appWidgetUpdater.updateScheduleWidget()
        withContext(dispatcherProvider.main()) {
          navigationEvent.emit(EditGoogleTaskListEvent.MoveBack)
        }
      } ?: run {
      withContext(dispatcherProvider.main()) {
        navigationEvent.emit(
          EditGoogleTaskListEvent.ShowError(textProvider.getString(R.string.failed_to_update_task))
        )
      }
    }
  }

  private suspend fun updateGoogleTaskList(googleTaskList: GoogleTaskList) {
    Logger.i(TAG, "Updating Google Task List (${googleTaskList.listId})")
    if (googleTaskList.isDefault()) {
      googleTaskListRepository.getDefault().forEach {
        it.def = 0
        googleTaskListRepository.save(it)
      }
    }
    googleTasksApi.updateTasksList(googleTaskList.title, googleTaskList)?.let {
      googleTaskListRepository.save(it)
      appWidgetUpdater.updateScheduleWidget()
      withContext(dispatcherProvider.main()) {
        navigationEvent.emit(EditGoogleTaskListEvent.MoveBack)
      }
    } ?: run {
      withContext(dispatcherProvider.main()) {
        navigationEvent.emit(
          EditGoogleTaskListEvent.ShowError(textProvider.getString(R.string.failed_to_update_task))
        )
      }
    }
  }

  private fun loadInternal() {
    if (listId == null) {
      Logger.w(TAG, "Google Task List id is null")
      return
    }
    viewModelScope.launch(dispatcherProvider.io()) {
      val editedTaskList = getGoogleTaskListByIdUseCase(listId)
      editedTaskList?.also { list ->
        Logger.w(TAG, "Loaded Google Task List with id: $listId")
        _state.update {
          it.copy(
            id = list.listId,
            name = list.title,
            colorIndex = list.color,
            isDefault = list.isDefault(),
            isDefaultLocked = list.isDefault(),
            canDelete = !list.isDefault(),
            wasDefault = list.isDefault(),
            screenTitleRes = R.string.edit_task_list,
          )
        }
      }
    }
  }

  sealed interface EditGoogleTaskListEvent {
    data object MoveBack : EditGoogleTaskListEvent

    data class ShowError(
      val message: String,
    ) : EditGoogleTaskListEvent
  }

  companion object {
    private const val TAG = "EditGoogleTaskListViewModel"
  }
}
