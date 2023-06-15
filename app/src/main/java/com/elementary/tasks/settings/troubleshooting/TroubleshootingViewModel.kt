package com.elementary.tasks.settings.troubleshooting

import androidx.lifecycle.LifecycleOwner
import com.elementary.tasks.core.arch.BaseProgressViewModel
import com.elementary.tasks.core.data.livedata.toSingleEvent
import com.elementary.tasks.core.os.PackageManagerWrapper
import com.elementary.tasks.core.os.SystemServiceProvider
import com.elementary.tasks.core.utils.DispatcherProvider
import com.elementary.tasks.core.utils.mutableLiveDataOf
import timber.log.Timber

class TroubleshootingViewModel(
  dispatcherProvider: DispatcherProvider,
  systemServiceProvider: SystemServiceProvider,
  private val packageManagerWrapper: PackageManagerWrapper
) : BaseProgressViewModel(dispatcherProvider) {

  private val powerManager = systemServiceProvider.providePowerManager()

  private val _hideBatteryOptimizationCard = mutableLiveDataOf<Boolean>()
  val hideBatteryOptimizationCard = _hideBatteryOptimizationCard.toSingleEvent()

  fun packageName(): String {
    return packageManagerWrapper.getPackageName()
  }

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    checkBatteryOptimization()
  }

  private fun checkBatteryOptimization() {
    val optimizationStatus = powerManager?.isIgnoringBatteryOptimizations(packageName())
    Timber.d("checkBatteryOptimization: is disabled = $optimizationStatus")
    _hideBatteryOptimizationCard.postValue(optimizationStatus ?: false)
  }
}
