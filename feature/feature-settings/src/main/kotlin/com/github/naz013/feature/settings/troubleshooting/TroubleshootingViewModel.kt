package com.github.naz013.feature.settings.troubleshooting

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.naz013.analytics.AnalyticsEventSender
import com.github.naz013.analytics.Screen
import com.github.naz013.analytics.ScreenUsedEvent
import com.github.naz013.common.ContextProvider
import com.github.naz013.common.PackageManagerWrapper
import com.github.naz013.feature.common.android.SystemServiceProvider
import com.github.naz013.feature.common.coroutine.DispatcherProvider
import com.github.naz013.feature.common.livedata.Event
import com.github.naz013.feature.common.livedata.emit
import com.github.naz013.feature.common.viewmodel.mutableLiveEventOf
import com.github.naz013.feature.common.viewmodel.stateInWhileSubscribed
import com.github.naz013.featureflags.FeatureFlag
import com.github.naz013.featureflags.FeatureFlags
import com.github.naz013.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

internal class TroubleshootingViewModel(
  private val dispatcherProvider: DispatcherProvider,
  systemServiceProvider: SystemServiceProvider,
  private val packageManagerWrapper: PackageManagerWrapper,
  private val featureFlags: FeatureFlags,
  private val contextProvider: ContextProvider,
  private val cacheUtil: TroubleshootingCacheUtil,
  private val analyticsEventSender: AnalyticsEventSender,
) : ViewModel() {

  private val powerManager = systemServiceProvider.providePowerManager()

  private val _state = MutableStateFlow(TroubleshootingScreenState())
  val state = _state.stateInWhileSubscribed(TroubleshootingScreenState())
    .onStart { internalLoad() }

  val event: LiveData<Event<ViewModelEvent>> field = mutableLiveEventOf()

  fun onOpenOptimizationSettingsClicked() {
    analyticsEventSender.send(ScreenUsedEvent(Screen.TROUBLESHOOTING))
    event.emit(ViewModelEvent.OpenOptimizationSettings)
  }

  fun packageName(): String = packageManagerWrapper.getPackageName()

  fun sendLogs() {
    viewModelScope.launch(dispatcherProvider.io()) {
      val logFile = getLogFile() ?: return@launch
      val cacheFile = cacheUtil.cacheFile(logFile) ?: return@launch

      withContext(dispatcherProvider.main()) {
        event.emit(ViewModelEvent.SendLogs(cacheFile))
      }
    }
  }

  private fun getLogFile(): File? {
    val dir = contextProvider.context.dataDir
    val logDir = File(dir, "files/log")
    Logger.d(TAG, "getLogFile: dir = $dir, logDir = $logDir")
    if (!logDir.exists()) return null
    val files = logDir.listFiles() ?: return null
    return files.firstOrNull { it.name.endsWith(".log") }
  }

  private fun internalLoad() {
    checkLogs()
    checkBatteryOptimization()
    checkEmptyView()
  }

  private fun checkLogs() {
    val enabled =
      featureFlags.isEnabled(FeatureFlag.ALLOW_LOGS) &&
        hasLogFiles()
    Logger.d(TAG, "Logging is $enabled")
    _state.update {
      it.copy(showSendLogs = enabled)
    }
  }

  private fun hasLogFiles(): Boolean = getLogFile() != null

  private fun checkBatteryOptimization() {
    val optimizationStatus = powerManager?.isIgnoringBatteryOptimizations(packageName())
    Logger.d(TAG, "Battery optimization is disabled = $optimizationStatus")
    _state.update {
      it.copy(showBatteryOptimizationCard = optimizationStatus?.not() ?: false)
    }
  }

  private fun checkEmptyView() {
    val optimizationDisabled = powerManager?.isIgnoringBatteryOptimizations(packageName()) ?: false
    val logsEnabled = featureFlags.isEnabled(FeatureFlag.ALLOW_LOGS)
    _state.update {
      it.copy(showEmptyView = optimizationDisabled && !logsEnabled)
    }
  }

  sealed interface ViewModelEvent {
    data object OpenOptimizationSettings : ViewModelEvent

    data class SendLogs(
      val file: File,
    ) : ViewModelEvent
  }

  companion object {
    private const val TAG = "TroubleshootingViewModel"
  }
}
