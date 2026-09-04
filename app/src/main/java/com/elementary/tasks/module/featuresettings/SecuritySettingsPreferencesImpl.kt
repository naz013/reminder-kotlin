package com.elementary.tasks.module.featuresettings

import com.elementary.tasks.core.utils.params.Prefs
import com.github.naz013.feature.settings.security.SecuritySettingsPreferences

class SecuritySettingsPreferencesImpl(
  private val prefs: Prefs,
) : SecuritySettingsPreferences {
  override fun verifyPinCode(pin: String): Boolean = prefs.verifyPinCode(pin)

  override fun setPinCode(pin: String) {
    prefs.setPinCode(pin)
  }

  override val hasPinCode: Boolean
    get() = prefs.hasPinCode

  override var useFingerprint: Boolean
    get() = prefs.useFingerprint
    set(value) { prefs.useFingerprint = value }

  override var shufflePinView: Boolean
    get() = prefs.shufflePinView
    set(value) { prefs.shufflePinView = value }

  override var isTelephonyEnabled: Boolean
    get() = prefs.isTelephonyEnabled
    set(value) { prefs.isTelephonyEnabled = value }
}
