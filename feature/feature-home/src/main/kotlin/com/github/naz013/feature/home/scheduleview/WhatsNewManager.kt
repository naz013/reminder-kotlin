package com.github.naz013.feature.home.scheduleview

import com.github.naz013.feature.home.HomePreferences
import com.github.naz013.common.PackageManagerWrapper

class WhatsNewManager(
  private val homePreferences: HomePreferences,
  private val packageManagerWrapper: PackageManagerWrapper,
) {
  fun hasChanges(): Boolean {
    val versionCode = packageManagerWrapper.getVersionCode()
    val prefsVersionCode = homePreferences.lastVersionCode
    return if (prefsVersionCode < versionCode) {
      true
    } else {
      if (prefsVersionCode != versionCode) {
        homePreferences.lastVersionCode = versionCode
      }
      false
    }
  }

  fun hideWhatsNew() {
    homePreferences.lastVersionCode = packageManagerWrapper.getVersionCode()
  }
}
