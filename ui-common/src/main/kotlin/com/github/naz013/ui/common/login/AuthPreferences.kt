package com.github.naz013.ui.common.login

interface AuthPreferences {
  val shufflePinView: Boolean
  val useFingerprint: Boolean
  val hasPinCode: Boolean
  val pinCode: String
}
