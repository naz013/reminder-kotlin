package com.elementary.tasks.whatsnew

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.elementary.tasks.core.os.PackageManagerWrapper
import com.elementary.tasks.core.utils.params.Prefs

class WhatsNewManager(
  private val prefs: Prefs,
  private val packageManagerWrapper: PackageManagerWrapper
) : DefaultLifecycleObserver {

  private val listeners = mutableListOf<Listener>()

  override fun onResume(owner: LifecycleOwner) {
    super.onResume(owner)
    val versionCode = packageManagerWrapper.getVersionCode()
    val prefsVersionCode = prefs.lastVersionCode
    if (prefsVersionCode < versionCode) {
      notifyListeners(true)
    } else {
      if (prefsVersionCode != versionCode) {
        prefs.lastVersionCode = versionCode
      }
      notifyListeners(false)
    }
  }

  fun hideWhatsNew() {
    prefs.lastVersionCode = packageManagerWrapper.getVersionCode()
    notifyListeners(false)
  }

  fun addListener(listener: Listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener)
    }
  }

  fun removeListener(listener: Listener) {
    if (listeners.contains(listener)) {
      listeners.remove(listener)
    }
  }

  private fun notifyListeners(isVisible: Boolean) {
    listeners.forEach { it.whatsNewVisible(isVisible) }
  }

  interface Listener {
    fun whatsNewVisible(isVisible: Boolean)
  }
}
