package com.elementary.tasks.settings.export.services

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.googletasks.usecase.tasklist.SyncAllGoogleTaskLists
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CloudServicesFragmentViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val syncAllGoogleTaskLists: SyncAllGoogleTaskLists,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val googleTaskRepository: GoogleTaskRepository
) : ViewModel(), DefaultLifecycleObserver {

  private val _isLoading = mutableLiveDataOf<Boolean>()
  val isLoading = _isLoading.toLiveData()

  fun clearGoogleTasks() {
    _isLoading.value = true
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
    _isLoading.value = true
    viewModelScope.launch(dispatcherProvider.default()) {
      syncAllGoogleTaskLists()
      Logger.i(TAG, "Google tasks loaded.")
      withContext(dispatcherProvider.main()) {
        _isLoading.value = false
        appWidgetUpdater.updateScheduleWidget()
      }
    }
  }

  companion object {
    private const val TAG = "CloudServicesFragmentVM"
  }
}
