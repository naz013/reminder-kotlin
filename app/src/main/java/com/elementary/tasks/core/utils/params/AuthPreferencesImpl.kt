package com.elementary.tasks.core.utils.params

import com.github.naz013.ui.common.login.AuthPreferences

class AuthPreferencesImpl(
  private val prefs: Prefs
) : AuthPreferences {
  override val shufflePinView: Boolean
    get() = prefs.shufflePinView
  override val useFingerprint: Boolean
    get() = prefs.useFingerprint
  override val hasPinCode: Boolean
    get() = prefs.hasPinCode
  override val pinCode: String
    get() = prefs.pinCode
}
