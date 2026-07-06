package com.elementary.tasks.googletasks.tasklist

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.usecase.googletasks.GetGoogleTaskListByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditGoogleTaskListViewModel(
  val listId: String,
  private val googleTasksApi: GoogleTasksApi,
  dispatcherProvider: DispatcherProvider,
  private val googleTaskRepository: GoogleTaskRepository,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val analyticsEventSender: AnalyticsEventSender,
  private val getGoogleTaskListByIdUseCase: GetGoogleTaskListByIdUseCase,
  private val contextProvider: ContextProvider,
  private val textProvider: TextProvider,
) : BaseProgressViewModel(dispatcherProvider) {
  val state: StateFlow<EditGoogleTaskListState> field =
    MutableStateFlow(
      EditGoogleTaskListState(
        sliderColors = ThemeProvider.colorsForSliderThemed(contextProvider.themedContext).map { Color(it) },
      ),
    )
  val navigationEvent: LiveData<Event<EditGoogleTaskListEvent>> field = mutableLiveEventOf()

  private var editedTaskList: GoogleTaskList? = null

  init {
    state.update { it.copy(hasId = hasId()) }
    viewModelScope.launch(dispatcherProvider.default()) {
      setBusy(true)
      editedTaskList = getGoogleTaskListByIdUseCase(listId)
      editedTaskList?.also { list ->
        state.update {
          it.copy(
            name = list.title,
            colorIndex = list.color,
            isDefault = list.isDefault(),
            isDefaultLocked = list.isDefault(),
            canDelete = !list.isDefault(),
          )
        }
      }
      setBusy(false)
    }
  }

  fun hasId(): Boolean = listId.isNotEmpty()

  fun onNameChange(name: String) {
    state.update { it.copy(name = name, nameError = false) }
  }

  fun onColorSelected(index: Int) {
    state.update { it.copy(colorIndex = index) }
  }

  fun onDefaultToggle() {
    if (state.value.isDefaultLocked) return
    state.update { it.copy(isDefault = !it.isDefault) }
  }

  fun onDeleteClick() {
    state.update { it.copy(showDeleteConfirm = true) }
  }

  fun onDeleteDismiss() {
    state.update { it.copy(showDeleteConfirm = false) }
  }

  fun deleteGoogleTaskList() {
    val googleTaskList = editedTaskList ?: return
    state.update { it.copy(showDeleteConfirm = false) }
    setBusy(true)
    Logger.i(TAG, "Deleting Google Task List (${googleTaskList.listId})")
    viewModelScope.launch(dispatcherProvider.default()) {
      if (googleTasksApi.deleteTaskList(googleTaskList.listId)) {
        googleTaskListRepository.delete(googleTaskList.listId)
        googleTaskRepository.deleteAll(googleTaskList.listId)
        if (googleTaskList.def == 1) {
          val lists = googleTaskListRepository.getAll()
          if (lists.isNotEmpty()) {
            val taskList = lists[0]
            taskList.def = 1
            googleTaskListRepository.save(taskList)
          }
        }
        setBusy(false)
        navigationEvent.postValue(Event(EditGoogleTaskListEvent.Deleted))
      } else {
        setBusy(false)
        postError(textProvider.getString(R.string.failed_to_update_task))
      }
    }
  }

  fun save() {
    val listName = state.value.name.trim()
    if (listName.isEmpty()) {
      state.update { it.copy(nameError = true) }
      return
    }
    val color = state.value.colorIndex
    val isDefault = state.value.isDefault
    var isNew = false
    val item =
      (editedTaskList ?: GoogleTaskList().also { isNew = true }).apply {
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

  private fun newGoogleTaskList(googleTaskList: GoogleTaskList) {
    setBusy(true)
    Logger.i(
      TAG,
      "Creating Google Task List (${googleTaskList.listId}), default=${googleTaskList.isDefault()}",
    )
    viewModelScope.launch(dispatcherProvider.default()) {
      if (googleTaskList.isDefault()) {
        googleTaskListRepository.getDefault().forEach {
          it.def = 0
          googleTaskListRepository.save(it)
        }
      }
      googleTasksApi
        .saveTasksList(googleTaskList.title, googleTaskList.color)
        ?.apply { this.def = googleTaskList.def }
        ?.let {
          googleTaskListRepository.save(it)
          analyticsEventSender.send(FeatureUsedEvent(Feature.CREATE_GOOGLE_TASK_LIST))
          setBusy(false)
          navigationEvent.postValue(Event(EditGoogleTaskListEvent.Saved))
        } ?: run {
        setBusy(false)
        postError(textProvider.getString(R.string.failed_to_update_task))
      }
    }
  }

  private fun updateGoogleTaskList(googleTaskList: GoogleTaskList) {
    setBusy(true)
    Logger.i(TAG, "Updating Google Task List (${googleTaskList.listId})")
    viewModelScope.launch(dispatcherProvider.default()) {
      if (googleTaskList.isDefault()) {
        googleTaskListRepository.getDefault().forEach {
          it.def = 0
          googleTaskListRepository.save(it)
        }
      }
      googleTasksApi.updateTasksList(googleTaskList.title, googleTaskList)?.let {
        googleTaskListRepository.save(it)
        setBusy(false)
        navigationEvent.postValue(Event(EditGoogleTaskListEvent.Saved))
      } ?: run {
        setBusy(false)
        postError(textProvider.getString(R.string.failed_to_update_task))
      }
    }
  }

  private fun setBusy(busy: Boolean) {
    postInProgress(busy)
    state.update { it.copy(isLoading = busy) }
  }

  companion object {
    private const val TAG = "EditGoogleTaskListViewModel"
  }
}
