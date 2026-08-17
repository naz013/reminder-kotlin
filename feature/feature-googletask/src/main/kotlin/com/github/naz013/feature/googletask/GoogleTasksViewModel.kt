package com.github.naz013.feature.googletask

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.TextProvider
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.domain.TaggedItemType
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.logic.googletask.usecase.SyncAllGoogleTaskListsUseCase
import com.github.naz013.platform.SystemInfo
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.repository.TagAssignmentRepository
import com.github.naz013.repository.TagRepository
import com.github.naz013.ui.common.isColorDark
import com.github.naz013.ui.common.theme.ThemeProvider
import com.github.naz013.ui.googletask.GoogleTaskItemState
import com.github.naz013.ui.googletask.GoogleTaskItemStateAdapter
import com.github.naz013.ui.tag.TagChipStateAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class GoogleTasksViewModel(
  private val googleTasksApi: GoogleTasksApi,
  private val dispatcherProvider: DispatcherProvider,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val googleTaskRepository: GoogleTaskRepository,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val googleTaskItemStateAdapter: GoogleTaskItemStateAdapter,
  private val syncAllGoogleTaskListsUseCase: SyncAllGoogleTaskListsUseCase,
  private val contextProvider: ContextProvider,
  private val analyticsEventSender: AnalyticsEventSender,
  private val textProvider: TextProvider,
  private val googleTasksAuthManager: GoogleTasksAuthManager,
  private val systemInfo: SystemInfo,
  private val tagAssignmentRepository: TagAssignmentRepository,
  private val tagRepository: TagRepository,
  private val tagChipStateAdapter: TagChipStateAdapter,
) : ViewModel() {

  private val _state = MutableStateFlow(GoogleTasksState())
  val state = _state.stateInWhileSubscribed(GoogleTasksState())
    .onStart { load() }

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  private var loadedTasks: List<GoogleTaskItemState> = emptyList()

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.GOOGLE_TASKS_LIST))
    viewModelScope.launch(dispatcherProvider.default()) {
      tagRepository.observeAll()
        .map { tags -> tags.map { tagChipStateAdapter(it) } }
        .collect { tags ->
          _state.update { it.copy(allTags = tags) }
        }
    }
  }

  fun onTagSelected(tagId: String?) {
    val newSelectedTagId = if (tagId != null && tagId == _state.value.selectedTagId) null else tagId
    _state.update { it.copy(selectedTagId = newSelectedTagId) }
    viewModelScope.launch(dispatcherProvider.main()) {
      applyTagFilter()
    }
  }

  private suspend fun applyTagFilter() {
    val selectedTagId = _state.value.selectedTagId
    val filtered = if (selectedTagId == null) {
      loadedTasks
    } else {
      val ids = withContext(dispatcherProvider.io()) {
        tagAssignmentRepository.getItemIdsForTag(selectedTagId, TaggedItemType.GOOGLE_TASK)
      }.toSet()
      loadedTasks.filter { it.id in ids }
    }
    _state.update { it.copy(tasks = filtered) }
  }

  fun onGoogleTasksAuthFailed() {
    Logger.w(TAG, "On Google Tasks auth failed")
    event.emit(ViewModelEvent.ShowLoginError)
  }

  fun onGoogleTasksLoginStateChanged(isLogged: Boolean) {
    Logger.i(TAG, "Google Tasks login state changed: $isLogged")
    _state.update { it.copy(isLoggedIn = isLogged) }
    if (isLogged) {
      loadGoogleTasks()
      analyticsEventSender.send(FeatureUsedEvent(Feature.GOOGLE_TASK))
    }
  }

  fun onLoginClicked() {
    if (systemInfo.googlePlayServicesAvailable) {
      event.emit(ViewModelEvent.Login)
    } else {
      event.emit(ViewModelEvent.ShowError(textProvider.getString(R.string.google_play_services_not_installed)))
    }
  }

  fun onBackPressed() {
    event.emit(ViewModelEvent.MoveBack)
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.main()) {
      _state.update {
        it.copy(isLoggedIn = googleTasksAuthManager.isAuthorized())
      }
      val googleTaskLists = withContext(dispatcherProvider.io()) {
        googleTaskListRepository.getAll()
      }
      val map = withContext(dispatcherProvider.default()) {
        googleTaskLists.associateBy { it.listId }
      }

      loadedTasks = withContext(dispatcherProvider.io()) {
        googleTaskRepository.getAll().map {
          googleTaskItemStateAdapter.convert(it, map[it.listId])
        }
      }

      val defTaskList = withContext(dispatcherProvider.io()) {
        googleTaskLists.firstOrNull { it.isDefault() }
          ?: googleTaskLists.firstOrNull()
      }

      _state.update {
        it.copy(
          taskLists = googleTaskLists.map { list -> list.toEntry() },
          fabContainerColor = defTaskList?.let { list -> Color(themedColor(list.color)) },
          fabContentColor = defTaskList?.let { list -> fabContentColor(list) },
        )
      }
      applyTagFilter()
    }
  }

  private fun GoogleTaskList.toEntry() = UiGoogleTaskListEntry(id = listId, title = title, color = themedColor(color))

  private fun themedColor(colorIndex: Int): Int = ThemeProvider.themedColor(contextProvider.themedContext, colorIndex)

  private fun fabContentColor(list: GoogleTaskList): Color =
    if (themedColor(list.color).isColorDark()) Color.White else Color.Black

  fun loadGoogleTasks() = refreshTasks { syncAllGoogleTaskListsUseCase() }

  fun sync() {
    if (_state.value.isLoading) return
    refreshTasks { syncAllGoogleTaskListsUseCase() }
  }

  private fun refreshTasks(sync: suspend () -> Unit) {
    _state.update {
      it.copy(isLoading = true)
    }
    viewModelScope.launch(dispatcherProvider.io()) {
      sync()
      load()
      withContext(dispatcherProvider.main()) {
        _state.update {
          it.copy(isLoading = false)
        }
      }
    }
  }

  fun toggleTask(taskId: String) {
    _state.update {
      it.copy(isLoading = true)
    }
    viewModelScope.launch(dispatcherProvider.main()) {
      val googleTask = withContext(dispatcherProvider.io()) {
        googleTaskRepository.getById(taskId)
      } ?: run {
        _state.update {
          it.copy(isLoading = false)
        }
        return@launch
      }

      val updated = withContext(dispatcherProvider.io()) {
        try {
          val updatedGoogleTask = if (googleTask.isNeedAction()) {
            googleTasksApi.updateTaskStatus(GoogleTask.TASKS_COMPLETE, googleTask)
          } else {
            googleTasksApi.updateTaskStatus(GoogleTask.TASKS_NEED_ACTION, googleTask)
          }
          updatedGoogleTask?.also { googleTaskRepository.save(it) }
          updatedGoogleTask != null
        } catch (e: Exception) {
          false
        }
      }

      if (updated) {
        load()
        appWidgetUpdater.updateScheduleWidget()
      }
      _state.update {
        it.copy(isLoading = false)
      }
    }
  }

  sealed interface ViewModelEvent {
    data object MoveBack : ViewModelEvent
    data object Login : ViewModelEvent
    data object ShowLoginError : ViewModelEvent

    data class ShowError(
      val message: String
    ) : ViewModelEvent
  }

  companion object {
    private const val TAG = "GoogleTasksViewModel"
  }
}
