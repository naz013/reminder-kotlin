package com.elementary.tasks.googletasks

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.Commands
import com.elementary.tasks.core.data.adapter.google.UiGoogleTaskListAdapter
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

  private val isBusy = MutableStateFlow(false)

  init {
    viewModelScope.launch {
      isBusy.collect { loading ->
        postInProgress(loading)
        state.update { it.copy(isLoading = loading) }
      }
    }
  }

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    viewModelScope.launch(dispatcherProvider.default()) { load() }
  }

  /** Bridges [com.elementary.tasks.core.cloud.compose.rememberGoogleTasksLogin], which needs a
   *  Compose-scoped activity-result launcher and so cannot live in this ViewModel. */
  fun updateLoginStatus(isLogged: Boolean) {
    state.update { it.copy(isLoggedIn = isLogged) }
    if (isLogged) {
      loadGoogleTasks()
    }
  }

  private suspend fun load() {
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

  private fun GoogleTaskList.toEntry() = UiGoogleTaskListEntry(id = listId, title = title, color = themedColor(color))

  private fun themedColor(colorIndex: Int): Int = ThemeProvider.themedColor(contextProvider.themedContext, colorIndex)

  private fun fabContentColor(list: GoogleTaskList): Color = if (themedColor(list.color).isColorDark()) Color.White else Color.Black

  fun loadGoogleTasks() = refreshTasks { syncAllGoogleTaskLists() }

  fun sync() {
    if (isBusy.value) return
    refreshTasks { syncAllGoogleTaskLists() }
  }

  private fun refreshTasks(sync: suspend () -> Unit) {
    isBusy.value = true
    viewModelScope.launch(dispatcherProvider.default()) {
      sync()
      load()
      isBusy.value = false
    }
  }

  fun toggleTask(taskId: String) {
    isBusy.value = true
    viewModelScope.launch(dispatcherProvider.default()) {
      try {
        val googleTask = googleTaskRepository.getById(taskId)
        if (googleTask == null) {
          isBusy.value = false
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
        isBusy.value = false
        postCommand(Commands.UPDATED)
        withContext(dispatcherProvider.main()) {
          appWidgetUpdater.updateScheduleWidget()
        }
      } catch (e: IOException) {
        isBusy.value = false
        postCommand(Commands.FAILED)
      }
    }
  }
}
