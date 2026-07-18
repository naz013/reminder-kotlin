package com.elementary.tasks.settings.export.services

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elementary.tasks.R
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.googletasks.usecase.tasklist.SyncAllGoogleTaskLists
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Feature
import com.github.naz013.analytics.FeatureUsedEvent
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.appwidgets.AppWidgetUpdater
import com.github.naz013.cloudapi.googledrive.GoogleDriveAuthManager
import com.github.naz013.cloudapi.googletasks.GoogleTasksAuthManager
import com.github.naz013.common.system.SystemInfo
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.logging.Logger
import com.github.naz013.repository.GoogleTaskListRepository
import com.github.naz013.repository.GoogleTaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CloudServicesViewModel(
  private val dispatcherProvider: DispatcherProvider,
  private val appWidgetUpdater: AppWidgetUpdater,
  private val syncAllGoogleTaskLists: SyncAllGoogleTaskLists,
  private val googleTaskListRepository: GoogleTaskListRepository,
  private val googleTaskRepository: GoogleTaskRepository,
  private val featureManager: FeatureManager,
  private val googleDriveAuthManager: GoogleDriveAuthManager,
  private val analyticsEventSender: AnalyticsEventSender,
  private val googleTasksAuthManager: GoogleTasksAuthManager,
  private val systemInfo: SystemInfo,
) : ViewModel() {

  private val _state = MutableStateFlow(CloudServicesState())
  val state = _state.stateInWhileSubscribed(CloudServicesState())
    .onStart { loadState() }

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  private var accountPermissionMode: AccountPermissionMode? = null

  init {
    analyticsEventSender.send(ScreenUsedEvent(Screen.CLOUD_DRIVES))
  }

  fun onGoogleDriveAuthFailed() {
    Logger.w(TAG, "On Google Drive auth failed")
    event.emit(ViewModelEvent.ShowLoginError)
  }

  fun onGoogleTasksAuthFailed() {
    Logger.w(TAG, "On Google Tasks auth failed")
    event.emit(ViewModelEvent.ShowLoginError)
  }

  fun onGoogleDriveClicked() {
    Logger.i(TAG, "On Google Drive button clicked")
    accountPermissionMode = AccountPermissionMode.GoogleDrive
    event.emit(ViewModelEvent.RequestAccountsPermission)
  }

  fun onGoogleTasksClicked() {
    Logger.i(TAG, "On Google Tasks button clicked")
    accountPermissionMode = AccountPermissionMode.GoogleTasks
    event.emit(ViewModelEvent.RequestAccountsPermission)
  }

  fun onDropboxClicked() {
    Logger.i(TAG, "On Dropbox button clicked")
    event.emit(ViewModelEvent.LogInDropbox)
  }

  fun onAccountsPermissionGranted() {
    Logger.i(TAG, "On Accounts permission granted, mode: $accountPermissionMode")
    when (accountPermissionMode) {
      AccountPermissionMode.GoogleDrive -> {
        if (!systemInfo.googlePlayServicesAvailable) {
          event.emit(ViewModelEvent.ShowToast(R.string.google_play_services_not_installed))
        } else if (googleDriveAuthManager.isAuthorized()) {
          event.emit(ViewModelEvent.LogOutGoogleDrive)
        } else {
          event.emit(ViewModelEvent.LogInGoogleDrive)
        }
      }

      AccountPermissionMode.GoogleTasks -> {
        if (!systemInfo.googlePlayServicesAvailable) {
          event.emit(ViewModelEvent.ShowToast(R.string.google_play_services_not_installed))
        } else if (googleTasksAuthManager.isAuthorized()) {
          googleTasksAuthManager.removeUserName()
          _state.update {
            it.copy(
              isGoogleTasksLoggedIn = false
            )
          }
          clearGoogleTasks()
        } else {
          event.emit(ViewModelEvent.LogInGoogleTasks)
        }
      }

      else -> {
        // no-op
      }
    }
  }

  fun onDropboxLoginStateChanged(isLogged: Boolean) {
    Logger.i(TAG, "Dropbox login state changed: $isLogged")
    if (isLogged) analyticsEventSender.send(FeatureUsedEvent(Feature.DROPBOX))
    _state.update { it.copy(isDropboxLoggedIn = isLogged) }
  }

  fun onGoogleTasksLoginStateChanged(isLogged: Boolean) {
    Logger.i(TAG, "Google Tasks login state changed: $isLogged")
    _state.update { it.copy(isGoogleTasksLoggedIn = isLogged) }
    if (isLogged) {
      loadGoogleTasks()
      analyticsEventSender.send(FeatureUsedEvent(Feature.GOOGLE_TASK))
    }
  }

  fun onGoogleDriveLoginStateChanged(isLogged: Boolean) {
    Logger.i(TAG, "Google Drive login state changed: $isLogged")
    _state.update { it.copy(isGoogleDriveLoggedIn = isLogged) }
    if (isLogged) {
      analyticsEventSender.send(FeatureUsedEvent(Feature.GOOGLE_DRIVE))
    }
  }

  fun clearGoogleTasks() {
    _state.update { it.copy(isLoading = true) }
    viewModelScope.launch(dispatcherProvider.default()) {
      googleTaskRepository.deleteAll()
      googleTaskListRepository.deleteAll()
      Logger.i(TAG, "Google tasks cleared.")
      withContext(dispatcherProvider.main()) {
        _state.update { it.copy(isLoading = false) }
        appWidgetUpdater.updateScheduleWidget()
      }
    }
  }

  fun loadGoogleTasks() {
    _state.update { it.copy(isLoading = true) }
    viewModelScope.launch(dispatcherProvider.default()) {
      syncAllGoogleTaskLists()
      Logger.i(TAG, "Google tasks loaded.")
      withContext(dispatcherProvider.main()) {
        _state.update { it.copy(isLoading = false) }
        appWidgetUpdater.updateScheduleWidget()
      }
    }
  }

  private fun loadState() {
    _state.update {
      it.copy(
        isDropboxVisible = featureManager.isFeatureEnabled(FeatureManager.Feature.DROPBOX),
        isGoogleDriveLoggedIn = googleDriveAuthManager.isAuthorized(),
        isGoogleTasksLoggedIn = googleTasksAuthManager.isAuthorized(),
        isGoogleDriveVisible = systemInfo.googlePlayServicesAvailable &&
          featureManager.isFeatureEnabled(FeatureManager.Feature.GOOGLE_DRIVE),
        isGoogleTasksVisible = systemInfo.googlePlayServicesAvailable &&
          featureManager.isFeatureEnabled(FeatureManager.Feature.GOOGLE_TASKS)
      )
    }
  }

  sealed interface ViewModelEvent {
    data object RequestAccountsPermission : ViewModelEvent
    data object LogInGoogleDrive : ViewModelEvent
    data object LogOutGoogleDrive : ViewModelEvent
    data object LogInGoogleTasks : ViewModelEvent
    data object LogInDropbox : ViewModelEvent
    data object ShowLoginError : ViewModelEvent
    data class ShowToast(val messageRes: Int) : ViewModelEvent
  }

  private enum class AccountPermissionMode { GoogleDrive, GoogleTasks }

  companion object {
    private const val TAG = "CloudServicesVM"
  }
}
