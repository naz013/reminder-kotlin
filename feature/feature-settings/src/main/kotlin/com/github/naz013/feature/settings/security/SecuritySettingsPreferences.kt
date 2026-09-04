package com.github.naz013.feature.settings.security

interface SecuritySettingsPreferences {
  fun verifyPinCode(pin: String): Boolean
  fun setPinCode(pin: String)
  val hasPinCode: Boolean
  var useFingerprint: Boolean
  var shufflePinView: Boolean
  var isTelephonyEnabled: Boolean
}
