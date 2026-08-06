package com.elementary.tasks.whatsnew

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.common.PackageManagerWrapper

class WhatsNewManager(
  private val prefs: Prefs,
  private val packageManagerWrapper: PackageManagerWrapper,
) {
  fun hasChanges(): Boolean {
    val versionCode = packageManagerWrapper.getVersionCode()
    val prefsVersionCode = prefs.lastVersionCode
    return if (prefsVersionCode < versionCode) {
      true
    } else {
      if (prefsVersionCode != versionCode) {
        prefs.lastVersionCode = versionCode
      }
      false
    }
  }

  fun hideWhatsNew() {
    prefs.lastVersionCode = packageManagerWrapper.getVersionCode()
  }
}
