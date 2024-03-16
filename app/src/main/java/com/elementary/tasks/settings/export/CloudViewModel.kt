package com.elementary.tasks.settings.export

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.appwidgets.UpdatesHelper
import com.elementary.tasks.core.data.AppDb
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.withUIContext
import com.elementary.tasks.googletasks.usecase.tasklist.SyncAllGoogleTaskLists
import kotlinx.coroutines.launch

class CloudViewModel(
  private val appDb: AppDb,
  private val dispatcherProvider: DispatcherProvider,
  private val updatesHelper: UpdatesHelper,
  private val syncAllGoogleTaskLists: SyncAllGoogleTaskLists
) : ViewModel(), LifecycleObserver {

  var isLoading: MutableLiveData<Boolean> = MutableLiveData()

  fun clearGoogleTasks() {
    isLoading.postValue(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      appDb.googleTasksDao().deleteAll()
      appDb.googleTaskListsDao().deleteAll()
      withUIContext {
        updatesHelper.updateTasksWidget()
        isLoading.postValue(false)
      }
    }
  }

  fun loadGoogleTasks() {
    isLoading.postValue(true)
    viewModelScope.launch(dispatcherProvider.default()) {
      syncAllGoogleTaskLists()
      withUIContext {
        isLoading.postValue(false)
        updatesHelper.updateTasksWidget()
      }
    }
  }
}
