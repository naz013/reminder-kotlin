package com.elementary.tasks.settings.troubleshooting

import androidx.lifecycle.LifecycleOwner
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.livedata.toSingleEvent
import com.elementary.tasks.core.os.ContextProvider
import com.elementary.tasks.core.os.PackageManagerWrapper
import com.elementary.tasks.core.os.SystemServiceProvider
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.FeatureManager
import com.elementary.tasks.core.utils.io.CacheUtil
import com.elementary.tasks.core.utils.mutableLiveDataOf
import com.elementary.tasks.core.utils.toLiveData
import com.github.naz013.logging.Logger
import java.io.File

class TroubleshootingViewModel(
  dispatcherProvider: DispatcherProvider,
  systemServiceProvider: SystemServiceProvider,
  private val packageManagerWrapper: PackageManagerWrapper,
  private val featureManager: FeatureManager,
  private val contextProvider: ContextProvider,
  private val cacheUtil: CacheUtil
) : BaseProgressViewModel(dispatcherProvider) {

  private val powerManager = systemServiceProvider.providePowerManager()

  private val _hideBatteryOptimizationCard = mutableLiveDataOf<Boolean>()
  val hideBatteryOptimizationCard = _hideBatteryOptimizationCard.toSingleEvent()

  private val _showEmptyView = mutableLiveDataOf<Boolean>()
  val showEmptyView = _showEmptyView.toLiveData()

  private val _showSendLogs = mutableLiveDataOf<Boolean>()
  val showSendLogs = _showSendLogs.toSingleEvent()

  private val _sendLogFile = mutableLiveDataOf<File>()
  val sendLogFile = _sendLogFile.toSingleEvent()

  fun packageName(): String {
    return packageManagerWrapper.getPackageName()
  }

  fun sendLogs() {
    val logFile = getLogFile() ?: return
    val cacheFile = cacheUtil.cacheFile(logFile) ?: return
    _sendLogFile.postValue(cacheFile)
  }

  private fun getLogFile(): File? {
    val dir = contextProvider.context.dataDir
    val logDir = File(dir, "files/log")
    Logger.d("getLogFile: dir = $dir, logDir = $logDir")
    if (!logDir.exists()) return null
    val files = logDir.listFiles() ?: return null
    return files.firstOrNull { it.name.endsWith(".log") }
  }

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    checkLogs()
    checkBatteryOptimization()
    checkEmptyView()
  }

  private fun checkLogs() {
    val enabled = featureManager.isFeatureEnabled(FeatureManager.Feature.ALLOW_LOGS) &&
      hasLogFiles()
    Logger.d("Logging is $enabled")
    _showSendLogs.postValue(enabled)
  }

  private fun hasLogFiles(): Boolean {
    return getLogFile() != null
  }

  private fun checkBatteryOptimization() {
    val optimizationStatus = powerManager?.isIgnoringBatteryOptimizations(packageName())
    Logger.d("Battery optimization is disabled = $optimizationStatus")
    _hideBatteryOptimizationCard.postValue(optimizationStatus ?: false)
  }

  private fun checkEmptyView() {
    val optimizationDisabled = powerManager?.isIgnoringBatteryOptimizations(packageName()) ?: false
    val logsEnabled = featureManager.isFeatureEnabled(FeatureManager.Feature.ALLOW_LOGS)
    _showEmptyView.postValue(optimizationDisabled && !logsEnabled)
  }
}
