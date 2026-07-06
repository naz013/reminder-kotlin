package com.elementary.tasks.settings.export.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.googletasks.usecase.tasklist.SyncAllGoogleTaskLists
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CloudServicesViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val syncAllGoogleTaskLists: SyncAllGoogleTaskLists,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val googleTaskRepository: GoogleTaskRepository,
) : ViewModel() {

  val state: StateFlow<CloudServicesState> field = MutableStateFlow(CloudServicesState())

  fun clearGoogleTasks() {
    state.update { it.copy(isLoading = true) }
    viewModelScope.launch(dispatcherProvider.default()) {
      googleTaskRepository.deleteAll()
      googleTaskListRepository.deleteAll()
      Logger.i(TAG, "Google tasks cleared.")
      withContext(dispatcherProvider.main()) {
        appWidgetUpdater.updateScheduleWidget()
      }
    }
  }

  fun loadGoogleTasks() {
    state.update { it.copy(isLoading = true) }
    viewModelScope.launch(dispatcherProvider.default()) {
      syncAllGoogleTaskLists()
      Logger.i(TAG, "Google tasks loaded.")
      withContext(dispatcherProvider.main()) {
        state.update { it.copy(isLoading = false) }
        appWidgetUpdater.updateScheduleWidget()
      }
    }
  }

  companion object {
    private const val TAG = "CloudServicesVM"
  }
}
