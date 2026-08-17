package com.github.naz013.feature.settings.security

interface SecuritySettingsPreferences {
  var pinCode: String
  val hasPinCode: Boolean
  var useFingerprint: Boolean
  var shufflePinView: Boolean
  var isTelephonyEnabled: Boolean
}
