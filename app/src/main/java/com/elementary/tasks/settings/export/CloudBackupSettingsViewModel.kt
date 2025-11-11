package com.elementary.tasks.settings.export

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.core.services.JobScheduler
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.toLiveData
import com.github.naz013.feature.common.viewmodel.mutableLiveDataOf
import com.github.naz013.logging.Logger
import com.github.naz013.sync.CloudApiProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CloudBackupSettingsViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val cloudApiProvider: CloudApiProvider,
  private val jobScheduler: JobScheduler,
) : ViewModel(), DefaultLifecycleObserver {

  private val _hasAnyCloudApi = mutableLiveDataOf<Boolean>()
  val hasAnyCloudApi = _hasAnyCloudApi.toLiveData()

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    loadCloudApis()
  }

  fun onAutoBackupIntervalChanged() {
    Logger.i(TAG, "Auto backup interval changed, rescheduling auto backup job.")
    jobScheduler.scheduleAutoBackup()
  }

  private fun loadCloudApis() {
    viewModelScope.launch(dispatcherProvider.default()) {
      val apis = cloudApiProvider.getAllowedCloudApis()
      Logger.i(TAG, "Loaded cloud APIs: ${apis.size}")
      withContext(dispatcherProvider.main()) {
        _hasAnyCloudApi.value = apis.isNotEmpty()
      }
    }
  }

  companion object {
    private const val TAG = "CloudBackupViewModel"
  }
}
