package com.elementary.tasks.googletasks

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.googletasks.usecase.tasklist.SyncAllGoogleTaskLists
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.cloudapi.googletasks.GoogleTasksApi
import com.github.naz013.common.ContextProvider
import com.github.naz013.domain.GoogleTask
import com.github.naz013.domain.GoogleTaskList
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import com.github.naz013.ui.common.isColorDark
import com.github.naz013.ui.common.theme.ThemeProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class GoogleTasksViewModel(
  private val googleTasksApi: GoogleTasksApi,
  dispatcherProvider: DispatcherProvider,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val googleTaskRepository: GoogleTaskRepository,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val uiGoogleTaskListAdapter: UiGoogleTaskListAdapter,
  private val syncAllGoogleTaskLists: SyncAllGoogleTaskLists,
  private val contextProvider: ContextProvider,
) : BaseProgressViewModel(dispatcherProvider) {
  val state: StateFlow<GoogleTasksState> field = MutableStateFlow(GoogleTasksState())

  private var isSyncing = false
  private var isBusy = false
  private var isLoginInProgress = false
  private var job: Job? = null

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    load()
  }

  /** Bridges [com.elementary.tasks.core.cloud.GoogleLogin], which needs the Fragment for its
   *  sign-in activity result contract and so cannot live in this ViewModel. */
  fun updateLoginStatus(isLogged: Boolean) {
    state.update { it.copy(isLoggedIn = isLogged) }
    if (isLogged) {
      loadGoogleTasks()
    }
  }

  fun setLoginInProgress(inProgress: Boolean) {
    isLoginInProgress = inProgress
    refreshLoadingFlag()
  }

  private fun load() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val googleTaskLists = googleTaskListRepository.getAll()
      val map = googleTaskLists.associateBy { it.listId }

      val tasks =
        googleTaskRepository.getAll().map {
          uiGoogleTaskListAdapter.convert(it, map[it.listId])
        }

      val defTaskList =
        googleTaskLists.firstOrNull { it.isDefault() }
          ?: googleTaskLists.firstOrNull()

      state.update {
        it.copy(
          taskLists = googleTaskLists.map { list -> list.toEntry() },
          tasks = tasks,
          fabContainerColor = defTaskList?.let { list -> Color(themedColor(list.color)) },
          fabContentColor = defTaskList?.let { list -> fabContentColor(list) },
        )
      }
    }
  }

  private fun GoogleTaskList.toEntry() = UiGoogleTaskListEntry(id = listId, title = title, color = themedColor(color))

  private fun themedColor(colorIndex: Int): Int = ThemeProvider.themedColor(contextProvider.themedContext, colorIndex)

  private fun fabContentColor(list: GoogleTaskList): Color = if (themedColor(list.color).isColorDark()) Color.White else Color.Black

  fun loadGoogleTasks() {
    setBusy(true)
    job =
      viewModelScope.launch(dispatcherProvider.default()) {
        syncAllGoogleTaskLists()
        load()
        withUIContext {
          setBusy(false)
        }
        job = null
      }
  }

  fun sync() {
    if (isSyncing) return
    isSyncing = true
    setBusy(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      syncAllGoogleTaskLists()
      load()
      isSyncing = false
      withUIContext {
        setBusy(false)
      }
    }
  }

  fun toggleTask(taskId: String) {
    setBusy(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      try {
        val googleTask = googleTaskRepository.getById(taskId)
        if (googleTask == null) {
          setBusy(false)
          postCommand(Commands.FAILED)
          return@launch
        }
        val updated =
          if (googleTask.isNeedAction()) {
            googleTasksApi.updateTaskStatus(GoogleTask.TASKS_COMPLETE, googleTask)
          } else {
            googleTasksApi.updateTaskStatus(GoogleTask.TASKS_NEED_ACTION, googleTask)
          }
        updated?.also { googleTaskRepository.save(it) }
        load()
        setBusy(false)
        postCommand(Commands.UPDATED)
        withUIContext {
          appWidgetUpdater.updateScheduleWidget()
        }
      } catch (e: IOException) {
        setBusy(false)
        postCommand(Commands.FAILED)
      }
    }
  }

  private fun setBusy(busy: Boolean) {
    postInProgress(busy)
    isBusy = busy
    refreshLoadingFlag()
  }

  private fun refreshLoadingFlag() {
    state.update { it.copy(isLoading = isBusy || isLoginInProgress) }
  }
}
