package com.github.naz013.feature.reminder.build.bi

import com.github.naz013.feature.reminder.build.BuilderItem
import com.github.naz013.common.system.BuildInfo
import com.github.naz013.common.system.Module
import com.github.naz013.platform.SystemInfo
import com.github.naz013.domain.reminder.BiType
import com.github.naz013.logging.Logger

internal class BiFilter(
  private val locationFilter: LocationFilter,
  private val buildInfo: BuildInfo
) {
  operator fun invoke(item: BuilderItem<*>): Boolean {
    val isEnabled = item.isEnabled
    val isForPro = item.isForPro
    val isProEnabled = !item.isForPro || (item.isForPro && buildInfo.isPro)
    val isInSdkRange = if (item.maxSdk == BuilderItem.MAX_SDK_UNDEFINED) {
      Module.CURRENT_SDK >= item.minSdk
    } else {
      Module.CURRENT_SDK in item.minSdk..item.maxSdk
    }
    val isLocationAllowed = locationFilter(item)
    return (isEnabled && isProEnabled && isInSdkRange && isLocationAllowed).also {
      Logger.d(
        "BiFilter",
        "Item filtered ($it): ${item.biType}, enabled=$isEnabled, isForPro=$isForPro, " +
          "isProEnabled=$isProEnabled, isInSdkRange=$isInSdkRange, isLocationAllowed=$isLocationAllowed",
      )
    }
  }
}

internal class LocationFilter(
  systemInfo: SystemInfo
) {
  private val hasLocation = systemInfo.hasLocation

  operator fun invoke(item: BuilderItem<*>): Boolean =
    if (LOCATION_TYPES.contains(item.biType)) {
      hasLocation
    } else {
      true
    }

  companion object {
    private val LOCATION_TYPES =
      listOf(
        BiType.LEAVING_COORDINATES,
        BiType.ARRIVING_COORDINATES,
        BiType.LOCATION_DELAY_DATE,
        BiType.LOCATION_DELAY_TIME,
      )
  }
}
